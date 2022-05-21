package com.expl0itz.worldwidechat.translators;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.expl0itz.worldwidechat.WorldwideChat;

public class BasicTranslation {

	//TODO: Make translators download their languages from out GitHub repo, and not their websites
	//In case the websites change, we won't break
	
	public WorldwideChat main = WorldwideChat.instance;
	
	public String textToTranslate;
	public String inputLang;
	public String outputLang;
	
	public boolean isInitializing;
	
	public BasicTranslation(String textToTranslate, String inputLang, String outputLang) {
		isInitializing = false;
		this.textToTranslate = textToTranslate;
		this.inputLang = inputLang;
		this.outputLang = outputLang;
	}
	
	public BasicTranslation(boolean isInitializing) {
		this.isInitializing = isInitializing;
	}
	
	public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
		return textToTranslate;
	}
	
}
