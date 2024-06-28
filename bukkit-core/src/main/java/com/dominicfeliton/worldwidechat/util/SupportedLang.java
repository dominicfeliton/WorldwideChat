package com.dominicfeliton.worldwidechat.util;

import java.util.Objects;

public class SupportedLang implements Comparable<SupportedLang> {
	private String langCode = "";
	private String langName = "";
	private String nativeLangName = "";

	public SupportedLang(String langCode, String langName, String nativeLangName) {
		this.langCode = langCode;
		setLangName(langName);
		setNativeLangName(nativeLangName);
	}
	
	public SupportedLang(String langCode, String langName) {
		this.langCode = langCode;
		setLangName(langName);
		setNativeLangName("");
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
		// Remove blank space for commands
		langName = i.replaceAll("\\s+", "-");;
	}

	public void setNativeLangName(String i) {
		// Remove blank space for commands
		nativeLangName = i.replaceAll("\\s+", "-");;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SupportedLang other = (SupportedLang) obj;
		return langCode.equals(other.langCode) &&
				langName.equals(other.langName) &&
				nativeLangName.equals(other.nativeLangName);
	}

	@Override
	public int compareTo(SupportedLang other) {
		int langCodeComparison = langCode.compareTo(other.langCode);
		if (langCodeComparison != 0) {
			return langCodeComparison;
		}
		int langNameComparison = langName.compareTo(other.langName);
		if (langNameComparison != 0) {
			return langNameComparison;
		}
		return nativeLangName.compareTo(other.nativeLangName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(langCode, langName, nativeLangName);
	}

	@Override
	public String toString() { return langCode + "|" + langName + "|" + nativeLangName; }
}
