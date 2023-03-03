package com.badskater0729.worldwidechat.util;

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
		return o.getInputLang().compareTo(inputLang) +
				o.getOutputLang().compareTo(outputLang) +
				o.getInputPhrase().compareTo(inputPhrase);
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
		// TODO: String references may change depending on how Java handles strings
		//CommonRefs.debugMsg(inputLang);
		return Objects.hash(inputLang, outputLang, inputPhrase);
	}
}
