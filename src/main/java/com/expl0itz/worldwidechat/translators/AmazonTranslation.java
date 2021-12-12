package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.command.CommandSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;

public class AmazonTranslation {
	private String textToTranslate = "";
	private String inputLang = "";
	private String outputLang = "";

	private CommandSender sender;

	private boolean isInitializing = false;

	private WorldwideChat main = WorldwideChat.instance;

	public AmazonTranslation(String textToTranslate, String inputLang, String outputLang, CommandSender sender) {
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.sender = sender;
	}

	public AmazonTranslation(String accessKeyId, String secretKeyId, String region) {
		System.setProperty("AMAZON_KEY_ID", accessKeyId);
		System.setProperty("AMAZON_SECRET_KEY", secretKeyId);
		System.setProperty("AMAZON_REGION", region);
		isInitializing = true;
	}

	public String useTranslator() throws TranslatorTimeoutException, TranslatorFailException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		try {
			/* Get test translation */
			finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			CommonDefinitions.sendDebugMessage("Amazon Translate Timeout!!");
			process.cancel(true);
			if (e instanceof ExecutionException) {
			    throw new TranslatorFailException(e);	
			}
			throw new TranslatorTimeoutException("Timed out while waiting for Amazon Translate response.", e);
		} finally {
			executor.shutdownNow();
		}

		/* Return final result */
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
				Document doc = Jsoup
						.connect("https://docs.aws.amazon.com/translate/latest/dg/what-is.html#what-is-languages")
						.get();
				Elements tr = doc.select("tr");
				for (int i = 1; i < tr.size(); i++) {
					Elements td = tr.get(i).select("td");
					if (td.size() > 0) {
						// langCode, langName == AmazonLangObj constructor
						// HTML page starts with langName, then langCode
						SupportedLanguageObject newObj = new SupportedLanguageObject(td.get(1).html(), td.get(0).html(),
								"", true, true);
						supportedLangs.add(newObj);
					}
				}
				main.setSupportedTranslatorLanguages(supportedLangs);

				/* Setup test translation */
				textToTranslate = "Hi, how are you?";
				inputLang = "en";
				outputLang = "es";
			}

			/* Convert input + output lang to lang code because this API is funky, man */
			if (!(inputLang.equals("None"))
					&& !CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode().equals(inputLang)) {
				inputLang = CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode();
			}
			if (!CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode().equals(outputLang)) {
				outputLang = CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode();
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
