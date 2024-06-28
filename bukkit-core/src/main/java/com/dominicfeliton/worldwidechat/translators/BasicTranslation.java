package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class BasicTranslation {
	
	public WorldwideChat main = WorldwideChat.instance;
	
	public String textToTranslate;
	public String inputLang;
	public String outputLang;

	public ExecutorService callbackExecutor;
	public boolean isInitializing;
	
	public BasicTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		isInitializing = false;
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
		this.callbackExecutor = callbackExecutor;
	}

	public BasicTranslation(boolean isInitializing, ExecutorService callbackExecutor) {
		this.callbackExecutor = callbackExecutor;
		this.isInitializing = isInitializing;
	}
	
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		return textToTranslate;
	}
}
