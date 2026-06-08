package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class OpenAITranslation extends BasicTranslation {

    private final String apiKey;
    private final String serviceUrl;
    private final String model;
    private final String systemPrompt;

    private final YamlConfiguration aiConf = main.getConfigManager().getAIConfig();

    public OpenAITranslation(String apiKey, String url, String model, String systemPrompt,
                             boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        this.apiKey = apiKey;
        this.serviceUrl = url;
        this.model = model;
        this.systemPrompt = systemPrompt;
    }

    @Override
    protected BasicTranslation.translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new OpenAITask(textToTranslate, inputLang, outputLang);
    }

    public void checkGuidelines(String exactMessage) throws Exception {
        Object request = createStructuredRequest(
                model,
                systemPrompt,
                formatUserQueryBlock(exactMessage),
                "translation_guideline_check",
                TranslationChatCompletionRequest.Schema.createGuidelineSchema()
        );

        String jsonResponse = sendOpenAIRequest(request, systemPrompt);
        GuidelinesAIResponseParser.GuidelinesAIResponse response = usesResponsesAPI() ?
                GuidelinesAIResponseParser.parseResponsesResponse(jsonResponse) :
                GuidelinesAIResponseParser.parseResponse(jsonResponse);
        if (!response.isTranslatable()) {
            throw new TranslationFailureException(
                    response.getReason(),
                    "Guidelines AI check blocked translation: " + response.getReason(),
                    true
            );
        }
    }

    private class OpenAITask extends translationTask {
        public OpenAITask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            if (isInitializing) {
                Map<String, SupportedLang> supportedLangs = new HashMap<>();

                // https://help.openai.com/en/articles/8357869-how-to-change-your-language-setting-in-chatgpt
                Set<String> langs = new HashSet<>(aiConf.getStringList("supportedLangs"));
                for (String key : langs) {
                    supportedLangs.put(key, new SupportedLang(key));
                }

                main.setInputLangs(refs.fixLangNames(supportedLangs, false, false));
                main.setOutputLangs(refs.fixLangNames(supportedLangs, false, false));

                inputLang = "en";
                outputLang = "es";
                textToTranslate = "How are you?";
            }

            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedLang(inputLang, CommonRefs.LangType.INPUT).getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, CommonRefs.LangType.OUTPUT).getLangCode();
            }

            Object request = createStructuredRequest(
                    model,
                    systemPrompt,
                    formatTranslationInput(inputLang, outputLang, textToTranslate),
                    "translation",
                    TranslationChatCompletionRequest.Schema.createTranslationSchema()
            );

            String jsonResponse = sendOpenAIRequest(request, systemPrompt);
            ChatResponseParser.ChatResponse response = usesResponsesAPI() ?
                    ChatResponseParser.parseResponsesResponse(jsonResponse) :
                    ChatResponseParser.parseResponse(jsonResponse);

            if (response.isSuccess()) {
                refs.debugMsg("OpenAI translation success.");
                String translation = response.getTranslation();
                if (containsGuidelinesBlockedSentinel(translation)) {
                    throw new TranslationFailureException("Guidelines", "OpenAI returned a blocked sentinel.", true);
                }
                if (translation == null || translation.isBlank() || translation.equalsIgnoreCase("none")) {
                    throw new TranslationFailureException("General", "OpenAI returned an empty translation.", false);
                }
                return translation;
            }

            boolean guidelinesFailure = response.isGuidelinesFailure()
                    || containsGuidelinesBlockedSentinel(response.getTranslation());
            throw new TranslationFailureException(
                    guidelinesFailure ? "Guidelines" : response.getReason(),
                    "OpenAI translation failed: " + response.getReason(),
                    guidelinesFailure
            );
        }
    }

    private Object createStructuredRequest(String model, String instructions, String input,
                                           String schemaName, TranslationChatCompletionRequest.Schema schema) {
        if (usesResponsesAPI()) {
            return new TranslationResponsesRequest(
                    model,
                    instructions,
                    input,
                    new TranslationResponsesRequest.TextConfig(
                            new TranslationResponsesRequest.JsonSchemaFormat(schemaName, schema, true)
                    )
            );
        }

        TranslationChatCompletionRequest.Message systemMsg = new TranslationChatCompletionRequest.Message("system", instructions);
        TranslationChatCompletionRequest.Message userMsg = new TranslationChatCompletionRequest.Message("user", input);
        TranslationChatCompletionRequest.JsonSchema jsonSchema = new TranslationChatCompletionRequest.JsonSchema(
                schemaName,
                schema,
                true
        );
        return new TranslationChatCompletionRequest(
                model,
                List.of(systemMsg, userMsg),
                new TranslationChatCompletionRequest.ResponseFormat("json_schema", jsonSchema)
        );
    }

    private boolean usesResponsesAPI() {
        if (serviceUrl == null || serviceUrl.isBlank()) {
            return false;
        }

        try {
            String path = URI.create(serviceUrl).normalize().getPath();
            return path != null && (path.equals("/v1/responses") || path.endsWith("/responses"));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String sendOpenAIRequest(Object request, String promptForDebug) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonRequest = gson.toJson(request);

        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(WorldwideChat.translatorConnectionTimeoutSeconds * 1000);
        conn.setReadTimeout(WorldwideChat.translatorConnectionTimeoutSeconds * 1000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = readResponse(in);
                refs.debugMsg("OpenAI RESPONSE: " + response);
                refs.debugMsg("Prompt: " + promptForDebug);
                return response.toString();
            }
        }

        if (conn.getErrorStream() == null) {
            checkError(responseCode, "");
        }

        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            checkError(responseCode, readResponse(errorReader).toString());
        } catch (IOException e) {
            refs.debugMsg("Failed to read the error stream");
            checkError(responseCode, "");
        }
        return "";
    }

    private StringBuilder readResponse(BufferedReader reader) throws IOException {
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        return response;
    }
}

class TranslationChatCompletionRequest {

    @SerializedName("model")
    private final String model;

    @SerializedName("messages")
    private final List<Message> messages;

    @SerializedName("response_format")
    private final ResponseFormat responseFormat;

    public TranslationChatCompletionRequest(String model, List<Message> messages, ResponseFormat responseFormat) {
        this.model = model;
        this.messages = messages;
        this.responseFormat = responseFormat;
    }

    public static class Message {
        @SerializedName("role")
        private final String role;

        @SerializedName("content")
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ResponseFormat {
        @SerializedName("type")
        private final String type;

        @SerializedName("json_schema")
        private final JsonSchema jsonSchema;

        public ResponseFormat(String type, JsonSchema jsonSchema) {
            this.type = type;
            this.jsonSchema = jsonSchema;
        }
    }

    public static class JsonSchema {
        @SerializedName("name")
        private final String name;

        @SerializedName("schema")
        private final Schema schema;

        @SerializedName("strict")
        private final boolean strict;

        public JsonSchema(String name, Schema schema, boolean strict) {
            this.name = name;
            this.schema = schema;
            this.strict = strict;
        }
    }

    public static class Schema {
        @SerializedName("type")
        private final String type;

        @SerializedName("properties")
        private final Object properties;

        @SerializedName("required")
        private final List<String> required;

        @SerializedName("additionalProperties")
        private final boolean additionalProperties;

        public Schema(String type, Object properties, List<String> required, boolean additionalProperties) {
            this.type = type;
            this.properties = properties;
            this.required = required;
            this.additionalProperties = additionalProperties;
        }

        public static Schema createTranslationSchema() {
            return new Schema(
                    "object",
                    new TranslationProperties(
                            new BooleanProperty("boolean"),
                            new StringProperty("string"),
                            new StringProperty("string")
                    ),
                    List.of("success", "translation", "reason"),
                    false
            );
        }

        public static Schema createGuidelineSchema() {
            return new Schema(
                    "object",
                    new GuidelineProperties(
                            new BooleanProperty("boolean")
                    ),
                    List.of("translatable"),
                    false
            );
        }
    }

    public static class TranslationProperties {
        @SerializedName("success")
        private final BooleanProperty success;

        @SerializedName("translation")
        private final StringProperty translation;

        @SerializedName("reason")
        private final StringProperty reason;

        public TranslationProperties(BooleanProperty success, StringProperty translation, StringProperty reason) {
            this.success = success;
            this.translation = translation;
            this.reason = reason;
        }
    }

    public static class GuidelineProperties {
        @SerializedName("translatable")
        private final BooleanProperty translatable;

        public GuidelineProperties(BooleanProperty translatable) {
            this.translatable = translatable;
        }
    }

    public static class BooleanProperty {
        @SerializedName("type")
        private final String type;

        public BooleanProperty(String type) {
            this.type = type;
        }
    }

    public static class StringProperty {
        @SerializedName("type")
        private final String type;

        public StringProperty(String type) {
            this.type = type;
        }
    }
}

class TranslationResponsesRequest {

    @SerializedName("model")
    private final String model;

    @SerializedName("instructions")
    private final String instructions;

    @SerializedName("input")
    private final String input;

    @SerializedName("text")
    private final TextConfig text;

    public TranslationResponsesRequest(String model, String instructions, String input, TextConfig text) {
        this.model = model;
        this.instructions = instructions;
        this.input = input;
        this.text = text;
    }

    public static class TextConfig {
        @SerializedName("format")
        private final JsonSchemaFormat format;

        public TextConfig(JsonSchemaFormat format) {
            this.format = format;
        }
    }

    public static class JsonSchemaFormat {
        @SerializedName("type")
        private final String type = "json_schema";

        @SerializedName("name")
        private final String name;

        @SerializedName("schema")
        private final TranslationChatCompletionRequest.Schema schema;

        @SerializedName("strict")
        private final boolean strict;

        public JsonSchemaFormat(String name, TranslationChatCompletionRequest.Schema schema, boolean strict) {
            this.name = name;
            this.schema = schema;
            this.strict = strict;
        }
    }
}

class ResponsesAPIContentParser {

    public static class ResponseContent {
        private final String text;
        private final boolean refusal;

        public ResponseContent(String text, boolean refusal) {
            this.text = text;
            this.refusal = refusal;
        }

        public String getText() {
            return text;
        }

        public boolean isRefusal() {
            return refusal;
        }
    }

    private static class OuterResponse {
        @SerializedName("output_text")
        private String outputText;

        private List<OutputItem> output;
    }

    private static class OutputItem {
        private List<ContentItem> content;
    }

    private static class ContentItem {
        private String type;
        private String text;
        private String refusal;
    }

    public static ResponseContent parse(String jsonResponse) {
        Gson gson = new Gson();
        try {
            OuterResponse outerResponse = gson.fromJson(jsonResponse, OuterResponse.class);
            if (outerResponse == null) {
                return new ResponseContent(null, false);
            }
            if (outerResponse.outputText != null && !outerResponse.outputText.isBlank()) {
                return new ResponseContent(outerResponse.outputText, false);
            }
            if (outerResponse.output == null) {
                return new ResponseContent(null, false);
            }

            for (OutputItem outputItem : outerResponse.output) {
                if (outputItem == null || outputItem.content == null) {
                    continue;
                }
                for (ContentItem contentItem : outputItem.content) {
                    if (contentItem == null) {
                        continue;
                    }
                    if (contentItem.refusal != null && !contentItem.refusal.isBlank()) {
                        return new ResponseContent(null, true);
                    }
                    if ("refusal".equals(contentItem.type)) {
                        return new ResponseContent(null, true);
                    }
                    if (contentItem.text != null && !contentItem.text.isBlank()) {
                        return new ResponseContent(contentItem.text, false);
                    }
                }
            }
            return new ResponseContent(null, false);
        } catch (JsonSyntaxException | IllegalStateException e) {
            return new ResponseContent(null, false);
        }
    }
}

class ChatResponseParser {

    public static class ChatResponse {
        private final boolean success;
        private final String translation;
        private final String reason;

        public ChatResponse(boolean success, String translation, String reason) {
            this.success = success;
            this.translation = translation;
            this.reason = normalizeReasonForSuccess(success, reason);
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

        public boolean isGuidelinesFailure() {
            return "Guidelines".equals(reason);
        }
    }

    private static class InnerTranslationResponse {
        private Boolean success;
        private String translation;
        private String reason;
    }

    private static class OuterResponse {
        private List<Choice> choices;

        private static class Choice {
            private Message message;
        }

        private static class Message {
            private String content;
            private String refusal;
        }
    }

    public static ChatResponse parseResponse(String jsonResponse) {
        Gson gson = new Gson();
        try {
            OuterResponse outerResponse = gson.fromJson(jsonResponse, OuterResponse.class);
            OuterResponse.Message message = getFirstMessage(outerResponse);
            if (message == null) {
                return failure("General");
            }
            if (message.refusal != null && !message.refusal.isBlank()) {
                return failure("Guidelines");
            }
            if (message.content == null || message.content.isBlank()) {
                return failure("General");
            }

            return parseContent(message.content);
        } catch (JsonSyntaxException | IllegalStateException e) {
            return failure("General");
        }
    }

    public static ChatResponse parseResponsesResponse(String jsonResponse) {
        ResponsesAPIContentParser.ResponseContent responseContent = ResponsesAPIContentParser.parse(jsonResponse);
        if (responseContent.isRefusal()) {
            return failure("Guidelines");
        }
        if (responseContent.getText() == null || responseContent.getText().isBlank()) {
            return failure("General");
        }
        return parseContent(responseContent.getText());
    }

    private static ChatResponse parseContent(String content) {
        Gson gson = new Gson();
        try {
            InnerTranslationResponse chatResponse = gson.fromJson(content, InnerTranslationResponse.class);
            if (chatResponse == null || chatResponse.success == null) {
                return failure("General");
            }
            return new ChatResponse(chatResponse.success, chatResponse.translation, chatResponse.reason);
        } catch (JsonSyntaxException | IllegalStateException e) {
            return failure("General");
        }
    }

    private static OuterResponse.Message getFirstMessage(OuterResponse outerResponse) {
        if (outerResponse == null || outerResponse.choices == null || outerResponse.choices.isEmpty()) {
            return null;
        }
        OuterResponse.Choice choice = outerResponse.choices.get(0);
        if (choice == null) {
            return null;
        }
        return choice.message;
    }

    private static ChatResponse failure(String reason) {
        return new ChatResponse(false, "none", reason);
    }

    private static String normalizeReasonForSuccess(boolean success, String reason) {
        String normalized = TranslationFailureException.normalizeReason(reason);
        if (success) {
            return "none";
        }
        if ("none".equals(normalized)) {
            return "General";
        }
        return normalized;
    }
}

class GuidelinesAIResponseParser {

    public static class GuidelinesAIResponse {
        private final boolean translatable;
        private final String reason;

        public GuidelinesAIResponse(boolean translatable, String reason) {
            this.translatable = translatable;
            this.reason = normalizeReasonForTranslatable(translatable, reason);
        }

        public boolean isTranslatable() {
            return translatable;
        }

        public String getReason() {
            return reason;
        }
    }

    private static class InnerGuidelineResponse {
        private Boolean translatable;
        private String reason;
    }

    private static class OuterResponse {
        private List<Choice> choices;

        private static class Choice {
            private Message message;
        }

        private static class Message {
            private String content;
            private String refusal;
        }
    }

    public static GuidelinesAIResponse parseResponse(String jsonResponse) {
        Gson gson = new Gson();
        try {
            OuterResponse outerResponse = gson.fromJson(jsonResponse, OuterResponse.class);
            OuterResponse.Message message = getFirstMessage(outerResponse);
            if (message == null) {
                return failure("General");
            }
            if (message.refusal != null && !message.refusal.isBlank()) {
                return failure("Guidelines");
            }
            if (message.content == null || message.content.isBlank()) {
                return failure("General");
            }

            return parseContent(message.content);
        } catch (JsonSyntaxException | IllegalStateException e) {
            return failure("General");
        }
    }

    public static GuidelinesAIResponse parseResponsesResponse(String jsonResponse) {
        ResponsesAPIContentParser.ResponseContent responseContent = ResponsesAPIContentParser.parse(jsonResponse);
        if (responseContent.isRefusal()) {
            return failure("Guidelines");
        }
        if (responseContent.getText() == null || responseContent.getText().isBlank()) {
            return failure("General");
        }
        return parseContent(responseContent.getText());
    }

    public static GuidelinesAIResponse parseRawResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return failure("General");
        }
        return parseContent(jsonResponse);
    }

    private static GuidelinesAIResponse parseContent(String content) {
        Gson gson = new Gson();
        try {
            InnerGuidelineResponse guidelineResponse = gson.fromJson(content, InnerGuidelineResponse.class);
            if (guidelineResponse == null || guidelineResponse.translatable == null) {
                return failure("General");
            }
            if (!guidelineResponse.translatable && (guidelineResponse.reason == null || guidelineResponse.reason.isBlank())) {
                return new GuidelinesAIResponse(false, "Guidelines");
            }
            return new GuidelinesAIResponse(guidelineResponse.translatable, guidelineResponse.reason);
        } catch (JsonSyntaxException | IllegalStateException e) {
            return failure("General");
        }
    }

    private static OuterResponse.Message getFirstMessage(OuterResponse outerResponse) {
        if (outerResponse == null || outerResponse.choices == null || outerResponse.choices.isEmpty()) {
            return null;
        }
        OuterResponse.Choice choice = outerResponse.choices.get(0);
        if (choice == null) {
            return null;
        }
        return choice.message;
    }

    private static GuidelinesAIResponse failure(String reason) {
        return new GuidelinesAIResponse(false, reason);
    }

    private static String normalizeReasonForTranslatable(boolean translatable, String reason) {
        String normalized = TranslationFailureException.normalizeReason(reason);
        if (translatable) {
            return "none";
        }
        if ("none".equals(normalized)) {
            return "General";
        }
        return normalized;
    }
}
