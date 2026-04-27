package com.dominicfeliton.worldwidechat.translators;

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
    void parsesGuidelinesAIViolation() {
        GuidelinesAIResponseParser.GuidelinesAIResponse response = GuidelinesAIResponseParser.parseResponse(
                responseWithContent("{\"translatable\":false}"));

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
    void usesDefaultModelWhenDedicatedGuidelinesModelIsBlank() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.chatGPTModel", "main-model");
        conf.set("Translator.guidelinesAIModel", "");

        assertEquals("main-model", OpenAITranslation.getGuidelinesAIModel(conf, "main-model"));
    }

    @Test
    void usesDedicatedGuidelinesModelWhenConfigured() {
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("Translator.chatGPTModel", "main-model");
        conf.set("Translator.guidelinesAIModel", "guidelines-model");

        assertEquals("guidelines-model", OpenAITranslation.getGuidelinesAIModel(conf, "main-model"));
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
