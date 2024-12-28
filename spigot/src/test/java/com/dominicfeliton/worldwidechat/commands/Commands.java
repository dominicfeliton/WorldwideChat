package com.dominicfeliton.worldwidechat.commands;

import com.dominicfeliton.worldwidechat.TestCommon;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers thorough command usage:
 *    /wwc, /wwcr, /wwcs, /wwcd, /wwcg,
 *    /wwct, /wwcl, /wwctb, /wwcts, /wwcti, /wwcte,
 *    /wwctco, /wwctci, /wwctrl,
 *    and user-based toggles.
 */
public class Commands {

    private WorldwideChat plugin;
    private ServerMock server;
    private PlayerMock playerA;
    private PlayerMock playerB;
    private PlayerMock playerC; // Non-op for partial perms testing
    private ConsoleCommandSender console;

    public Commands(PlayerMock playerA, PlayerMock playerB, PlayerMock playerC, WorldwideChat plugin, ServerMock server) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.playerC = playerC;
        this.plugin = plugin;
        this.server = server;
        console = Bukkit.getConsoleSender();
    }

    public void testWwcVersionAndReloadCommands() {
        // /wwc => prints plugin version
        playerA.performCommand("wwc");
        // We simply check that no exception occurred.

        // /wwcr => reload plugin
        TestCommon.reload(server, plugin);
        plugin.getLogger().info("Current translator name: " + plugin.getTranslatorName());
        assertNotEquals("Starting", plugin.getTranslatorName());
    }

    public void testWwcStatsAndDebug() {
        // /wwcs => stats for translator
        playerA.performCommand("wwcs");
        // /wwcs Beta
        playerA.performCommand("wwcs Beta");
        //server.getScheduler().waitAsyncTasksFinished();
        //assertNotEquals(playerA.get, InventoryType.CRAFTING);

        // /wwcd => debug
        // e.g. "wwcd version", "wwcd cache clear"
        playerA.performCommand("wwcd version");
        playerA.performCommand("wwcd cache clear");
        // Just ensuring no crash.
    }

    public void testWwcTranslateSelfAndStop() {
        // /wwct en es
        playerA.performCommand("wwct en es");
        ActiveTranslator atA = plugin.getActiveTranslator(playerA);
        assertEquals("en", atA.getInLangCode());
        assertEquals("es", atA.getOutLangCode());

        // /wwct stop
        playerA.performCommand("wwct stop");
        assertFalse(plugin.isActiveTranslator(playerA));
    }

    public void testWwcTranslateOthersAndStop() {
        // /wwct Beta en es
        playerA.performCommand("wwct Beta en es");
        ActiveTranslator atB = plugin.getActiveTranslator(playerB);
        assertEquals("en", atB.getInLangCode());
        assertEquals("es", atB.getOutLangCode());

        // /wwct Beta stop
        playerA.performCommand("wwct Beta stop");
        assertFalse(plugin.isActiveTranslator(playerB));
    }

    public void testWwcGlobalTranslate() {
        // /wwcg en es
        playerA.performCommand("wwcg en es");
        ActiveTranslator global = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
        assertEquals("en", global.getInLangCode());
        assertEquals("es", global.getOutLangCode());

        // /wwcg stop
        playerA.performCommand("wwcg stop");
        assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
    }

    public void testWwcLocalizeCommand() {
        // /wwcl en
        playerA.performCommand("wwcl en");
        assertEquals("en", plugin.getPlayerRecord(playerA, false).getLocalizationCode());

        // /wwcl Alpha es => from Beta
        playerB.performCommand("wwcl Alpha es");
        assertEquals("es", plugin.getPlayerRecord(playerA, false).getLocalizationCode());

        // /wwcl stop => from alpha
        playerA.performCommand("wwcl stop");
        assertTrue(plugin.getPlayerRecord(playerA, false).getLocalizationCode().isEmpty());
    }

    public void testWwcTranslateInGameObjects() {
        // /wwctb => Book
        playerA.performCommand("wwct en es");
        playerA.performCommand("wwctb");
        assertTrue(plugin.getActiveTranslator(playerA).getTranslatingBook());

        // /wwcti => Item
        playerA.performCommand("wwcti");
        assertTrue(plugin.getActiveTranslator(playerA).getTranslatingItem());

        // /wwcts => Sign
        playerA.performCommand("wwcts");
        assertTrue(plugin.getActiveTranslator(playerA).getTranslatingSign());

        // /wwcte => Entity
        playerA.performCommand("wwcte");
        assertTrue(plugin.getActiveTranslator(playerA).getTranslatingEntity());
    }

    public void testWwcTranslateChatIncomingOutgoing() {
        playerA.performCommand("wwct en es");

        // /wwctci => incoming
        playerA.performCommand("wwctci");
        assertTrue(plugin.getActiveTranslator(playerA).getTranslatingChatIncoming());

        // /wwctco => outgoing
        playerA.performCommand("wwctco");
        assertFalse(plugin.getActiveTranslator(playerA).getTranslatingChatOutgoing());
    }

    public void testWwcRateLimit() {
        // /wwctrl 5
        playerA.performCommand("wwct en es");
        playerA.performCommand("wwctrl 5");
        assertEquals(5, plugin.getActiveTranslator(playerA).getRateLimit());

        // /wwctrl => should disable
        playerA.performCommand("wwctrl");
        assertEquals(0, plugin.getActiveTranslator(playerA).getRateLimit());
    }

    public void testConsoleUsage() {
        // /wwct Beta en es => from console
        Bukkit.dispatchCommand(console, "wwct Beta en es");
        ActiveTranslator atB = plugin.getActiveTranslator(playerB);
        assertEquals("en", atB.getInLangCode());
        assertEquals("es", atB.getOutLangCode());

        // /wwcg en es => from console
        Bukkit.dispatchCommand(console, "wwcg en es");
        assertTrue(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
    }
}