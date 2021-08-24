package com.expl0itz.worldwidechat.translators;

public class TranslatorTimeoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1275860918183707388L;

	public TranslatorTimeoutException(String errorMessage, Throwable err) {
		super(errorMessage, err);
	}

}
