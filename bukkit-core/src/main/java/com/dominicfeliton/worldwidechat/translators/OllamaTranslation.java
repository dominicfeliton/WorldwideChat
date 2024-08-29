package com.dominicfeliton.worldwidechat.translators;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

public class OllamaTranslation extends BasicTranslation {
    public OllamaTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
        super(textToTranslate, inputLang, outputLang, callbackExecutor);
    }

    public OllamaTranslation(String url, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        System.setProperty("OLLAMA_URL", url);
    }

    @Override
    protected translationTask createTranslationTask() {
        return new ollamaTask();
    }

    private class ollamaTask extends translationTask {
        @Override
        public String call() throws Exception {
            return "";
        }
    }


}
