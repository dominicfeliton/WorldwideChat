package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void statsCommandOpensStatsGuiForOnlineSelfWithExistingRecord() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StatsSelf");
        plugin().addPlayerRecord(new PlayerRecord("None", player.getUniqueId().toString(), 7, 6));

        player.performCommand("wwcs StatsSelf");
        WWCTestSupport.drainScheduler();

        assertEquals("statsMainMenu", openedSmartInventory(player).getId());
        assertTrue(drainPlayerMessages(player).stream()
                .noneMatch(message -> message.contains("not found")));
    }

    @Test
    void statsCommandOpensStatsGuiForOnlineTargetWithExistingRecord() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("StatsSender");
        PlayerMock target = WWCTestSupport.addOpPlayer("StatsTarget");
        plugin().addPlayerRecord(new PlayerRecord("None", target.getUniqueId().toString(), 8, 5));

        sender.performCommand("wwcs StatsTarget");
        WWCTestSupport.drainScheduler();

        assertEquals("statsMainMenu", openedSmartInventory(sender).getId());
        assertTrue(drainPlayerMessages(sender).stream()
                .noneMatch(message -> message.contains("not found")));
    }

    @Test
    void statsCommandReportsRequestedUnknownPlayerName() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("StatsLookup");

        sender.performCommand("wwcs MissingName");
        WWCTestSupport.drainScheduler();

        List<String> messages = drainPlayerMessages(sender);
        assertTrue(messages.stream().anyMatch(message -> message.contains("MissingName") && message.contains("not found")),
                "Expected /wwcs to report the requested missing target name.");
    }

    @Test
    void statsCommandReportsNoRecordsForResolvedOnlinePlayerWithoutRecord() {
        PlayerMock sender = WWCTestSupport.addOpPlayer("StatsOwner");
        WWCTestSupport.addOpPlayer("NoRecord");

        sender.performCommand("wwcs NoRecord");
        WWCTestSupport.drainScheduler();

        List<String> messages = drainPlayerMessages(sender);
        assertTrue(messages.stream().anyMatch(message -> message.contains("no records yet available") && message.contains("NoRecord")),
                "Expected online player without stats to be resolved as no-records, not missing.");
        assertTrue(messages.stream().noneMatch(message -> message.contains("not found")));
    }

    private SmartInventory openedSmartInventory(PlayerMock player) {
        return plugin().getInventoryManager().getInventory(player)
                .orElseThrow(() -> new AssertionError("Expected a SmartInventory to be open."));
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
