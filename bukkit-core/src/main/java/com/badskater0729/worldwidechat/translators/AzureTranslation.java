package com.badskater0729.worldwidechat.translators;


import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.SupportedLang;
import io.github.brenoepics.at4j.AzureApi;
import io.github.brenoepics.at4j.AzureApiBuilder;
import io.github.brenoepics.at4j.azure.lang.Language;
import io.github.brenoepics.at4j.data.Translation;
import io.github.brenoepics.at4j.data.request.AvailableLanguagesParams;
import io.github.brenoepics.at4j.data.request.DetectLanguageParams;
import io.github.brenoepics.at4j.data.request.TranslateParams;
import io.github.brenoepics.at4j.data.response.DetectResponse;
import io.github.brenoepics.at4j.data.response.TranslationResponse;

import java.util.*;
import java.util.concurrent.*;

public class AzureTranslation extends BasicTranslation {

    public AzureTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
        super(textToTranslate, inputLang, outputLang, callbackExecutor);
    }

    public AzureTranslation(String key, String region, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        System.setProperty("AZURE_KEY", key);
        System.setProperty("AZURE_REGION", region);
    }

    @Override
    public String useTranslator() throws TimeoutException, ExecutionException, InterruptedException {
        Future<String> process = callbackExecutor.submit(new AzureTranslation.translationTask());
        String finalOut = "";

        /* Get test translation */
        finalOut = process.get(WorldwideChat.translatorConnectionTimeoutSeconds, TimeUnit.SECONDS);

        return finalOut;
    }

    private class translationTask implements Callable<String> {

        CommonRefs refs = main.getServerFactory().getCommonRefs();
        @Override
        public String call() throws Exception {
            /* Initialize AWS Creds + Translation Object */
            AzureApi translate = new AzureApiBuilder().setKey(System.getProperty("AZURE_KEY")).region(System.getProperty("AZURE_REGION")).build();
            if (isInitializing) {
                /* Get supported languages from AWS and set them */
                Collection<Language> azLangs = translate.getAvailableLanguages(new AvailableLanguagesParams()).get().get();

                /* Convert supportedLangs to our own SupportedLang objs */
                List<SupportedLang> supportedLangs = new ArrayList<SupportedLang>();
                for (Language eaLang : azLangs) {
                    supportedLangs.add(new SupportedLang(eaLang.getCode(), eaLang.getName(), eaLang.getNativeName()));
                }

                /* Set supported translator langs */
                main.setInputLangs(supportedLangs);
                main.setOutputLangs(supportedLangs);

                /* Setup test translation */
                textToTranslate = "Hi, how are you?";
                inputLang = "en";
                outputLang = "es";
            }

            /* Get language code of current input/output language.
             * APIs generally recognize language codes (en, es, etc.)
             * instead of full names (English, Spanish) */
            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedTranslatorLang(inputLang, "in").getLangCode();
                }
                outputLang = refs.getSupportedTranslatorLang(outputLang, "out").getLangCode();
            }

            // Detect if None
            // TODO: Add Source language is not valid error to errors to ignore??
            if (inputLang.equals("None")) {
                CompletableFuture<Optional<DetectResponse>> detect = translate.detectLanguage(new DetectLanguageParams(textToTranslate));
                inputLang = detect.get().get().getDetectedLanguages().get(0).getLanguageCode();
            }

            /* Actual translation */
            TranslateParams params = new TranslateParams(textToTranslate, Collections.singleton(outputLang)).setSourceLanguage(inputLang);
            Optional<TranslationResponse> result = translate.translate(params).join();

            /* Process + Return final result */
            Collection<Translation> coll = result.get().getResultList().get(0).getTranslations(); // who utilizes the batch feature like this??
            return coll.iterator().next().getText();
        }
    }
}

