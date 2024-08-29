package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;

import java.util.concurrent.*;

public abstract class BasicTranslation {

	protected final WorldwideChat main = WorldwideChat.instance;
	protected final ExecutorService callbackExecutor;
	protected final boolean isInitializing;

	public BasicTranslation(boolean isInitializing, ExecutorService callbackExecutor) {
		this.callbackExecutor = callbackExecutor;
		this.isInitializing = isInitializing;
	}

	public String useTranslator(String textToTranslate, String inputLang, String outputLang) throws TimeoutException, ExecutionException, InterruptedException {
		Future<String> process = callbackExecutor.submit(createTranslationTask(textToTranslate, inputLang, outputLang));
		return process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);
	}

	protected abstract translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang);

	public abstract class translationTask implements Callable<String> {
		protected String textToTranslate;
		protected String inputLang;
		protected String outputLang;

		public translationTask(String textToTranslate, String inputLang, String outputLang) {
			this.textToTranslate = textToTranslate;
			this.inputLang = inputLang;
			this.outputLang = outputLang;
		}

		@Override
		public abstract String call() throws Exception;
	}
}