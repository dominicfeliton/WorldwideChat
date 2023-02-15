package com.badskater0729.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;

import com.badskater0729.worldwidechat.util.SupportedLang;

public class TestTranslation extends BasicTranslation {

	public TestTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}

	public TestTranslation(String apikey, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("FAKE_API_KEY", apikey);
	}

	@Override
	public String useTranslator() {
		if (isInitializing) {
			/* Generate fake supported langs list */
			List<SupportedLang> outList = new ArrayList<SupportedLang>();
			outList.add(new SupportedLang("en", "English", ""));
			outList.add(new SupportedLang("es", "Spanish", ""));
			outList.add(new SupportedLang("fr", "French", ""));

			/* Set langList in Main */
			main.setOutputLangs(outList);
			main.setInputLangs(outList);

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
