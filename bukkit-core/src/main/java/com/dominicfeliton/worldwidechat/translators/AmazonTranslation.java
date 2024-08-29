package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.Language;
import software.amazon.awssdk.services.translate.model.ListLanguagesRequest;
import software.amazon.awssdk.services.translate.model.ListLanguagesResponse;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AmazonTranslation extends BasicTranslation {

	public AmazonTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
		super(textToTranslate, inputLang, outputLang, callbackExecutor);
	}

	public AmazonTranslation(String accessKeyId, String secretKeyId, String region, boolean isInitializing, ExecutorService callbackExecutor) {
		super(isInitializing, callbackExecutor);
		System.setProperty("AMAZON_KEY_ID", accessKeyId);
		System.setProperty("AMAZON_SECRET_KEY", secretKeyId);
		System.setProperty("AMAZON_REGION", region);
	}

    @Override
    protected translationTask createTranslationTask() {
        return new amazonTask();
    }

	private class amazonTask extends translationTask {

		CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String call() throws Exception {
			/* Initialize AWS Creds + Translation Object */
			// Create AWS credentials
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
					System.getProperty("AMAZON_KEY_ID"),
					System.getProperty("AMAZON_SECRET_KEY")
			);

            TranslateTextResponse result;
            try (TranslateClient translate = TranslateClient.builder()
                    .region(Region.of(System.getProperty("AMAZON_REGION")))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .httpClientBuilder(UrlConnectionHttpClient.builder())
                    .build()) {

                if (isInitializing) {
					// Get supported langs
                    ListLanguagesRequest langRequest = ListLanguagesRequest.builder().build();
                    ListLanguagesResponse langResponse = translate.listLanguages(langRequest);
                    List<Language> awsLangs = langResponse.languages();
                    refs.debugMsg(awsLangs.size() + "");

                    /* Convert supportedLangs to our own SupportedLang objs */
                    Map<String, SupportedLang> supportedLangs = new HashMap<>();
                    for (Language eaLang : awsLangs) {
						// Don't add auto
                        if (eaLang.languageCode().equals("auto") || eaLang.languageName().equals("auto")) {
                            continue;
                        }

						// Add all entries
                        SupportedLang langObj = new SupportedLang(eaLang.languageCode(), eaLang.languageName());
                        supportedLangs.put(eaLang.languageCode(), langObj);
                        supportedLangs.put(eaLang.languageName(), langObj);
                    }

                    /* Set supported translator langs */
                    main.setInputLangs(refs.fixLangNames(supportedLangs, true, false));
                    main.setOutputLangs(refs.fixLangNames(supportedLangs, true, false));

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
                        inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
                    }
                    outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
                }

                // Create the translation request using the builder pattern
                TranslateTextRequest request = TranslateTextRequest.builder()
                        .text(textToTranslate)
                        .sourceLanguageCode(inputLang.equals("None") ? "auto" : inputLang)
                        .targetLanguageCode(outputLang)
                        .build();

                // Execute the translation
                result = translate.translateText(request);
            }
            return result.translatedText();
		}
	}
}
