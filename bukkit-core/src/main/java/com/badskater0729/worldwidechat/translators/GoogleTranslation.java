package com.badskater0729.worldwidechat.translators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.apache.commons.lang3.StringUtils;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

import com.google.cloud.translate.TranslateOptions;

public class GoogleTranslation extends BasicTranslation {

	public GoogleTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public GoogleTranslation(String apikey, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("GOOGLE_API_KEY", apikey); // we do this because .setApi() spams console :(
	}

	@Override
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(new translationTask());
		String finalOut = "";
		
		/* Get translation */
		finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
		
		return finalOut;
	}

	private class translationTask implements Callable<String> {
		CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
			Translate translate = TranslateOptions.getDefaultInstance().getService();

			if (isInitializing) {
				/* Get languages */
				List<Language> allLanguages = translate.listSupportedLanguages();

				/* Parse languages */
				Map<String, SupportedLang> outLangMap = new HashMap<>();
				Map<String, SupportedLang> inLangMap = new HashMap<>();
				
				for (Language eaLang : allLanguages) {
					// Remove spaces from language name
					SupportedLang currLang = new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName()), "");
					outLangMap.put(currLang.getLangCode(), currLang);
					outLangMap.put(currLang.getLangName(), currLang);
					inLangMap.put(currLang.getLangCode(), currLang);
					inLangMap.put(currLang.getLangName(), currLang);
				}

				/* Set languages list */
				main.setOutputLangs(refs.fixLangNames(outLangMap, true, false));
				main.setInputLangs(refs.fixLangNames(inLangMap, true, false));

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
					inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
				}
				outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
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