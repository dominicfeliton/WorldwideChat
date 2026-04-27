package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.SpigotChatListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotPlayerLocaleListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotSignListener;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ListenerTest extends WWCIntegrationTest {

    @Test
    void outgoingChatTranslationMutatesAsyncChatMessage() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("Listener");
        sender.performCommand("wwct en es");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
    }

    @Test
    void incomingChatTranslationRemovesTranslatedRecipientFromOriginalEvent() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock translatedRecipient = WWCTestSupport.addOpPlayer("TranslatedRecipient");
        PlayerMock untouchedRecipient = WWCTestSupport.addOpPlayer("UntouchedRecipient");
        sender.performCommand("wwct en fr");
        sender.performCommand("wwctco");
        translatedRecipient.performCommand("wwct en es");
        translatedRecipient.performCommand("wwctci");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(translatedRecipient, untouchedRecipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertFalse(event.getRecipients().contains(translatedRecipient));
        assertTrue(event.getRecipients().contains(untouchedRecipient));
    }

    @Test
    void incomingGuidelinesAIBlockRunsOnceAndLeavesOriginalRecipients() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub);
            PlayerMock sender = WWCTestSupport.addOpPlayer("GuidelinesSpeaker");
            PlayerMock firstRecipient = WWCTestSupport.addOpPlayer("GuidelinesListenerOne");
            PlayerMock secondRecipient = WWCTestSupport.addOpPlayer("GuidelinesListenerTwo");
            sender.performCommand("wwct en fr");
            sender.performCommand("wwctco");
            firstRecipient.performCommand("wwct en es");
            firstRecipient.performCommand("wwctci");
            secondRecipient.performCommand("wwct en es");
            secondRecipient.performCommand("wwctci");
            drainPlayerMessages(sender);

            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                    "Hello, how are you?", new HashSet<>(Set.of(firstRecipient, secondRecipient)));

            new SpigotChatListener().onPlayerChat(event);

            assertEquals("Hello, how are you?", event.getMessage());
            assertFalse(event.isCancelled());
            assertTrue(event.getRecipients().contains(firstRecipient));
            assertTrue(event.getRecipients().contains(secondRecipient));
            assertEquals(1, stub.guidelinesRequestCount());
            assertEquals(0, stub.translationRequestCount());
            assertEquals(1, drainPlayerMessages(sender).stream()
                    .filter(message -> message.contains("translation guidelines"))
                    .count());
        }
    }

    @Test
    void incomingCachedTranslationsSkipGuidelinesAIBlock() throws IOException {
        try (OpenAIStub stub = OpenAIStub.success("{\"translatable\":false}")) {
            configureChatGPTGuidelinesChecks(stub);
            PlayerMock sender = WWCTestSupport.addOpPlayer("CachedGuidelinesSpeaker");
            PlayerMock firstRecipient = WWCTestSupport.addOpPlayer("CachedGuidelinesListenerOne");
            PlayerMock secondRecipient = WWCTestSupport.addOpPlayer("CachedGuidelinesListenerTwo");
            sender.performCommand("wwct en fr");
            sender.performCommand("wwctco");
            firstRecipient.performCommand("wwct en es");
            firstRecipient.performCommand("wwctci");
            secondRecipient.performCommand("wwct en es");
            secondRecipient.performCommand("wwctci");
            plugin().addCacheTerm(new CachedTranslation("en", "es", "Hello, how are you?"), "Cached translation");

            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                    "Hello, how are you?", new HashSet<>(Set.of(firstRecipient, secondRecipient)));

            new SpigotChatListener().onPlayerChat(event);

            assertEquals(0, stub.requestCount());
            assertFalse(event.getRecipients().contains(firstRecipient));
            assertFalse(event.getRecipients().contains(secondRecipient));
        }
    }

    @Test
    void forcedSeparateChannelCancelsTranslatedOriginalChatEvent() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("ForcedSpeaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("ForcedListener");
        plugin().setForceSeparateChatChannel(true);
        sender.performCommand("wwct en es");

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertTrue(event.isCancelled());
        assertEquals("Hello, how are you?", event.getMessage());
    }

    @Test
    void signEditIsCancelledAndWarningRecordedWhenSignTranslationEnabled() {
        PlayerMock player = WWCTestSupport.addOpPlayer("SignEditor");
        player.performCommand("wwct en es");
        player.performCommand("wwcts");
        ActiveTranslator translator = plugin().getActiveTranslator(player);

        PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, null, Side.FRONT,
                PlayerSignOpenEvent.Cause.INTERACT);

        new SpigotSignListener().onSignEdit(event);

        assertTrue(event.isCancelled());
        assertTrue(translator.getSignWarning());
    }

    @Test
    void localeListenerStoresSupportedLocaleCode() {
        PlayerMock player = WWCTestSupport.addOpPlayer("LocaleUser");

        new SpigotPlayerLocaleListener().checkAndSetLocale((Player) player, "es");
        WWCTestSupport.drainScheduler();

        assertEquals("es", plugin().getPlayerRecord(player, false).getLocalizationCode());
    }

    private void configureChatGPTGuidelinesChecks(OpenAIStub stub) {
        plugin().getConfigManager().getMainConfig().set("Translator.testModeTranslator", false);
        plugin().getConfigManager().getMainConfig().set("Translator.useChatGPT", true);
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTAPIKey", "chatgpt-test-key");
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTURL", stub.url());
        plugin().getConfigManager().getMainConfig().set("Translator.chatGPTModel", "main-model");
        plugin().getConfigManager().getMainConfig().set("Translator.useOpenAICompatible", false);
        plugin().getConfigManager().getMainConfig().set("Translator.enableGuidelinesAIChecks", true);
        plugin().getConfigManager().getMainConfig().set("Translator.guidelinesAIModel", "guidelines-model");
        plugin().setAISystemPrompt("Translate according to the schema.");
        plugin().setGuidelinesAIPrompt("Return whether this exact message is translatable.");
        plugin().setTranslatorName("ChatGPT");
        WWCTestSupport.useDirectPermissionChecks();
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
