package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class OpenAITranslation extends BasicTranslation {

    private final String apiKey;
    private final String serviceUrl;

    private YamlConfiguration conf = main.getConfigManager().getMainConfig();
    private YamlConfiguration aiConf = main.getConfigManager().getAIConfig();

    public OpenAITranslation(String apiKey, String url, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        this.apiKey = apiKey;
        this.serviceUrl = url;
    }

    @Override
    protected BasicTranslation.translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new openaiTask(textToTranslate, inputLang, outputLang);
    }

    private class openaiTask extends translationTask {
        public openaiTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            // Init vars
            if (isInitializing) {
                /* Get languages */
                Map<String, SupportedLang> supportedLangs = new HashMap<>();

                // https://help.openai.com/en/articles/8357869-how-to-change-your-language-setting-in-chatgpt
                Set<String> langs = new HashSet<>(aiConf.getStringList("supportedLangs"));
                for (String key : langs) {
                    supportedLangs.put(key, new SupportedLang(key));
                }

                /* Parse languages */
                main.setInputLangs(refs.fixLangNames(supportedLangs, false, false));
                main.setOutputLangs(refs.fixLangNames(supportedLangs, false, false));

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
            // If we do not know the input lang, ChatGPT should guess.

            // Create the request object
            TranslationChatCompletionRequest.Message systemMsg = new TranslationChatCompletionRequest.Message("system", main.getAISystemPrompt());
            TranslationChatCompletionRequest.Message userMsg = new TranslationChatCompletionRequest.Message(
                    "user",
                    "Input Lang: \"" + inputLang + "\"" +
                            ". Output Lang: \"" + outputLang + "\"" +
                            ". Text to translate: \"" + textToTranslate + "\"");

            TranslationChatCompletionRequest.BooleanProperty successProperty = new TranslationChatCompletionRequest.BooleanProperty("boolean");
            TranslationChatCompletionRequest.StringProperty translationProperty = new TranslationChatCompletionRequest.StringProperty("string");
            TranslationChatCompletionRequest.StringProperty reasonProperty = new TranslationChatCompletionRequest.StringProperty("string");

            TranslationChatCompletionRequest.Properties properties = new TranslationChatCompletionRequest.Properties(successProperty, translationProperty, reasonProperty);

            TranslationChatCompletionRequest.JsonSchema jsonSchema = new TranslationChatCompletionRequest.JsonSchema(
                    "translation",
                    TranslationChatCompletionRequest.Schema.createDefaultSchema(),  // Using the default schema
                    true
            );

            TranslationChatCompletionRequest.ResponseFormat responseFormat = new TranslationChatCompletionRequest.ResponseFormat("json_schema", jsonSchema);

            TranslationChatCompletionRequest request = new TranslationChatCompletionRequest(
                    conf.getString("Translator.chatGPTModel"),
                    List.of(systemMsg, userMsg),
                    new TranslationChatCompletionRequest.ResponseFormat("json_schema", jsonSchema)
            );
            ;

            // Send the request
            String jsonResponse = sendChatRequest(request, textToTranslate);

            // Parse the response
            ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(jsonResponse);

            // TODO: Properly handle response
            if (response != null && response.isSuccess()) {
                refs.debugMsg("Success!");
                return response.getTranslation();
            }
            return textToTranslate;
        }

        public String sendChatRequest(TranslationChatCompletionRequest request, String textToTranslate) throws Exception {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonRequest = gson.toJson(request);

            URL url = new URL(serviceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    refs.debugMsg("OpenAI RESPONSE: " + response.toString());
                    refs.debugMsg("Prooompt: " + main.getAISystemPrompt());
                    return response.toString();
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
            return "";
        }
    }

}

class TranslationChatCompletionRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("response_format")
    private ResponseFormat responseFormat;

    public TranslationChatCompletionRequest(String model, List<Message> messages, ResponseFormat responseFormat) {
        this.model = model;
        this.messages = messages;
        this.responseFormat = responseFormat;
    }

    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ResponseFormat {
        @SerializedName("type")
        private String type;

        @SerializedName("json_schema")
        private JsonSchema jsonSchema;

        public ResponseFormat(String type, JsonSchema jsonSchema) {
            this.type = type;
            this.jsonSchema = jsonSchema;
        }
    }

    public static class JsonSchema {
        @SerializedName("name")
        private String name;

        @SerializedName("schema")
        private Schema schema;

        @SerializedName("strict")
        private boolean strict;

        public JsonSchema(String name, Schema schema, boolean strict) {
            this.name = name;
            this.schema = schema;
            this.strict = strict;
        }
    }

    public static class Schema {
        @SerializedName("type")
        private String type;

        @SerializedName("properties")
        private Properties properties;

        @SerializedName("required")
        private List<String> required;

        @SerializedName("additionalProperties")
        private boolean additionalProperties;

        public Schema(String type, Properties properties, List<String> required, boolean additionalProperties) {
            this.type = type;
            this.properties = properties;
            this.required = required;
            this.additionalProperties = additionalProperties;
        }

        public static Schema createDefaultSchema() {
            return new Schema(
                    "object",
                    new Properties(
                            new BooleanProperty("boolean"),
                            new StringProperty("string"),
                            new StringProperty("string")
                    ),
                    List.of("success", "translation", "reason"),
                    false
            );
        }
    }

    public static class Properties {
        @SerializedName("success")
        private BooleanProperty success;

        @SerializedName("translation")
        private StringProperty translation;

        @SerializedName("reason")
        private StringProperty reason;

        public Properties(BooleanProperty success, StringProperty translation, StringProperty reason) {
            this.success = success;
            this.translation = translation;
            this.reason = reason;
        }
    }

    public static class BooleanProperty {
        @SerializedName("type")
        private String type;

        public BooleanProperty(String type) {
            this.type = type;
        }
    }

    public static class StringProperty {
        @SerializedName("type")
        private String type;

        public StringProperty(String type) {
            this.type = type;
        }
    }
}

class ChatResponseParser {

    public static class ChatResponse {
        private boolean success;
        private String translation;
        private String reason;

        public ChatResponse(boolean success, String translation, String reason) {
            this.success = success;
            this.translation = translation;
            this.reason = reason;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTranslation() {
            return translation;
        }

        public String getReason() {
            return reason;
        }
    }

    private static class OuterResponse {
        private List<Choice> choices;

        private static class Choice {
            private Message message;
        }

        private static class Message {
            private String content;
        }
    }

    public static ChatResponse parseResponse(String jsonResponse) {
        Gson gson = new Gson();

        // Parse the outer response to get the content field
        OuterResponse outerResponse = gson.fromJson(jsonResponse, OuterResponse.class);
        if (outerResponse != null && outerResponse.choices != null && !outerResponse.choices.isEmpty()) {
            String content = outerResponse.choices.get(0).message.content;

            // Now parse the content JSON string to get the success, translation, and reason fields
            ChatResponse chatResponse = gson.fromJson(content, ChatResponse.class);
            return chatResponse;
        }

        return null;
    }
}