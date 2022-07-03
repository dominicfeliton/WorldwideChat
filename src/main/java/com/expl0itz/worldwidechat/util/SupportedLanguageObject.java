package com.expl0itz.worldwidechat.util;

public class SupportedLanguageObject {
	private String langCode = "";
	private String langName = "";
	private String nativeLangName = "";
	boolean supportedAsSource = true;
	boolean supportedAsTarget = true;

	public SupportedLanguageObject(String langCode, String langName, String nativeLangName, boolean supportedAsSource,
			boolean supportedAsTarget) {
		this.langCode = langCode;
		this.langName = langName;
		this.nativeLangName = nativeLangName;
		this.supportedAsSource = supportedAsSource;
		this.supportedAsTarget = supportedAsTarget;
	}
	
	public SupportedLanguageObject(String langCode, String langName, String nativeLangName) {
		this.langCode = langCode;
		this.langName = langName;
		this.nativeLangName = nativeLangName;
		this.supportedAsSource = true;
		this.supportedAsTarget = true;
	}
	
	public SupportedLanguageObject(String langCode, String langName) {
		this.langCode = langCode;
		this.langName = langName;
		this.nativeLangName = "";
		this.supportedAsSource = true;
		this.supportedAsTarget = true;
	}

	/* Getters */
	public String getLangCode() {
		return langCode;
	}

	public String getLangName() {
		return langName;
	}

	public String getNativeLangName() {
		return nativeLangName;
	}

	public boolean getSupportedAsSource() {
		return supportedAsSource;
	}

	public boolean getSupportedAsTarget() {
		return supportedAsTarget;
	}

	/* Setters */
	public void setLangCode(String i) {
		langCode = i;
	}

	public void setLangName(String i) {
		langName = i;
	}

	public void setNativeLangName(String i) {
		nativeLangName = i;
	}

	public void setSupportedAsSource(boolean i) {
		supportedAsSource = i;
	}

	public void setSupportedAsTarget(boolean i) {
		supportedAsTarget = i;
	}
}
