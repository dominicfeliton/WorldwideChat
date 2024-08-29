package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class TestTranslation extends BasicTranslation {

	CommonRefs refs = main.getServerFactory().getCommonRefs();

	public TestTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public TestTranslation(String apikey, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("FAKE_API_KEY", apikey);
	}

	@Override
	protected translationTask createTranslationTask() {
		return new testTask();
	}

	private class testTask extends translationTask {
		@Override
		public String call() throws Exception {
			if (isInitializing) {
				/* Generate fake supported langs list */
				Map<String, SupportedLang> outMap = new HashMap<>();
				SupportedLang en = new SupportedLang("en", "English", "");
				SupportedLang es = new SupportedLang("es", "Spanish", "");
				SupportedLang fr = new SupportedLang("fr", "French", "");

				outMap.put(en.getLangCode(), en);
				outMap.put(en.getLangName(), en);

				outMap.put(es.getLangCode(), es);
				outMap.put(es.getLangName(), es);

				outMap.put(fr.getLangCode(), fr);
				outMap.put(fr.getLangName(), fr);

				/* Set langList in Main */
				main.setOutputLangs(refs.fixLangNames(outMap, true, false));
				main.setInputLangs(refs.fixLangNames(outMap, true, false));

				/* Test translation setup */
				outputLang = "es";
				textToTranslate = "How many diamonds do you have?";
			}
			/* Test cases */
			if (outputLang.equals("es") && textToTranslate.equals("How many diamonds do you have?")) {
				return "Cuantos diamantes tienes?";
			}
			if (inputLang.equals("en") && outputLang.equals("es") && textToTranslate.equals("Hello, how are you?")) {
				return "Hola, como estas?";
			}
			return "Invalid test case!";
		}
	}
}
