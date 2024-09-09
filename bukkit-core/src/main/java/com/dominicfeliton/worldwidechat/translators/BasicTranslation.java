package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;

import java.util.concurrent.*;

public abstract class BasicTranslation {

    protected final WorldwideChat main = WorldwideChat.instance;
    protected final CommonRefs refs = main.getServerFactory().getCommonRefs();
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

    public void checkError(int statusCode, String msg) throws Exception {
        refs.debugMsg(msg);
        switch (statusCode) {
            case 400:
                throw new Exception(refs.getPlainMsg("translatorBadRequest")); // Bad Request
            case 401:
                throw new Exception(refs.getPlainMsg("translatorBadAPIKey")); // Unauthorized
            case 403:
                throw new Exception(refs.getPlainMsg("translatorForbidden")); // Forbidden
            case 404:
                throw new Exception(refs.getPlainMsg("translatorNotFound")); // Not Found
            case 405:
                throw new Exception(refs.getPlainMsg("translatorMethodNotAllowed")); // Method Not Allowed
            case 429:
                throw new Exception(refs.getPlainMsg("translatorLimitExceeded")); // Too Many Requests
            case 500:
                throw new Exception(refs.getPlainMsg("translatorServerError")); // Internal Server Error
            default:
                throw new Exception(refs.getPlainMsg("translatorUnknownError", statusCode + "")); // Unknown error
        }
    }
}