package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.google.gson.Gson;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIResponseParserTest {

    private static final Gson GSON = new Gson();

    @Test
    void parsesSuccessfulTranslationContent() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(
                responseWithContent("{\"success\":true,\"translation\":\"Hola\",\"reason\":\"none\"}"));

        assertTrue(response.isSuccess());
        assertEquals("Hola", response.getTranslation());
        assertEquals("none", response.getReason());
    }

    @Test
    void parsesSuccessfulResponsesTranslationContent() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponsesResponse(
                responsesWithOutputText("{\"success\":true,\"translation\":\"Hola\",\"reason\":\"none\"}"));

        assertTrue(response.isSuccess());
        assertEquals("Hola", response.getTranslation());
        assertEquals("none", response.getReason());
    }

    @Test
    void parsesStructuredGuidelineFailure() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(
                responseWithContent("{\"success\":false,\"translation\":\"none\",\"reason\":\"Guidelines\"}"));

        assertFalse(response.isSuccess());
        assertTrue(response.isGuidelinesFailure());
        assertEquals("Guidelines", response.getReason());
    }

    @Test
    void treatsExplicitRefusalAsGuidelineFailure() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(
                responseWithRefusal("I cannot help with that."));

        assertFalse(response.isSuccess());
        assertTrue(response.isGuidelinesFailure());
    }

    @Test
    void treatsResponsesRefusalAsGuidelineFailure() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponsesResponse(
                responsesWithRefusal("I cannot help with that."));

        assertFalse(response.isSuccess());
        assertTrue(response.isGuidelinesFailure());
    }

    @Test
    void treatsMalformedTranslationContentAsGeneralFailure() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(
                responseWithContent("not json"));

        assertFalse(response.isSuccess());
        assertEquals("General", response.getReason());
    }

    @Test
    void treatsMissingTranslationContentAsGeneralFailure() {
        ChatResponseParser.ChatResponse response = ChatResponseParser.parseResponse(
                "{\"choices\":[{\"message\":{}}]}");

        assertFalse(response.isSuccess());
        assertEquals("General", response.getReason());
    }

    @Test
    void parsesTranslatableGuidelinesAIResponse() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponse(
                responseWithContent("{\"translatable\":true}"));

        assertTrue(response.isTranslatable());
        assertEquals("none", response.getReason());
    }

    @Test
    void parsesResponsesTranslatableGuidelinesAIResponse() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponsesResponse(
                responsesWithOutputText("{\"translatable\":true}"));

        assertTrue(response.isTranslatable());
        assertEquals("none", response.getReason());
    }

    @Test
    void parsesRawOllamaTranslatableGuidelinesAIResponse() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseRawResponse(
                "{\"translatable\":true}");

        assertTrue(response.isTranslatable());
        assertEquals("none", response.getReason());
    }

    @Test
    void parsesGuidelinesAIViolation() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponse(
                responseWithContent("{\"translatable\":false}"));

        assertFalse(response.isTranslatable());
        assertEquals("Guidelines", response.getReason());
    }

    @Test
    void parsesRawOllamaGuidelinesAIViolation() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseRawResponse(
                "{\"translatable\":false}");

        assertFalse(response.isTranslatable());
        assertEquals("Guidelines", response.getReason());
    }

    @Test
    void treatsGuidelinesAIRefusalAsViolation() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponse(
                responseWithRefusal("I cannot help with that."));

        assertFalse(response.isTranslatable());
        assertEquals("Guidelines", response.getReason());
    }

    @Test
    void treatsMalformedGuidelinesAIResponseAsFailure() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponse(
                responseWithContent("not json"));

        assertFalse(response.isTranslatable());
        assertEquals("General", response.getReason());
    }

    @Test
    void treatsMalformedOrEmptyRawOllamaGuidelinesAIResponseAsFailure() {
        GuidelinesAIResponseParser.GuidelinesAIResponse malformedResponse =
                GuidelinesAIResponseParser.parseRawResponse("not json");
        GuidelinesAIResponseParser.GuidelinesAIResponse emptyResponse =
                GuidelinesAIResponseParser.parseRawResponse("");

        assertAll(
                () -> assertFalse(malformedResponse.isTranslatable()),
                () -> assertEquals("General", malformedResponse.getReason()),
                () -> assertFalse(emptyResponse.isTranslatable()),
                () -> assertEquals("General", emptyResponse.getReason())
        );
    }

    @Test
    void ollamaGuidelinesResponseAllowsTranslatableRawJson() {
        assertDoesNotThrow(() -> OllamaTranslation.validateGuidelinesResponse("{\"translatable\":true}"));
    }

    @Test
    void ollamaGuidelinesResponseBlocksAsGuidelines() {
        TranslationFailureException exception = assertThrows(
                TranslationFailureException.class,
                () -> OllamaTranslation.validateGuidelinesResponse("{\"translatable\":false}"));

        assertEquals("Guidelines", exception.getReason());
        assertTrue(exception.shouldNotifyPlayer());
    }

    @Test
    void ollamaGuidelinesResponseFailsGeneralForMalformedOrEmptyJson() {
        TranslationFailureException malformedException = assertThrows(
                TranslationFailureException.class,
                () -> OllamaTranslation.validateGuidelinesResponse("not json"));
        TranslationFailureException emptyException = assertThrows(
                TranslationFailureException.class,
                () -> OllamaTranslation.validateGuidelinesResponse(""));

        assertAll(
                () -> assertEquals("General", malformedException.getReason()),
                () -> assertTrue(malformedException.shouldNotifyPlayer()),
                () -> assertEquals("General", emptyException.getReason()),
                () -> assertTrue(emptyException.shouldNotifyPlayer())
        );
    }

    @Test
    void usesDefaultModelWhenDedicatedGuidelinesModelIsBlank() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.chatGPTModel", "main-model");
        conf.set("Translator.guidelinesAIModel", "");

        assertEquals("main-model", CommonRefs.resolveGuidelinesAIModel(conf, "main-model"));
    }

    @Test
    void usesDedicatedGuidelinesModelWhenConfigured() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.chatGPTModel", "main-model");
        conf.set("Translator.guidelinesAIModel", "guidelines-model");

        assertEquals("guidelines-model", CommonRefs.resolveGuidelinesAIModel(conf, "main-model"));
    }

    @Test
    void usesActiveProviderModelWhenGuidelinesModelIsBlank() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.guidelinesAIModel", "");

        assertAll(
                () -> assertEquals("chatgpt-model", CommonRefs.resolveGuidelinesAIModel(conf, "chatgpt-model")),
                () -> assertEquals("compatible-model", CommonRefs.resolveGuidelinesAIModel(conf, "compatible-model")),
                () -> assertEquals("ollama-model", CommonRefs.resolveGuidelinesAIModel(conf, "ollama-model"))
        );
    }

    @Test
    void keepsOpenAIDefaultGuidelinesModelForOllamaWhenExplicitlyConfigured() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.guidelinesAIModel", "gpt-5.4-mini");

        assertEquals("gpt-5.4-mini", CommonRefs.resolveGuidelinesAIModel(conf, "ollama-model"));
    }

    @Test
    void keepsExplicitGuidelinesModelForAnyActiveProvider() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.guidelinesAIModel", "guidelines-model");

        assertAll(
                () -> assertEquals("guidelines-model", CommonRefs.resolveGuidelinesAIModel(conf, "chatgpt-model")),
                () -> assertEquals("guidelines-model", CommonRefs.resolveGuidelinesAIModel(conf, "compatible-model")),
                () -> assertEquals("guidelines-model", CommonRefs.resolveGuidelinesAIModel(conf, "ollama-model"))
        );
    }

    private static String responseWithContent(String content) {
        return "{\"choices\":[{\"message\":{\"content\":" + GSON.toJson(content) + "}}]}";
    }

    private static String responseWithRefusal(String refusal) {
        return "{\"choices\":[{\"message\":{\"content\":null,\"refusal\":" + GSON.toJson(refusal) + "}}]}";
    }

    private static String responsesWithOutputText(String text) {
        return "{\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":"
                + GSON.toJson(text) + "}]}]}";
    }

    private static String responsesWithRefusal(String refusal) {
        return "{\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"refusal\",\"refusal\":"
                + GSON.toJson(refusal) + "}]}]}";
    }
}
