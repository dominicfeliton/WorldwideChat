package com.expl0itz.worldwidechat.amazontranslate;

public class AmazonTranslateSupportedLanguageObject {

	private String langCode = "";
    private String langName = "";
    
    public AmazonTranslateSupportedLanguageObject(String langCode, String langName) {
        this.langCode = langCode;
        this.langName = langName;
    }
	
	/* Getters */
    public String getLangCode() {
        return langCode;
    }
    
    public String getLangName() {
        return langName;
    }
    
    /* Setters */
    public void setLangCode(String i) {
        langCode = i;
    }
    
    public void setLangName(String i) {
        langName = i;
    }
	
}
