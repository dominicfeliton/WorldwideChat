package com.expl0itz.worldwidechat.translators;

import java.util.ArrayList;
import java.util.List;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;

public class TestTranslation extends BasicTranslation {

	private String textToTranslate = "";
	private String inputLang = "";
	private String outputLang = "";

	private boolean isInitializing = false;

	private WorldwideChat main = WorldwideChat.instance;

	public TestTranslation(String textToTranslate, String inputLang, String outputLang) {
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
	}

	public TestTranslation(String apikey, boolean isInitializing) {
		System.setProperty("FAKE_API_KEY", apikey);
		this.isInitializing = isInitializing;
	}

	public String useTranslator() {
		if (isInitializing) {
			/* Generate fake supported langs list */
			List<SupportedLanguageObject> outList = new ArrayList<SupportedLanguageObject>();
			outList.add(new SupportedLanguageObject("en", "English", "", true, true));
			outList.add(new SupportedLanguageObject("es", "Spanish", "", true, true));
			outList.add(new SupportedLanguageObject("fr", "French", "", true, true));

			/* Set langList in Main */
			main.setSupportedTranslatorLanguages(outList);

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
