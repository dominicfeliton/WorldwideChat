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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLanguageObject;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.getSupportedTranslatorLang;

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
		@Override
		public String call() throws Exception {
			/* Initialize AWS Creds + Translation Object */
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(System.getProperty("AMAZON_KEY_ID"),
					System.getProperty("AMAZON_SECRET_KEY"));
			AmazonTranslate translate = AmazonTranslateClient.builder()
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
					.withRegion(System.getProperty("AMAZON_REGION")).build();

			if (isInitializing) {
				/* Get supported languages from AWS docs and set them */
				List<SupportedLanguageObject> supportedLangs = new ArrayList<SupportedLanguageObject>();
				//WARNING:
				//IF YOU GET A MARSHALL RESPONSE ERROR, CHECK THIS LINK AND MAKE SURE IT IS NOT BROKEN!
				Document doc = Jsoup
						.connect("https://docs.aws.amazon.com/translate/latest/dg/what-is-languages.html")
						.get();
				Elements tr = doc.select("tr");
				for (int i = 1; i < tr.size(); i++) {
					Elements td = tr.get(i).select("td");
					if (td.size() > 0) {
						// langCode, langName == AmazonLangObj constructor
						// HTML page starts with langName, then langCode
						// Remove lang name spaces
						SupportedLanguageObject newObj = new SupportedLanguageObject(td.get(1).html(), StringUtils.deleteWhitespace(td.get(0).html()),
								"", true, true);
						supportedLangs.add(newObj);
					}
				}
				main.setSupportedTranslatorLanguages(supportedLangs);
				if (supportedLangs.size() == 0) {
					main.getLogger().warning(getMsg("wwcBackupLangCodesWarning"));
					debugMsg("---> Using backup codes!!! Fix this!!! <---");
					setBackupCodes();
				}

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
					inputLang = getSupportedTranslatorLang(inputLang).getLangCode();
				}
				outputLang = getSupportedTranslatorLang(outputLang).getLangCode();
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
