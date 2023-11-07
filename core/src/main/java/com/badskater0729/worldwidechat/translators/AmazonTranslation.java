package com.badskater0729.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.Language;
import com.amazonaws.services.translate.model.ListLanguagesRequest;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.SupportedLang;

public class AmazonTranslation extends BasicTranslation {

	public AmazonTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}

	public AmazonTranslation(String accessKeyId, String secretKeyId, String region, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("AMAZON_KEY_ID", accessKeyId);
		System.setProperty("AMAZON_SECRET_KEY", secretKeyId);
		System.setProperty("AMAZON_REGION", region);
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		
		/* Get test translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		process.cancel(true);
		executor.shutdownNow();
		
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
				List<SupportedLang> supportedLangs = new ArrayList<SupportedLang>();
				for (Language eaLang : awsLangs) {
					// Don't add auto
					if (eaLang.getLanguageCode().equals("auto") || eaLang.getLanguageName().equals("auto")) {
						continue;
					}
					supportedLangs.add(new SupportedLang(eaLang.getLanguageCode(), eaLang.getLanguageName()));
				}

				/* Set supported translator langs */
				main.setInputLangs(supportedLangs);
				main.setOutputLangs(supportedLangs);

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
					inputLang = refs.getSupportedTranslatorLang(inputLang, "in").getLangCode();
				}
				outputLang = refs.getSupportedTranslatorLang(outputLang, "out").getLangCode();
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
