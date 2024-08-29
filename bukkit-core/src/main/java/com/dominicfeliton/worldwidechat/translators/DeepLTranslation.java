package com.dominicfeliton.worldwidechat.translators;

import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class DeepLTranslation extends BasicTranslation {

	public DeepLTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}
	
	public DeepLTranslation(String apikey, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("DEEPL_API_KEY", apikey);
	}

	@Override
	protected translationTask createTranslationTask() {
		return new deeplTask();
	}

	private class deeplTask extends translationTask {
		CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String call() throws Exception {
			/* Initialize translation object again */
			Translator translate = new Translator(System.getProperty("DEEPL_API_KEY"));

			if (isInitializing) {
				/* Parse Supported Languages */
				Map<String, SupportedLang> sourceLangs = new HashMap<>();
				for (Language eaLang : translate.getSourceLanguages()) {
					SupportedLang lang = new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName()));
					sourceLangs.put(eaLang.getCode(), lang);
					sourceLangs.put(eaLang.getName(), lang);
				}
				Map<String, SupportedLang> targetLangs = new HashMap<String, SupportedLang>();
				for (Language eaLang : translate.getTargetLanguages()) {
					SupportedLang lang = new SupportedLang(eaLang.getCode(), StringUtils.deleteWhitespace(eaLang.getName()));
					targetLangs.put(lang.getLangCode(), lang);
					targetLangs.put(lang.getLangName(), lang);
				}

				/* Set languages list */
				main.setOutputLangs(refs.fixLangNames(targetLangs, true, false));
				main.setInputLangs(refs.fixLangNames(sourceLangs, true, false));

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

			/* If inputLang set to None, set as null for translateText() */
			if (inputLang.equals("None")) { // if we do not know the input
				inputLang = null;
			}

			/* Actual translation */
			TextResult result = translate.translateText(textToTranslate, inputLang,
					outputLang);
			return result.getText();
		}
	}
	
}
