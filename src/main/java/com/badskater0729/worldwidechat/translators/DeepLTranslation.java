package com.badskater0729.worldwidechat.translators;

public class DeepLTranslation extends BasicTranslation {

	public DeepLTranslation(String textToTranslate, String inputLang, String outputLang) {
		super(textToTranslate, inputLang, outputLang);
	}
	
	public DeepLTranslation(String apikey, String serviceUrl, boolean isInitializing) {
		super(isInitializing);
		System.setProperty("DEEPL_API_KEY", apikey);
		System.setProperty("DEEPL_SERVICE_URL", serviceUrl);
	}

}
