package com.expl0itz.worldwidechat.translators;

public class TranslatorFailException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8706450917510967995L;

	public TranslatorFailException(Throwable err) {
		super("An error occurred while processing a translation.", err);
	}
}
