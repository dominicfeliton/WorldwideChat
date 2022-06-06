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

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

import com.google.cloud.translate.TranslateOptions;

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
			/* Convert input + output lang to lang code because this API is funky, man */
			if (!isInitializing && !(inputLang.equals("None"))
					&& !CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode().equals(inputLang)) {
				inputLang = CommonDefinitions.getSupportedTranslatorLang(inputLang).getLangCode();
			}
			if (!isInitializing && !CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode().equals(outputLang)) {
				outputLang = CommonDefinitions.getSupportedTranslatorLang(outputLang).getLangCode();
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