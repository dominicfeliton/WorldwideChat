package com.expl0itz.worldwidechat.util;

public class CachedTranslation {

	private String inputLang;
	private String outputLang;
	private String inputPhrase;
	private String outputPhrase;

	public CachedTranslation(String inputLang, String outputLang, String inputPhrase, String outputPhrase) {
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.inputPhrase = inputPhrase;
		this.outputPhrase = outputPhrase;
	}

	/* Getters */
	public String getInputLang() {
		return inputLang;
	}

	public String getOutputLang() {
		return outputLang;
	}

	public String getInputPhrase() {
		return inputPhrase;
	}

	public String getOutputPhrase() {
		return outputPhrase;
	}

	/* Setters */
	public void setInputLang(String i) {
		inputLang = i;
	}

	public void setOutputLang(String i) {
		outputLang = i;
	}

	public void setInputPhrase(String i) {
		inputPhrase = i;
	}

	public void setOutputPhrase(String i) {
		outputPhrase = i;
	}
}
