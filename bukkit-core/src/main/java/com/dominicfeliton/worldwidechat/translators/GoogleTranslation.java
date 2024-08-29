package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.cloud.translate.*;
import com.google.cloud.translate.Translate.TranslateOption;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GoogleTranslation extends BasicTranslation {

	private Translate translate;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	public GoogleTranslation(String apiKey, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("GOOGLE_API_KEY", apiKey); // we do this because .setApi() spams console :(
		translate = TranslateOptions.getDefaultInstance().getService();
	}

	@Override
	protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
		return new googleTask(textToTranslate, inputLang, outputLang);
	}

	private class googleTask extends translationTask {
		public googleTask(String textToTranslate, String inputLang, String outputLang) {
			super(textToTranslate, inputLang, outputLang);
		}

		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
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