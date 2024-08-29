package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class SystranTranslation extends BasicTranslation {

    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private final String apiKey;

    public SystranTranslation(String apiKey, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        this.apiKey = apiKey;
    }

    @Override
    protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new systranTask(textToTranslate, inputLang, outputLang);
    }

    private class systranTask extends translationTask {

        public systranTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            // Init vars
            Gson gson = new Gson();

            if (isInitializing) {
                /* Get supported languages */
                URL url = new URL("https://api-translate.systran.net/translation/supportedLanguages");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Key " + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
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

                        // Setup test translation
                        inputLang = "en";
                        outputLang = "es";
                        textToTranslate = "How are you?";
                    }
                } else {
                    // Capture the error response
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        String errorLine;
                        StringBuilder errorResponse = new StringBuilder();

                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }

                        checkError(responseCode, errorResponse.toString());
                    } catch (IOException e) {
                        refs.debugMsg("Failed to read the error stream");
                        checkError(responseCode, "");
                    }
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
            conn.setRequestProperty("Authorization", "Key " + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "source=" + (inputLang.equals("None") ? "auto" : inputLang) + "&target=" + outputLang +
                    "&input=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params);
            writer.flush();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                    JsonArray outputs = jsonResponse.getAsJsonArray("outputs");

                    if (!outputs.isEmpty()) {
                        return outputs.get(0).getAsJsonObject().get("output").getAsString();
                    } else {
                        return textToTranslate;
                    }
                }
            } else {
                // Capture the error response
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    String errorLine;
                    StringBuilder errorResponse = new StringBuilder();

                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }

                    checkError(responseCode, errorResponse.toString());
                } catch (IOException e) {
                    refs.debugMsg("Failed to read the error stream");
                    checkError(responseCode, "");
                }
            }

            return textToTranslate;
        }
    }

    private void checkError(int in, String msg) throws Exception {
        refs.debugMsg(msg);
        switch (in) {
            case 400:
            case 403:
            case 429:
            case 500:
                throw new Exception(refs.getPlainMsg("chatGPT500"));
            default:
                throw new Exception(refs.getPlainMsg("chatGPTUnknown", in + ""));
        }
    }
}