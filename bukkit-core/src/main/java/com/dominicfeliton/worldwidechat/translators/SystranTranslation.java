package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class SystranTranslation extends BasicTranslation {

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    public SystranTranslation(String textToTranslate, String inputLang, String outputLang, ExecutorService callbackExecutor) {
        super(textToTranslate, inputLang, outputLang, callbackExecutor);
    }

    public SystranTranslation(String apiKey, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        System.setProperty("SYSTRAN_API_KEY", apiKey);
    }

    @Override
    protected translationTask createTranslationTask() {
        return new systranTask();
    }

    private class systranTask extends translationTask {
        @Override
        public String call() throws Exception {
            // Init vars
            Gson gson = new Gson();

            if (isInitializing) {
                /* Get supported languages */
                URL url = new URL("https://api-translate.systran.net/translation/supportedLanguages");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Key " + System.getProperty("SYSTRAN_API_KEY"));

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream responseStream = conn.getInputStream();
                    String response = new Scanner(responseStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
                    JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                    JsonArray languagePairs = jsonResponse.getAsJsonArray("languagePairs");

                    Map<String, SupportedLang> outLangMap = new HashMap<>();
                    Map<String, SupportedLang> inLangMap = new HashMap<>();
                    for (JsonElement pair : languagePairs) {
                        JsonObject langPair = pair.getAsJsonObject();
                        String sourceCode = langPair.get("source").getAsString();
                        String targetCode = langPair.get("target").getAsString();
                        SupportedLang sourceLang = new SupportedLang(sourceCode, sourceCode);
                        SupportedLang targetLang = new SupportedLang(targetCode, targetCode);

                        inLangMap.put(sourceLang.getLangCode(), sourceLang);
                        outLangMap.put(targetLang.getLangCode(), targetLang);
                    }

                    // Systran does not include native or regular lang names.
                    main.setOutputLangs(refs.fixLangNames(outLangMap, false, false));
                    main.setInputLangs(refs.fixLangNames(inLangMap, false, false));

                    /* Setup test translation */
                    inputLang = "en";
                    outputLang = "es";
                    textToTranslate = "How are you?";
                } else {
                    checkError(responseCode);
                }
            }

            /* Get language code of current input/output language */
            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
            }

            /* Actual translation */
            URL url = new URL("https://api-translate.systran.net/translation/text/translate");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Key " + System.getProperty("SYSTRAN_API_KEY"));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "source=" + (inputLang.equals("None") ? "auto" : inputLang) + "&target=" + outputLang +
                    "&input=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params);
            writer.flush();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream responseStream = conn.getInputStream();
                String response = new Scanner(responseStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                JsonArray outputs = jsonResponse.getAsJsonArray("outputs");
                if (!outputs.isEmpty()) {
                    return outputs.get(0).getAsJsonObject().get("output").getAsString();
                } else {
                    return textToTranslate;
                }
            } else {
                checkError(responseCode);
            }

            return textToTranslate;
        }
    }

    private void checkError(int statusCode) throws Exception {
        switch (statusCode) {
            case 400:
                throw new Exception(refs.getPlainMsg("systranHttp400"));
            case 403:
            case 429:
            case 500:
                throw new Exception(refs.getPlainMsg("systranHttp500", statusCode + ""));
            default:
                throw new Exception(refs.getPlainMsg("systranHttpUnknown", statusCode + ""));
        }
    }
}