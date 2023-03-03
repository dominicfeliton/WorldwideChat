package com.badskater0729.worldwidechat.util;

public class SupportedLang {
	private String langCode = "";
	private String langName = "";
	private String nativeLangName = "";

	public SupportedLang(String langCode, String langName, String nativeLangName) {
		this.langCode = langCode;
		this.langName = langName;
		this.nativeLangName = nativeLangName;
	}
	
	public SupportedLang(String langCode, String langName) {
		this.langCode = langCode;
		this.langName = langName;
		this.nativeLangName = "";
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
}
