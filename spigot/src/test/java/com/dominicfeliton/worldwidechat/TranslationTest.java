package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.TranslationCapacityLimiter;
import com.dominicfeliton.worldwidechat.translators.TestTranslation;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    void translationCapacityFullReturnsOriginalWithoutCountingTranslatorError() throws Exception {
        PlayerMock player = WWCTestSupport.addOpPlayer("CapacityFull");
        player.performCommand("wwct en es");
        drainPlayerMessages(player);
        TranslationCapacityLimiter originalLimiter = plugin().getTranslationCapacityLimiter();
        TranslationCapacityLimiter limited = TranslationCapacityLimiter.forLimits(1, 0);
        Field limiterField = WorldwideChat.class.getDeclaredField("translationCapacityLimiter");
        limiterField.setAccessible(true);
        limiterField.set(plugin(), limited);
        plugin().setTranslatorErrorCount(0);

        try (TranslationCapacityLimiter.Permit heldPermit = limited.acquire(1, TimeUnit.MILLISECONDS)) {
            assertTrue(heldPermit.acquired());
            String input = "capacity-full-translation";

            String translated = plugin().getServerFactory().getCommonRefs()
                    .translateText(input, player);

            assertEquals(input, translated);
            assertEquals(0, plugin().getTranslatorErrorCount());
        } finally {
            limiterField.set(plugin(), originalLimiter);
        }
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
    void objectBatchWideLimitOnlyRunsPendingEntries() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ObjectBatchWide");
        player.performCommand("wwct en es");
        plugin().setObjectTranslationConcurrencyLimit(10);
        TestTranslation.resetConcurrencyTracking();
        TestTranslation.setArtificialDelayMillis(75);

        try {
            List<String> translated = plugin().getServerFactory().getCommonRefs()
                    .translateObjectText(List.of("batch-wide-0", "batch-wide-1", "batch-wide-2"), player);

            assertEquals(List.of(
                    "translated-batch-wide-0",
                    "translated-batch-wide-1",
                    "translated-batch-wide-2"), translated);
            assertTrue(TestTranslation.getMaxActiveTranslations() > 1);
            assertTrue(TestTranslation.getMaxActiveTranslations() <= 3);
        } finally {
            TestTranslation.resetConcurrencyTracking();
        }
    }

    @Test
    void objectBatchTimeoutUsesTranslatorConnectionTimeoutAndCancelsProviderWork() throws InterruptedException {
        int originalConnectionTimeout = WorldwideChat.translatorConnectionTimeoutSeconds;
        int originalFatalAbort = WorldwideChat.translatorFatalAbortSeconds;
        PlayerMock player = WWCTestSupport.addOpPlayer("ObjectBatchTimeout");
        player.performCommand("wwct en es");
        drainPlayerMessages(player);
        plugin().setObjectTranslationConcurrencyLimit(4);
        TestTranslation.resetConcurrencyTracking();
        TestTranslation.setArtificialDelayMillis(5000);

        try {
            WorldwideChat.translatorConnectionTimeoutSeconds = 1;
            WorldwideChat.translatorFatalAbortSeconds = 6;
            List<String> input = List.of("batch-timeout-0", "batch-timeout-1", "batch-timeout-2", "batch-timeout-3");

            long startTime = System.nanoTime();
            List<String> translated = plugin().getServerFactory().getCommonRefs()
                    .translateObjectText(input, player);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            assertEquals(input, translated);
            assertTrue(elapsedMillis < 4000,
                    () -> "Expected object batch to timeout through translatorConnectionTimeoutSeconds, elapsed " + elapsedMillis + "ms.");
            waitForActiveTranslationsToDrain();
            WWCTestSupport.drainScheduler();
            List<String> messages = drainPlayerMessages(player);
            assertTrue(messages.stream().anyMatch(message -> message.contains("timed out")),
                    () -> "Expected provider timeout to use the timeout message path. Messages: " + messages);
            assertTrue(messages.stream().noneMatch(message -> message.contains("problem occurred")),
                    "Expected provider timeout not to use the generic translator error path.");
        } finally {
            WorldwideChat.translatorConnectionTimeoutSeconds = originalConnectionTimeout;
            WorldwideChat.translatorFatalAbortSeconds = originalFatalAbort;
            TestTranslation.resetConcurrencyTracking();
        }
    }

    @Test
    void objectTranslationConcurrencyConfigDefaultsAcceptsWideValuesAndRejectsBelowOne() {
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 2);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(2, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 5);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(5, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 32);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(32, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", 0);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", -1);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());

        plugin().getConfigManager().getMainConfig().set("General.objectTranslationConcurrencyLimit", "many");
        plugin().getConfigManager().loadMainSettings();
        assertEquals(4, plugin().getObjectTranslationConcurrencyLimit());
    }

    @Test
    void translationCapacityConfigAcceptsAutoOrPositiveAndRejectsBadValues() {
        plugin().getConfigManager().getMainConfig().set("General.translationCapacityLimit", 0);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(0, plugin().getTranslationCapacityLimit());
        assertEquals(
                TranslationCapacityLimiter.resolveActiveLimit(0, Runtime.getRuntime().availableProcessors()),
                plugin().getTranslationCapacityLimiter().getActiveLimit());
        assertEquals(
                TranslationCapacityLimiter.resolveQueueLimit(plugin().getTranslationCapacityLimiter().getActiveLimit()),
                plugin().getTranslationCapacityLimiter().getQueueLimit());

        plugin().getConfigManager().getMainConfig().set("General.translationCapacityLimit", 12);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(12, plugin().getTranslationCapacityLimit());
        assertEquals(12, plugin().getTranslationCapacityLimiter().getActiveLimit());
        assertEquals(48, plugin().getTranslationCapacityLimiter().getQueueLimit());

        plugin().getConfigManager().getMainConfig().set("General.translationCapacityLimit", -1);
        plugin().getConfigManager().loadMainSettings();
        assertEquals(0, plugin().getTranslationCapacityLimit());

        plugin().getConfigManager().getMainConfig().set("General.translationCapacityLimit", "many");
        plugin().getConfigManager().loadMainSettings();
        assertEquals(0, plugin().getTranslationCapacityLimit());
    }

    @Test
    void translatorErrorCountIncrementsAtomically() throws Exception {
        plugin().setTranslatorErrorCount(0);
        int workers = 8;
        int incrementsPerWorker = 250;
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < workers; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(1, TimeUnit.SECONDS));
                    for (int j = 0; j < incrementsPerWorker; j++) {
                        plugin().incrementTranslatorErrorCount();
                    }
                    return null;
                }));
            }

            assertTrue(ready.await(1, TimeUnit.SECONDS));
            start.countDown();
            for (Future<?> future : futures) {
                future.get(2, TimeUnit.SECONDS);
            }

            assertEquals(workers * incrementsPerWorker, plugin().getTranslatorErrorCount());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
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
    void rateLimitExemptPermissionOverridesNumericRateLimit() {
        PlayerMock player = WWCTestSupport.addPlayer("RateLimitExempt");
        player.addAttachment(plugin(), "worldwidechat.ratelimit.5", true);
        player.addAttachment(plugin(), "worldwidechat.ratelimit.exempt", true);
        player.performCommand("wwct en es");
        drainPlayerMessages(player);

        assertTwoImmediateBatchTranslationsAllowed(player, "batch-rate-exempt");
        WWCTestSupport.drainScheduler();
        assertTrue(drainPlayerMessages(player).stream()
                        .noneMatch(message -> message.contains("rate limited")),
                "Expected exempt player not to receive a rate-limit message.");
    }

    @Test
    void numericRateLimitPermissionStillBlocksImmediateSecondTranslation() {
        PlayerMock player = WWCTestSupport.addPlayer("RateLimitNumeric");
        player.addAttachment(plugin(), "worldwidechat.ratelimit.5", true);
        player.performCommand("wwct en es");
        drainPlayerMessages(player);

        String firstInput = "batch-rate-numeric-first";
        String secondInput = "batch-rate-numeric-second";
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();

        assertEquals("translated-" + firstInput, refs.translateText(firstInput, player));
        assertEquals(secondInput, refs.translateText(secondInput, player));
        WWCTestSupport.drainScheduler();
        assertTrue(drainPlayerMessages(player).stream()
                        .anyMatch(message -> message.contains("rate limited")),
                "Expected numeric rate-limit permission to block the second immediate translation.");
    }

    @Test
    void disabledNumericRateLimitPermissionIsIgnored() {
        PlayerMock player = WWCTestSupport.addPlayer("RateLimitDisabledNumeric");
        player.addAttachment(plugin(), "worldwidechat.ratelimit.5", false);
        player.performCommand("wwct en es");
        drainPlayerMessages(player);

        assertTwoImmediateBatchTranslationsAllowed(player, "batch-rate-disabled-numeric");
    }

    @Test
    void nonNumericRateLimitPermissionIsIgnored() {
        PlayerMock player = WWCTestSupport.addPlayer("RateLimitNonNumeric");
        player.addAttachment(plugin(), "worldwidechat.ratelimit.fast", true);
        player.performCommand("wwct en es");
        drainPlayerMessages(player);

        assertTwoImmediateBatchTranslationsAllowed(player, "batch-rate-nonnumeric");
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
    void guidelinesAICheckSkipsAtRuntimeWhenNoAIProviderIsEnabled() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureOpenAIProviderConfig(stub);
            setAIProviderFlags(false, false, false);
            plugin().setTranslatorName("ChatGPT");
            WWCTestSupport.useDirectPermissionChecks();
            plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);
            PlayerMock player = WWCTestSupport.addOpPlayer("GuidelinesNoAIProvider");
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
    void guidelinesAIChecksDisabledOnStartupWhenNoAIProviderIsEnabled() {
        setAIProviderFlags(false, false, false);
        plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);

        plugin().getConfigManager().initAISettings();

        assertFalse(plugin().getConfigManager().getMainConfig().getBoolean("Translator.enableGuidelinesAIChecks"));
    }

    @Test
    void guidelinesAIChecksStayEnabledOnStartupWhenAnyAIProviderIsEnabled() {
        assertGuidelinesStartupAllowedFor("Translator.useChatGPT");
        assertGuidelinesStartupAllowedFor("Translator.useOpenAICompatible");
        assertGuidelinesStartupAllowedFor("Translator.useOllama");
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

    private void assertGuidelinesStartupAllowedFor(String enabledProviderConfigKey) {
        setAIProviderFlags(false, false, false);
        plugin().getConfigManager().getMainConfig().set(enabledProviderConfigKey, true);
        plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);

        plugin().getConfigManager().initAISettings();

        assertTrue(plugin().getConfigManager().getMainConfig().getBoolean("Translator.enableGuidelinesAIChecks"),
                enabledProviderConfigKey + " should allow Guidelines AI checks to remain enabled.");
    }

    private void setAIProviderFlags(boolean chatGPT, boolean openAICompatible, boolean ollama) {
        plugin().getConfigManager().getMainConfig().set("Translator.useChatGPT", chatGPT);
        plugin().getConfigManager().getMainConfig().set("Translator.useOpenAICompatible", openAICompatible);
        plugin().getConfigManager().getMainConfig().set("Translator.useOllama", ollama);
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

    private static void waitForActiveTranslationsToDrain() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 1000;
        while (TestTranslation.getActiveTranslations() != 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(25);
        }
        assertEquals(0, TestTranslation.getActiveTranslations(),
                "Expected timed-out provider work to be cancelled.");
    }

    private void assertTwoImmediateBatchTranslationsAllowed(PlayerMock player, String inputPrefix) {
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();
        String firstInput = inputPrefix + "-first";
        String secondInput = inputPrefix + "-second";

        assertEquals("translated-" + firstInput, refs.translateText(firstInput, player));
        assertEquals("translated-" + secondInput, refs.translateText(secondInput, player));
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
