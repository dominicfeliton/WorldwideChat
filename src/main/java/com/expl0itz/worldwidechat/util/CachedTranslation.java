package com.expl0itz.worldwidechat.util;

import java.util.Objects;

public class CachedTranslation implements Comparable<CachedTranslation> {

	private String inputLang;
	private String outputLang;
	private String inputPhrase;
	
	public CachedTranslation(String inputLang, String outputLang, String inputPhrase) {
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.inputPhrase = inputPhrase;
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

	@Override
	public int compareTo(CachedTranslation o) {
		int compareResult = inputLang.compareToIgnoreCase(o.getInputLang()) + outputLang.compareToIgnoreCase(o.getOutputLang()) + inputPhrase.compareToIgnoreCase(o.getInputPhrase());
		return compareResult;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CachedTranslation test = (CachedTranslation) obj;
		if (test.compareTo(this) == 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(inputLang, outputLang, inputPhrase);
	}
}
