package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.SpigotChatListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotPlayerLocaleListener;
import com.dominicfeliton.worldwidechat.listeners.SpigotSignListener;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
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
}
