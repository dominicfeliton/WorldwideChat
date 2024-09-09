package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.SupportedLang;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class AmazonTranslation extends BasicTranslation {

    private TranslateClient translate;

    public AmazonTranslation(String accessKeyId, String secretKeyId, String region, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        this.translate = TranslateClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKeyId)))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Override
    protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new amazonTask(textToTranslate, inputLang, outputLang);
    }

    private class amazonTask extends translationTask {

        public amazonTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            if (isInitializing) {
                ListLanguagesRequest langRequest = ListLanguagesRequest.builder().build();
                ListLanguagesResponse langResponse = translate.listLanguages(langRequest);
                List<Language> awsLangs = langResponse.languages();
                refs.debugMsg(awsLangs.size() + "");

                Map<String, SupportedLang> supportedLangs = new HashMap<>();
                for (Language eaLang : awsLangs) {
                    if (eaLang.languageCode().equals("auto")) continue;
                    SupportedLang langObj = new SupportedLang(eaLang.languageCode(), eaLang.languageName());
                    supportedLangs.put(eaLang.languageCode(), langObj);
                    supportedLangs.put(eaLang.languageName(), langObj);
                }

                main.setInputLangs(refs.fixLangNames(supportedLangs, true, false));
                main.setOutputLangs(refs.fixLangNames(supportedLangs, true, false));

                textToTranslate = "Hi, how are you?";
                inputLang = "en";
                outputLang = "es";
            }

            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
            }

            TranslateTextRequest request = TranslateTextRequest.builder()
                    .text(textToTranslate)
                    .sourceLanguageCode(inputLang.equals("None") ? "auto" : inputLang)
                    .targetLanguageCode(outputLang)
                    .build();

            TranslateTextResponse result = translate.translateText(request);
            return result.translatedText();
        }
    }
}