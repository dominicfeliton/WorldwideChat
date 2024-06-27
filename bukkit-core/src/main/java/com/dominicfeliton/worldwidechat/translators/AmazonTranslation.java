package com.dominicfeliton.worldwidechat.translators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.Language;
import com.amazonaws.services.translate.model.ListLanguagesRequest;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;

public class AmazonTranslation extends BasicTranslation {

	public AmazonTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public AmazonTranslation(String accessKeyId, String secretKeyId, String region, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("AMAZON_KEY_ID", accessKeyId);
		System.setProperty("AMAZON_SECRET_KEY", secretKeyId);
		System.setProperty("AMAZON_REGION", region);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(new translationTask());
		String finalOut = "";
		
		/* Get test translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {

		CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String call() throws Exception {
			/* Initialize AWS Creds + Translation Object */
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getProperty("AMAZON_KEY_ID"),
					System.getProperty("AMAZON_SECRET_KEY"));
			AmazonTranslate translate = AmazonTranslateClient.builder()
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withRegion(System.getProperty("AMAZON_REGION")).build();

			if (isInitializing) {
				/* Get supported languages from AWS and set them */
				ListLanguagesRequest langRequest = new ListLanguagesRequest();
				List<Language> awsLangs = translate.listLanguages(langRequest).getLanguages();
				refs.debugMsg(awsLangs.size() + "");
				
				/* Convert supportedLangs to our own SupportedLang objs */
				Map<String, SupportedLang> supportedLangs = new HashMap<>();
				for (Language eaLang : awsLangs) {
					// Don't add auto
					if (eaLang.getLanguageCode().equals("auto") || eaLang.getLanguageName().equals("auto")) {
						continue;
					}

					// Add all entries
					SupportedLang langObj = new SupportedLang(eaLang.getLanguageCode(), eaLang.getLanguageName());
					supportedLangs.put(eaLang.getLanguageCode(), langObj);
					supportedLangs.put(eaLang.getLanguageName(), langObj);
				}

				/* Set supported translator langs */
				main.setInputLangs(refs.fixLangNames(supportedLangs, true, false));
				main.setOutputLangs(refs.fixLangNames(supportedLangs, true, false));

				/* Setup test translation */
				textToTranslate = "Hi, how are you?";
				inputLang = "en";
				outputLang = "es";
			}

			/* Get language code of current input/output language. 
			 * APIs generally recognize language codes (en, es, etc.)
			 * instead of full names (English, Spanish) */
			if (!isInitializing) {
				if (!inputLang.equals("None")) {
					inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
				}
				outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
			}
			
			/* Actual translation */
			TranslateTextRequest request = new TranslateTextRequest().withText(textToTranslate)
					.withSourceLanguageCode(inputLang.equals("None") ? "auto" : inputLang)
					.withTargetLanguageCode(outputLang);
			TranslateTextResult result = translate.translateText(request);

			/* Process + Return final result */
			return result.getTranslatedText();
		}
	}
}
