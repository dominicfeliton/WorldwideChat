package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class LibreTranslation extends BasicTranslation {

    private final String serviceUrl;
    private final String apiKey;

    public LibreTranslation(String apiKey, String serviceUrl, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        if (apiKey == null || apiKey.equalsIgnoreCase("none")) {
            this.apiKey = "";
        } else {
            this.apiKey = apiKey;
        }
        this.serviceUrl = serviceUrl;
    }

    @Override
    protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new libreTask(textToTranslate, inputLang, outputLang);
    }

    private class libreTask extends translationTask {

        public libreTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            // Init vars
            Gson gson = new Gson();

            if (isInitializing) {
                /* Get languages */
                URL url = new URL(serviceUrl + "/languages");

                // Open a connection, no need to check the protocol
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                int listResponseCode = conn.getResponseCode();

                Map<String, SupportedLang> outLangMap = new HashMap<>();
                Map<String, SupportedLang> inLangMap = new HashMap<>();

                if (listResponseCode == HttpURLConnection.HTTP_OK) {
                    // Scan response
                    StringBuilder inLine = new StringBuilder();
                    Scanner scanner = new Scanner(url.openStream());

                    while (scanner.hasNext()) {
                        inLine.append(scanner.nextLine());
                    }

                    scanner.close();

                    // Get lang code/name, remove spaces from name
                    JsonElement jsonTree = JsonParser.parseString(inLine.toString());
                    for (JsonElement element : jsonTree.getAsJsonArray()) {
                        JsonObject eaProperty = (JsonObject) element;
                        SupportedLang currLang = new SupportedLang(
                                eaProperty.get("code").getAsString(),
                                StringUtils.deleteWhitespace(eaProperty.get("name").getAsString()));
                        // we put names/codes because native terms are taken care of
                        outLangMap.put(currLang.getLangCode(), currLang);
                        outLangMap.put(currLang.getLangName(), currLang);

                        inLangMap.put(currLang.getLangCode(), currLang);
                        inLangMap.put(currLang.getLangName(), currLang);
                    }
                } else {
                    // Capture the error response
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        String errorLine;
                        StringBuilder errorResponse = new StringBuilder();

                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }

                        checkError(listResponseCode, errorResponse.toString());
                    } catch (IOException e) {
                        refs.debugMsg("Failed to read the error stream");
                        checkError(listResponseCode, "");
                    }
                }

                /* Parse languages */
                main.setOutputLangs(refs.fixLangNames(outLangMap, true, false));
                main.setInputLangs(refs.fixLangNames(inLangMap, true, false));

                /* Setup test translation */
                inputLang = "en";
                outputLang = "es";
                textToTranslate = "How are you?";
            }

            /* Get language code of current input/output language.
             * APIs generally recognize language codes (en, es, etc.)
             * instead of full names (English, Spanish) */
            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedLang(inputLang, CommonRefs.LangType.INPUT).getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, CommonRefs.LangType.OUTPUT).getLangCode();
            }

            /* Detect inputLang */
            if (inputLang.equals("None")) { // if we do not know the input
                /* Craft detection request */
                URL url = new URL(serviceUrl + "/detect");
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("POST");

                httpConn.setRequestProperty("accept", "application/json");
                httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                httpConn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

                String formatted = "q=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8);
                if (apiKey != null) {
                    formatted += "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
                }
                writer.write(formatted);
                writer.flush();
                writer.close();
                httpConn.getOutputStream().close();

                /* Process response */
                int statusCode = httpConn.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        DetectResponse[] outArray = gson.fromJson(response.toString(), DetectResponse[].class);
                        inputLang = outArray[0].getLanguage();
                    }
                } else {
                    // Capture the error response
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()))) {
                        String errorLine;
                        StringBuilder errorResponse = new StringBuilder();

                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }

                        checkError(statusCode, errorResponse.toString());
                    } catch (IOException e) {
                        refs.debugMsg("Failed to read the error stream");
                        checkError(statusCode, "");
                    }
                }
            }

            /* Actual translation */
            URL url = new URL(serviceUrl + "/translate");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("accept", "application/json");
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            httpConn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

            String formatted = "q=" + URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8) +
                    "&source=" + URLEncoder.encode(inputLang, StandardCharsets.UTF_8) +
                    "&target=" + URLEncoder.encode(outputLang, StandardCharsets.UTF_8) +
                    "&format=text";
            if (apiKey != null) {
                formatted += "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            }
            writer.write(formatted);
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

            /* Checking response */
            int statusCode = httpConn.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    return gson.fromJson(response.toString(), TranslateResponse.class).getTranslatedText();
                }
            } else {
                // Capture the error response
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()))) {
                    String errorLine;
                    StringBuilder errorResponse = new StringBuilder();

                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }

                    checkError(statusCode, errorResponse.toString());
                } catch (IOException e) {
                    refs.debugMsg("Failed to read the error stream");
                    checkError(statusCode, "");
                }
            }
            return textToTranslate;
        }
    }
}

class TranslateResponse {
    String translatedText;

    public String getTranslatedText() {
        return translatedText;
    }
}

class DetectResponse {
    private String language;

    private double confidenceLevel;

    public String getLanguage() {
        return language;
    }

    public Double getConfidenceLevel() {
        return confidenceLevel;
    }
}