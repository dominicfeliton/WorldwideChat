package com.badskater0729.worldwidechat.util;

import java.util.Objects;

public class CachedTranslation implements Comparable<CachedTranslation> {

	private String inputLang;
	private String outputLang;
	private String inputPhrase;
	private boolean hasBeenSaved;
	
	public CachedTranslation(String inputLang, String outputLang, String inputPhrase) {
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.inputPhrase = inputPhrase;
		hasBeenSaved = false;
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

	public boolean hasBeenSaved() { return hasBeenSaved; }

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

	public void setHasBeenSaved(boolean b) { hasBeenSaved = b; }

	@Override
	public int compareTo(CachedTranslation o) {
		int inputLangComparison = inputLang.compareTo(o.getInputLang());
		if (inputLangComparison != 0) {
			return inputLangComparison;
		}
		int outputLangComparison = outputLang.compareTo(o.getOutputLang());
		if (outputLangComparison != 0) {
			return outputLangComparison;
		}
		return inputPhrase.compareTo(o.getInputPhrase());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CachedTranslation other = (CachedTranslation) obj;
		return Objects.equals(inputLang, other.inputLang) &&
				Objects.equals(outputLang, other.outputLang) &&
				Objects.equals(inputPhrase, other.inputPhrase);
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputLang, outputLang, inputPhrase);
	}
}
