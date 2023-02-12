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

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLanguageObject;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

import com.google.cloud.translate.TranslateOptions;

import static com.badskater0729.worldwidechat.util.CommonRefs.getSupportedTranslatorLang;

public class GoogleTranslation extends BasicTranslation {

	public GoogleTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}

	public GoogleTranslation(String apikey, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("GOOGLE_API_KEY", apikey); // we do this because .setApi() spams console :(
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> process = executor.submit(new translationTask());
		String finalOut = "";
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		process.cancel(true);
		executor.shutdownNow();
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
			Translate translate = TranslateOptions.getDefaultInstance().getService();

			if (isInitializing) {
				/* Get languages */
				List<Language> allLanguages = translate.listSupportedLanguages();

				/* Parse languages */
				List<SupportedLanguageObject> outList = new ArrayList<SupportedLanguageObject>();
				for (Language eaLang : allLanguages) {
					outList.add(new SupportedLanguageObject(eaLang.getCode(), eaLang.getName(), "", true, true));
				}

				/* Set languages list */
				main.setSupportedTranslatorLanguages(outList);

				/* Setup test translation */
				inputLang = "en";
				outputLang = "es";
				textToTranslate = "How are you?";
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

			/* Detect inputLang */
			if (inputLang.equals("None")) { // if we do not know the input
				Detection detection = translate.detect(textToTranslate);
				inputLang = detection.getLanguage();
			}

			/* Actual translation */
			Translation translation = translate.translate(textToTranslate, TranslateOption.sourceLanguage(inputLang),
					TranslateOption.targetLanguage(outputLang), TranslateOption.format("text"));
			return translation.getTranslatedText();
		}
	}

}