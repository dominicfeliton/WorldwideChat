package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest extends WWCIntegrationTest {

    @Test
    void versionAndReloadCommandsCompleteWithoutInvalidatingTranslator() {
        PlayerMock player = WWCTestSupport.addOpPlayer("Admin");

        player.performCommand("wwc");
        player.performCommand("wwcr");
        WWCTestSupport.drainScheduler();
        WWCTestSupport.waitForCondition(() -> !"Starting".equals(plugin().getTranslatorName()));

        assertEquals("JUnit/MockBukkit Testing Translator", plugin().getTranslatorName());
    }

    @Test
    void selfTranslationCanBeConfiguredAndStopped() {
        PlayerMock player = WWCTestSupport.addOpPlayer("Alpha");

        player.performCommand("wwct en es");
        ActiveTranslator translator = plugin().getActiveTranslator(player);
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());

        player.performCommand("wwct stop");

        assertFalse(plugin().isActiveTranslator(player));
    }

    @Test
    void otherPlayerTranslationCanBeConfiguredAndStoppedByPermittedSender() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("Alpha");
        PlayerMock target = WWCTestSupport.addOpPlayer("Beta");

        sender.performCommand("wwct Beta en es");
        ActiveTranslator translator = plugin().getActiveTranslator(target);
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());

        sender.performCommand("wwct Beta stop");

        assertFalse(plugin().isActiveTranslator(target));
    }

    @Test
    void globalTranslationCanBeConfiguredAndStopped() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("GlobalAdmin");

        sender.performCommand("wwcg en es");
        ActiveTranslator global = plugin().getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
        assertEquals("en", global.getInLangCode());
        assertEquals("es", global.getOutLangCode());

        sender.performCommand("wwcg stop");

        assertFalse(plugin().isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
    }

    @Test
    void localizationCommandHandlesSelfOtherAndStop() {
        PlayerMock alpha = WWCTestSupport.addOpPlayer("Alpha");
        PlayerMock beta = WWCTestSupport.addOpPlayer("Beta");

        alpha.performCommand("wwcl en");
        assertEquals("en", plugin().getPlayerRecord(alpha, false).getLocalizationCode());

        beta.performCommand("wwcl Alpha es");
        assertEquals("es", plugin().getPlayerRecord(alpha, false).getLocalizationCode());

        alpha.performCommand("wwcl stop");
        assertEquals("", plugin().getPlayerRecord(alpha, false).getLocalizationCode());
    }

    @Test
    void inGameObjectChatAndRateLimitTogglesUpdateActiveTranslator() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ToggleUser");

        player.performCommand("wwct en es");
        player.performCommand("wwctb");
        player.performCommand("wwcti");
        player.performCommand("wwcts");
        player.performCommand("wwcte");
        player.performCommand("wwctci");
        player.performCommand("wwctco");
        player.performCommand("wwctrl 5");

        ActiveTranslator translator = plugin().getActiveTranslator(player);
        assertTrue(translator.getTranslatingBook());
        assertTrue(translator.getTranslatingItem());
        assertTrue(translator.getTranslatingSign());
        assertTrue(translator.getTranslatingEntity());
        assertTrue(translator.getTranslatingChatIncoming());
        assertFalse(translator.getTranslatingChatOutgoing());
        assertEquals(5, translator.getRateLimit());

        player.performCommand("wwctrl");
        assertEquals(0, translator.getRateLimit());
    }

    @Test
    void consoleCanTargetPlayersAndGlobalTranslation() {
        PlayerMock beta = WWCTestSupport.addOpPlayer("Beta");
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        Bukkit.dispatchCommand(console, "wwct Beta en es");
        ActiveTranslator translator = plugin().getActiveTranslator(beta);
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());

        Bukkit.dispatchCommand(console, "wwcg en es");
        assertTrue(plugin().isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
    }

    @Test
    void invalidSelfReconfigurationDoesNotRemoveExistingSession() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StableUser");
        player.performCommand("wwct en es");

        player.performCommand("wwct en en");
        ActiveTranslator translator = plugin().getActiveTranslator(player);

        assertTrue(plugin().isActiveTranslator(player));
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());

        player.performCommand("wwct en es extra");
        assertTrue(plugin().isActiveTranslator(player));
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());
    }

    @Test
    void usersWithoutPermissionCannotMutateOtherPlayerSessions() {
        PlayerMock owner = WWCTestSupport.addOpPlayer("Owner");
        PlayerMock target = WWCTestSupport.addOpPlayer("Target");
        PlayerMock intruder = WWCTestSupport.addPlayer("Intruder");
        owner.performCommand("wwct Target en es");

        intruder.performCommand("wwct Target fr en");
        ActiveTranslator translator = plugin().getActiveTranslator(target);
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());

        intruder.performCommand("wwct Target stop");
        assertTrue(plugin().isActiveTranslator(target));
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());
    }
}
