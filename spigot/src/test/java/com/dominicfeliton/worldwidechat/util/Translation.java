package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the internal translation logic:
 *   - source->target
 *   - auto->target
 *   - usage of /wwct for self vs. other players
 *   - ensuring “None” is handled properly
 */
public class Translation {

    private PlayerMock player1, player2;
    private CommonRefs refs;
    private WorldwideChat plugin;
    private ServerMock server;

    public Translation(PlayerMock player1, PlayerMock player2, WorldwideChat plugin, ServerMock server) {
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.plugin = plugin;
        refs = plugin.getServerFactory().getCommonRefs();
    }

    public void testTranslationFunctionSourceTarget() {
        player1.performCommand("wwct en es");
        assertEquals("Hola, como estas?",
                refs.translateText("Hello, how are you?", player1),
                "Simple English->Spanish check (example output; can vary with real translator).");
    }

    public void testTranslationFunctionTarget() {
        player1.performCommand("wwct es");
        assertEquals("Cuantos diamantes tienes?",
                refs.translateText("How many diamonds do you have?", player1),
                "Simple auto->Spanish check. May differ with real translator.");
    }

    public void testTranslationFunctionSourceTargetOther() {
        player1.performCommand("wwct player2 en es");
        assertEquals("Hola, como estas?",
                refs.translateText("Hello, how are you?", player2));
    }

    public void testTranslationFunctionTargetOther() {
        player1.performCommand("wwct player2 es");
        assertEquals("Cuantos diamantes tienes?",
                refs.translateText("How many diamonds do you have?", player2));
    }

    public void testCacheBehaviorInTranslation() {
        // Turn on a translator
        player1.performCommand("wwct en es");
        // Do a translation
        String out = refs.translateText("Test me!", player1);
        assertNotNull(out);
        // Check that it appears in the cache if translator has caching
        // We won't check exact phrase unless we want to mock external translator
        assertTrue(plugin.getCache().estimatedSize() > 0);
    }

    public void testNoTranslatorSetup() {
        // If no translator is set for player1,
        // attempt a translation => return same phrase
        player1.performCommand("wwct stop");
        String out = refs.translateText("Should remain the same if no translator", player1);
        assertEquals("Should remain the same if no translator", out);
    }
}