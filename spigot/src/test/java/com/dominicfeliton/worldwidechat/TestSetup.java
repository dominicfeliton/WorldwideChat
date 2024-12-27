package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.commands.CommandsTest;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.TranslationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

public class TestSetup {

    // This is because MockBukkit doesn't fully deregister static variables. Very cool

    protected static ServerMock server;
    protected static WorldwideChat plugin;

    @BeforeAll
    public static void setUp()
    {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(WorldwideChat.class);
    }

    @AfterAll
    public static void tearDown() {
        MockBukkit.unmock();
    }

    @Order(1)
    @Test
    public void testTranslations() {
        // Setup
        PlayerMock player1 = server.addPlayer("player1");
        PlayerMock player2 = server.addPlayer("player2");
        player1.setOp(true);
        player2.setOp(true);

        TranslationTest test = new TranslationTest(player1, player2, plugin, server);
        test.testCacheBehaviorInTranslation();
    }

    @Order(2)
    @Test
    public void testCommands() {
        // Setup
        PlayerMock playerA = server.addPlayer("Alpha");
        PlayerMock playerB = server.addPlayer("Beta");
        PlayerMock playerC = server.addPlayer("Charlie");
        playerA.setOp(true);
        playerB.setOp(true);

        CommandsTest test = new CommandsTest(playerA, playerB, playerC, plugin, server);
        test.testWwcVersionAndReloadCommands();
    }

}
