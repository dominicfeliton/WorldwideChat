package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.commands.CommandsTest;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.TranslationTest;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Method;

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

    @AfterEach
    public void resetWWC(TestInfo testInfo) {
        server.executeConsole("wwcd", "reset confirm");
        server.getScheduler().waitAsyncTasksFinished();
        server.getScheduler().waitAsyncEventsFinished();

        String methodName = testInfo.getTestMethod().map(Method::getName).orElse("Unknown");
        plugin.getLogger().info("Completed Tests: " + methodName);
    }

    @Order(1)
    @Test
    public void testTranslations() {
        // Setup
        PlayerMock player1 = server.addPlayer("player1");
        PlayerMock player2 = server.addPlayer("player2");
        player1.setOp(true);
        player2.setOp(true);

        // Execution
        TranslationTest test = new TranslationTest(player1, player2, plugin, server);
        test.testTranslationFunctionSourceTarget();
        test.testTranslationFunctionTarget();
        test.testTranslationFunctionSourceTargetOther();
        test.testTranslationFunctionTargetOther();
        test.testCacheBehaviorInTranslation();
        test.testNoTranslatorSetup();
    }

    @Order(2)
    @Test
    public void testCommands() {
        PlayerMock playerA = server.addPlayer("Alpha");
        PlayerMock playerB = server.addPlayer("Beta");
        PlayerMock playerC = server.addPlayer("Charlie");
        playerA.setOp(true);
        playerB.setOp(true);

        CommandsTest test = new CommandsTest(playerA, playerB, playerC, plugin, server);
        test.testWwcVersionAndReloadCommands();
        test.testWwcStatsAndDebug();
        test.testWwcTranslateSelfAndStop();
        test.testWwcTranslateOthersAndStop();
        test.testWwcGlobalTranslate();
        test.testWwcLocalizeCommand();
        test.testWwcTranslateInGameObjects();
        test.testWwcTranslateChatIncomingOutgoing();
        test.testWwcRateLimit();
        test.testConsoleUsage();
    }

}
