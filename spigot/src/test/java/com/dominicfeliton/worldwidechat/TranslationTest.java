package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.translators.TestTranslation;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationTest extends WWCIntegrationTest {

    @Test
    void startsWithJUnitTranslatorAndSupportedLanguages() {
        assertEquals("JUnit/MockBukkit Testing Translator", plugin().getTranslatorName());

        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        assertTrue(refs.isSupportedLang("en", CommonRefs.LangType.INPUT));
        assertTrue(refs.isSupportedLang("es", CommonRefs.LangType.OUTPUT));
        assertTrue(refs.isSupportedLang("fr", CommonRefs.LangType.OUTPUT));
    }

    @Test
    void translatesKnownSourceTargetPhrase() {
        PlayerMock player = WWCTestSupport.addOpPlayer("Translator");
        player.performCommand("wwct en es");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("Hello, how are you?", player);

        assertEquals("Hola, como estas?", translated);
    }

    @Test
    void translatesKnownAutoTargetPhrase() {
        PlayerMock player = WWCTestSupport.addOpPlayer("AutoTranslator");
        player.performCommand("wwct es");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("How many diamonds do you have?", player);

        assertEquals("Cuantos diamantes tienes?", translated);
    }

    @Test
    void translationWithoutSessionReturnsOriginalText() {
        PlayerMock player = WWCTestSupport.addOpPlayer("NoSession");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("This phrase should not change", player);

        assertEquals("This phrase should not change", translated);
    }

    @Test
    void cachesSuccessfulTranslationsAndReusesResult() {
        PlayerMock player = WWCTestSupport.addOpPlayer("CacheUser");
        player.performCommand("wwct en es");

        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        String first = refs.translateText("Hello, how are you?", player);
        String second = refs.translateText("Hello, how are you?", player);

        assertEquals(first, second);
        assertTrue(plugin().getEstimatedCacheSize() > 0);
        assertEquals("Hola, como estas?",
                plugin().getCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?")));
    }

    @Test
    void objectBatchPreservesOrderAndCapsConcurrentTranslations() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ObjectBatchCap");
        player.performCommand("wwct en es");
        plugin().setObjectTranslationConcurrencyLimit(4);
        TestTranslation.resetConcurrencyTracking();
        TestTranslation.setArtificialDelayMillis(75);

        try {
            List<String> input = List.of(
                    "batch-0", "batch-1", "batch-2", "batch-3",
                    "batch-4", "batch-5", "batch-6", "batch-7");

            List<String> translated = plugin().getServerFactory().getCommonRefs()
                    .translateObjectText(input, player);

            assertEquals(List.of(
                    "translated-batch-0", "translated-batch-1", "translated-batch-2", "translated-batch-3",
                    "translated-batch-4", "translated-batch-5", "translated-batch-6", "translated-batch-7"), translated);
            assertTrue(TestTranslation.getMaxActiveTranslations() > 1);
            assertTrue(TestTranslation.getMaxActiveTranslations() <= 4);
        } finally {
            TestTranslation.resetConcurrencyTracking();
        }
    }

    @Test
    void objectBatchLimitOneKeepsTranslationSerial() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ObjectBatchSerial");
        player.performCommand("wwct en es");
        plugin().setObjectTranslationConcurrencyLimit(1);
        TestTranslation.resetConcurrencyTracking();
        TestTranslation.setArtificialDelayMillis(25);

        try {
            List<String> translated = plugin().getServerFactory().getCommonRefs()
                    .translateObjectText(List.of("batch-serial-0", "batch-serial-1", "batch-serial-2"), player);

            assertEquals(List.of(
                    "translated-batch-serial-0",
                    "translated-batch-serial-1",
                    "translated-batch-serial-2"), translated);
            assertEquals(1, TestTranslation.getMaxActiveTranslations());
        } finally {
            TestTranslation.resetConcurrencyTracking();
        }
    }

    @Test
    void objectTranslationConcurrencyConfigDefaultsAndRejectsInvalidValues() {
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 2);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(2, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 5);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());
    }

    @Test
    void cacheEvictsEntriesOverConfiguredLimit() {
        plugin().getConfigManager().getMainConfig().set("Translator.translatorCacheSize", 2);
        plugin().setCacheProperties(2);

        CachedTranslation first = new CachedTranslation("en", "es", "first");
        CachedTranslation second = new CachedTranslation("en", "es", "second");
        CachedTranslation third = new CachedTranslation("en", "es", "third");

        plugin().addCacheTerm(first, "uno");
        plugin().addCacheTerm(second, "dos");
        plugin().addCacheTerm(third, "tres");

        WWCTestSupport.waitForCondition(() -> plugin().getEstimatedCacheSize() <= 2);

        List<CachedTranslation> keys = List.of(first, second, third);
        long retainedEntries = keys.stream()
                .filter(plugin()::hasCacheTerm)
                .count();

        assertEquals(2, plugin().getEstimatedCacheSize());
        assertEquals(2, retainedEntries);
    }

    @Test
    void guidelinesAICheckUsesMainModelWhenDedicatedModelIsBlank() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":true}")) {
            configureChatGPTGuidelinesChecks(stub, "");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesMainModel");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hola, como estas?", translated);
            assertTrue(stub.guidelinesRequestBody().contains("\"model\": \"main-model\""));
            assertFalse(stub.guidelinesRequestBody().contains("\"reason\""));
            assertEndsWithUserQueryBlock(stub.guidelinesRequestInput(), "Hello, how are you?");
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(1, stub.translationRequestCount());
        }
    }

    @Test
    void guidelinesAICheckUsesConfiguredDedicatedModel() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":true}")) {
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesModel");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hola, como estas?", translated);
            assertTrue(stub.guidelinesRequestBody().contains("\"model\": \"guidelines-model\""));
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(1, stub.translationRequestCount());
        }
    }

    @Test
    void guidelinesCheckUsesOpenAICompatibleActiveProviderModelWhenDedicatedModelIsBlank() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":true}")) {
            configureGuidelinesChecks(stub, "", "OpenAI Compatible");
            PlayerMock player = WWCTestSupport.addOpPlayer("CompatGuidelinesModel");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hola, como estas?", translated);
            assertTrue(stub.guidelinesRequestBody().contains("\"model\": \"compatible-main-model\""));
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(1, stub.translationRequestCount());
        }
    }

    @Test
    void guidelinesAICheckDisabledByDefaultSendsOnlyTranslationRequest() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureOpenAIProviderConfig(stub);
            plugin().setTranslatorName("ChatGPT");
            WWCTestSupport.useDirectPermissionChecks();
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesDisabled");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hola, como estas?", translated);
            assertEquals(0, stub.guidelinesRequestCount());
            assertEquals(1, stub.translationRequestCount());
        }
    }

    @Test
    void chatGPTTranslationUsesChatGPTConfigOnly() throws Exception {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":true}")) {
            configureOpenAIProviderConfig(stub);

            String translated = plugin().getServerFactory().getCommonRefs()
                    .getTranslatorResult("ChatGPT", "Hello, how are you?", "en", "es", false);

            assertEquals("Hola, como estas?", translated);
            assertTrue(stub.translationRequestBody().contains("\"model\": \"main-model\""));
            assertFalse(stub.translationRequestBody().contains("compatible-main-model"));
            assertEndsWithUserQueryBlock(stub.translationRequestInput(), "Hello, how are you?");
        }
    }

    @Test
    void openAICompatibleTranslationUsesCompatibleConfigOnly() throws Exception {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":true}")) {
            configureOpenAIProviderConfig(stub);

            String translated = plugin().getServerFactory().getCommonRefs()
                    .getTranslatorResult("OpenAI Compatible", "Hello, how are you?", "en", "es", false);

            assertEquals("Hola, como estas?", translated);
            assertTrue(stub.translationRequestBody().contains("\"model\": \"compatible-main-model\""));
            assertFalse(stub.translationRequestBody().contains("\"model\": \"main-model\""));
        }
    }

    @Test
    void chatCompletionsEndpointStillParsesChatCompletionResponses() throws Exception {
        try (OpenAIStub stub = OpenAIStub.chatCompletionsSuccess()) {
            configureOpenAIProviderConfig(stub);
            plugin().getConfigManager().getMainConfig().set("Translator.chatGPTURL", stub.url());

            String translated = plugin().getServerFactory().getCommonRefs()
                    .getTranslatorResult("ChatGPT", "Hello, how are you?", "en", "es", false);

            assertEquals("Hola, como estas?", translated);
        }
    }

    @Test
    void guardrailsPlaceholderExpandsIntoDefaultPrompts() {
        plugin().getConfigManager().initAISettings();

        YamlConfiguration aiConfig = plugin().getConfigManager().getAIConfig();
        assertAll(
                () -> assertPromptHasDefaultGuardrails(aiConfig.getString("chatGPTDefaultSystemPrompt")),
                () -> assertPromptHasDefaultGuardrails(aiConfig.getString("openAICompatibleDefaultSystemPrompt")),
                () -> assertPromptHasDefaultGuardrails(aiConfig.getString("guidelinesAIDefaultPrompt")),
                () -> assertPromptHasDefaultGuardrails(aiConfig.getString("ollamaDefaultSystemPrompt")),
                () -> assertPromptHasDefaultGuardrails(plugin().getGuidelinesAIPrompt())
        );
    }

    @Test
    void guardrailsOverrideAffectsDefaultAndActivePrompts() throws IOException {
        YamlConfiguration aiConfig = plugin().getConfigManager().getAIConfig();
        aiConfig.set("guardrailsOverridePrompt", "custom shared guardrails");
        aiConfig.set("chatGPTOverrideSystemPrompt", "{default}");
        aiConfig.set("openAICompatibleOverrideSystemPrompt", "{default}");
        aiConfig.set("guidelinesAIOverridePrompt", "{default}");
        aiConfig.set("ollamaOverrideSystemPrompt", "{default}");
        plugin().getConfigManager().saveCustomConfig(aiConfig, plugin().getConfigManager().getAIFile(), false);
        plugin().getConfigManager().getMainConfig().set("Translator.useChatGPT", true);
        plugin().getConfigManager().getMainConfig().set("Translator.useOpenAICompatible", false);
        plugin().getConfigManager().getMainConfig().set("Translator.useOllama", false);

        plugin().getConfigManager().initAISettings();

        YamlConfiguration resolvedAIConfig = plugin().getConfigManager().getAIConfig();
        assertAll(
                () -> assertTrue(resolvedAIConfig.getString("chatGPTDefaultSystemPrompt", "").contains("custom shared guardrails")),
                () -> assertTrue(resolvedAIConfig.getString("openAICompatibleDefaultSystemPrompt", "").contains("custom shared guardrails")),
                () -> assertTrue(resolvedAIConfig.getString("guidelinesAIDefaultPrompt", "").contains("custom shared guardrails")),
                () -> assertTrue(resolvedAIConfig.getString("ollamaDefaultSystemPrompt", "").contains("custom shared guardrails")),
                () -> assertTrue(plugin().getAISystemPrompt().contains("custom shared guardrails")),
                () -> assertTrue(plugin().getGuidelinesAIPrompt().contains("custom shared guardrails"))
        );
    }

    @Test
    void guidelinesAIViolationBlocksTranslationAndSkipsCache() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesViolation");
            player.performCommand("wwct en es");
            drainPlayerMessages(player);

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hello, how are you?", translated);
            assertNull(plugin().getCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?")));
            assertEquals(0, plugin().getPlayerRecord(player, false).getSuccessfulTranslations());
            assertPlayerReceivedGuidelinesBlockedMessage(player);
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
        }
    }

    @Test
    void blockedSentinelTranslationNotifiesPlayerAndReturnsOriginal() throws IOException {
        try (OpenAIStub stub = OpenAIStub.translationSuccess("{\"success\":true,\"translation\":\"Hola BLOCKED amigo\",\"reason\":\"none\"}")) {
            configureOpenAIProviderConfig(stub);
            plugin().setTranslatorName("ChatGPT");
            WWCTestSupport.useDirectPermissionChecks();
            plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", false);
            PlayerMock player = WWCTestSupport.addOpPlayer("BlockedSentinel");
            player.performCommand("wwct en es");
            drainPlayerMessages(player);

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hello, how are you?", translated);
            assertEquals(0, plugin().getPlayerRecord(player, false).getSuccessfulTranslations());
            assertPlayerReceivedGuidelinesBlockedMessage(player);
            assertEquals(0, stub.guidelinesRequestCount());
            assertEquals(1, stub.translationRequestCount());
        }
    }

    @Test
    void malformedGuidelinesAIResponseBlocksTranslation() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("not json")) {
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesMalformed");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hello, how are you?", translated);
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
        }
    }

    @Test
    void guidelinesAIHttpErrorBlocksTranslation() throws IOException {
        try (OpenAIStub stub = OpenAIStub.error()) {
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesError");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hello, how are you?", translated);
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
        }
    }

    @Test
    void guidelinesAISocketTimeoutBlocksTranslation() throws IOException {
        int originalConnectionTimeout = WorldwideChat.translatorConnectionTimeoutSeconds;
        int originalFatalAbort = WorldwideChat.translatorFatalAbortSeconds;
        try (OpenAIStub stub = OpenAIStub.timeout()) {
            WorldwideChat.translatorConnectionTimeoutSeconds = 1;
            WorldwideChat.translatorFatalAbortSeconds = 3;
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesTimeout");
            player.performCommand("wwct en es");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Hello, how are you?", translated);
            assertEquals(0, plugin().getPlayerRecord(player, false).getSuccessfulTranslations());
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
        } finally {
            WorldwideChat.translatorConnectionTimeoutSeconds = originalConnectionTimeout;
            WorldwideChat.translatorFatalAbortSeconds = originalFatalAbort;
        }
    }

    @Test
    void cacheHitSkipsGuidelinesAICheck() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub, "guidelines-model");
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesCache");
            player.performCommand("wwct en es");
            plugin().addCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?"), "Cached translation");

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText("Hello, how are you?", player);

            assertEquals("Cached translation", translated);
            assertEquals(0, stub.requestCount());
            assertEquals(0, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
        }
    }

    private void configureChatGPTGuidelinesChecks(OpenAIStub stub, String guidelinesModel) {
        configureGuidelinesChecks(stub, guidelinesModel, "ChatGPT");
    }

    private void configureGuidelinesChecks(OpenAIStub stub, String guidelinesModel, String activeTranslator) {
        configureOpenAIProviderConfig(stub);
        plugin().setTranslatorName(activeTranslator);
        WWCTestSupport.useDirectPermissionChecks();
        plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);
        plugin().getConfigManager().getMainConfig().set("Translator.guidelinesAIModel", guidelinesModel);
        plugin().setGuidelinesAIPrompt("Return whether this exact message is translatable.");
    }

    private void configureOpenAIProviderConfig(OpenAIStub stub) {
        plugin().getConfigManager().getMainConfig().set("Translator.testModeTranslator", false);
        plugin().getConfigManager().getMainConfig().set("Translator.useChatGPT", true);
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTAPIKey", "chatgpt-test-key");
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTURL", stub.url());
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTModel", "main-model");
        plugin().getConfigManager().getMainConfig().set("Translator.useOpenAICompatible", true);
        plugin().getConfigManager().getMainConfig().set("Translator.openAICompatibleAPIKey", "compatible-test-key");
        plugin().getConfigManager().getMainConfig().set("Translator.openAICompatibleURL", stub.url());
        plugin().getConfigManager().getMainConfig().set("Translator.openAICompatibleModel", "compatible-main-model");
        plugin().setAISystemPrompt("Translate according to the schema.");
    }

    private static void assertPromptHasDefaultGuardrails(String prompt) {
        assertNotNull(prompt);
        assertTrue(prompt.contains("Translation guardrails:"));
        assertTrue(prompt.contains("Minecraft color codes"));
        assertFalse(prompt.contains("{guardrails}"));
    }

    private static void assertEndsWithUserQueryBlock(String input, String message) {
        String expectedSuffix = "User Query:\n\"\"\"\n" + message + "\n\"\"\"";
        assertTrue(input.endsWith(expectedSuffix),
                () -> "Expected request input to end with User Query block, got:\n" + input);
    }

    private static void assertPlayerReceivedGuidelinesBlockedMessage(PlayerMock player) {
        assertTrue(drainPlayerMessages(player).stream()
                        .anyMatch(message -> message.contains("translation guidelines")),
                "Expected player to receive Guidelines AI block message.");
    }

    private static List<String> drainPlayerMessages(PlayerMock player) {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            try {
                String message = player.nextMessage();
                if (message == null) {
                    return messages;
                }
                messages.add(message);
            } catch (AssertionError noMoreMessages) {
                return messages;
            }
        }
        fail("Player still had queued messages after draining 50 entries.");
        return messages;
    }
}
