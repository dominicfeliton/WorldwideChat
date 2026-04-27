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
    void multiPlayerChatTranslationRoutesOutgoingAndIncomingByTranslatorConfig() {
        PlayerMock speaker = WWCTestSupport.addOpPlayer("Speaker");
        PlayerMock sameConfigListener = WWCTestSupport.addOpPlayer("SameConfigListener");
        PlayerMock englishListener = WWCTestSupport.addOpPlayer("EnglishListener");
        PlayerMock frenchListener = WWCTestSupport.addOpPlayer("FrenchListener");
        PlayerMock autoFrenchListener = WWCTestSupport.addOpPlayer("AutoFrenchListener");
        PlayerMock untranslatedListener = WWCTestSupport.addOpPlayer("UntranslatedListener");
        PlayerMock incomingDisabledListener = WWCTestSupport.addOpPlayer("IncomingDisabledListener");

        speaker.performCommand("wwct en es");
        sameConfigListener.performCommand("wwct en es");
        sameConfigListener.performCommand("wwctci");
        englishListener.performCommand("wwct es en");
        englishListener.performCommand("wwctci");
        frenchListener.performCommand("wwct es fr");
        frenchListener.performCommand("wwctci");
        autoFrenchListener.performCommand("wwct fr");
        autoFrenchListener.performCommand("wwctci");
        incomingDisabledListener.performCommand("wwct es en");

        WWCTestSupport.addCacheTerm("es", "en", "Hola, como estas?", "English cached translation");
        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "French cached translation");
        WWCTestSupport.addCacheTerm("None", "fr", "Hola, como estas?", "Auto French cached translation");

        drainPlayerMessages(speaker);
        drainPlayerMessages(sameConfigListener);
        drainPlayerMessages(englishListener);
        drainPlayerMessages(frenchListener);
        drainPlayerMessages(autoFrenchListener);
        drainPlayerMessages(untranslatedListener);
        drainPlayerMessages(incomingDisabledListener);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(
                sameConfigListener, englishListener, frenchListener, autoFrenchListener,
                untranslatedListener, incomingDisabledListener)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertTrue(event.getRecipients().contains(sameConfigListener));
        assertTrue(event.getRecipients().contains(untranslatedListener));
        assertTrue(event.getRecipients().contains(incomingDisabledListener));
        assertFalse(event.getRecipients().contains(englishListener));
        assertFalse(event.getRecipients().contains(frenchListener));
        assertFalse(event.getRecipients().contains(autoFrenchListener));
        assertTrue(drainPlayerMessages(sameConfigListener).isEmpty());
        assertTrue(drainPlayerMessages(untranslatedListener).isEmpty());
        assertTrue(drainPlayerMessages(incomingDisabledListener).isEmpty());
        assertTrue(drainPlayerMessages(englishListener).stream()
                .anyMatch(message -> message.contains("English cached translation")));
        assertTrue(drainPlayerMessages(frenchListener).stream()
                .anyMatch(message -> message.contains("French cached translation")));
        assertTrue(drainPlayerMessages(autoFrenchListener).stream()
                .anyMatch(message -> message.contains("Auto French cached translation")));
    }

    @Test
    void bidirectionalTranslatorsRespectOutgoingAndIncomingPairDifferences() {
        PlayerMock outgoingOnlySpeaker = WWCTestSupport.addOpPlayer("OutgoingOnlySpeaker");
        PlayerMock samePairBidirectional = WWCTestSupport.addOpPlayer("SamePairBidirectional");
        PlayerMock differentPairBidirectional = WWCTestSupport.addOpPlayer("DifferentPairBidirectional");

        outgoingOnlySpeaker.performCommand("wwct en es");
        samePairBidirectional.performCommand("wwct en es");
        samePairBidirectional.performCommand("wwctci");
        differentPairBidirectional.performCommand("wwct es fr");
        differentPairBidirectional.performCommand("wwctci");

        ActiveTranslator outgoingOnly = plugin().getActiveTranslator(outgoingOnlySpeaker);
        ActiveTranslator samePair = plugin().getActiveTranslator(samePairBidirectional);
        ActiveTranslator differentPair = plugin().getActiveTranslator(differentPairBidirectional);
        assertTrue(outgoingOnly.getTranslatingChatOutgoing());
        assertFalse(outgoingOnly.getTranslatingChatIncoming());
        assertTrue(samePair.getTranslatingChatOutgoing());
        assertTrue(samePair.getTranslatingChatIncoming());
        assertEquals("en", samePair.getInLangCode());
        assertEquals("es", samePair.getOutLangCode());
        assertTrue(differentPair.getTranslatingChatOutgoing());
        assertTrue(differentPair.getTranslatingChatIncoming());
        assertEquals("es", differentPair.getInLangCode());
        assertEquals("fr", differentPair.getOutLangCode());

        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "Different pair incoming translation");
        drainPlayerMessages(outgoingOnlySpeaker);
        drainPlayerMessages(samePairBidirectional);
        drainPlayerMessages(differentPairBidirectional);

        AsyncPlayerChatEvent outgoingOnlyEvent = new AsyncPlayerChatEvent(true, outgoingOnlySpeaker,
                "Hello, how are you?", new HashSet<>(Set.of(samePairBidirectional, differentPairBidirectional)));

        new SpigotChatListener().onPlayerChat(outgoingOnlyEvent);

        assertEquals("Hola, como estas?", outgoingOnlyEvent.getMessage());
        assertFalse(outgoingOnlyEvent.isCancelled());
        assertTrue(outgoingOnlyEvent.getRecipients().contains(samePairBidirectional));
        assertFalse(outgoingOnlyEvent.getRecipients().contains(differentPairBidirectional));
        assertTrue(drainPlayerMessages(samePairBidirectional).isEmpty());
        assertTrue(drainPlayerMessages(differentPairBidirectional).stream()
                .anyMatch(message -> message.contains("Different pair incoming translation")));

        AsyncPlayerChatEvent bidirectionalSpeakerEvent = new AsyncPlayerChatEvent(true, samePairBidirectional,
                "Hello, how are you?", new HashSet<>(Set.of(outgoingOnlySpeaker, differentPairBidirectional)));

        new SpigotChatListener().onPlayerChat(bidirectionalSpeakerEvent);

        assertEquals("Hola, como estas?", bidirectionalSpeakerEvent.getMessage());
        assertFalse(bidirectionalSpeakerEvent.isCancelled());
        assertTrue(bidirectionalSpeakerEvent.getRecipients().contains(outgoingOnlySpeaker));
        assertFalse(bidirectionalSpeakerEvent.getRecipients().contains(differentPairBidirectional));
        assertTrue(drainPlayerMessages(outgoingOnlySpeaker).isEmpty());
        assertTrue(drainPlayerMessages(differentPairBidirectional).stream()
                .anyMatch(message -> message.contains("Different pair incoming translation")));
    }

    @Test
    void globalOutgoingChatTranslationMutatesMessageForNonTranslatorSpeaker() {
        PlayerMock admin = WWCTestSupport.addOpPlayer("GlobalAdmin");
        PlayerMock speaker = WWCTestSupport.addOpPlayer("GlobalSpeaker");
        PlayerMock recipient = WWCTestSupport.addOpPlayer("GlobalRecipient");
        admin.performCommand("wwcg en es");
        drainPlayerMessages(admin);
        drainPlayerMessages(speaker);
        drainPlayerMessages(recipient);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(recipient)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertTrue(event.getRecipients().contains(recipient));
        assertTrue(drainPlayerMessages(recipient).isEmpty());
    }

    @Test
    void globalOutgoingChatFeedsIncomingRecipientTranslations() {
        PlayerMock admin = WWCTestSupport.addOpPlayer("GlobalAdmin");
        PlayerMock speaker = WWCTestSupport.addOpPlayer("GlobalSpeaker");
        PlayerMock frenchListener = WWCTestSupport.addOpPlayer("GlobalFrenchListener");
        PlayerMock untranslatedListener = WWCTestSupport.addOpPlayer("GlobalUntranslatedListener");
        admin.performCommand("wwcg en es");
        frenchListener.performCommand("wwct es fr");
        frenchListener.performCommand("wwctci");
        WWCTestSupport.addCacheTerm("es", "fr", "Hola, como estas?", "Global French cached translation");
        drainPlayerMessages(admin);
        drainPlayerMessages(speaker);
        drainPlayerMessages(frenchListener);
        drainPlayerMessages(untranslatedListener);

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, speaker,
                "Hello, how are you?", new HashSet<>(Set.of(frenchListener, untranslatedListener)));

        new SpigotChatListener().onPlayerChat(event);

        assertEquals("Hola, como estas?", event.getMessage());
        assertFalse(event.isCancelled());
        assertFalse(event.getRecipients().contains(frenchListener));
        assertTrue(event.getRecipients().contains(untranslatedListener));
        assertTrue(drainPlayerMessages(frenchListener).stream()
                .anyMatch(message -> message.contains("Global French cached translation")));
        assertTrue(drainPlayerMessages(untranslatedListener).isEmpty());
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
