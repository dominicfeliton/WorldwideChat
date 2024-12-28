package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.commands.Commands;
import com.dominicfeliton.worldwidechat.util.DataRetention;
import com.dominicfeliton.worldwidechat.util.Translation;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestMain {

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
        TestCommon.reload(server, plugin,true);

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
        Translation test = new Translation(player1, player2, plugin, server);
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

        Commands test = new Commands(playerA, playerB, playerC, plugin, server);
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

    @Order(3)
    @Test
    public void testDataRetention() {
        PlayerMock p1 = server.addPlayer("user1");
        PlayerMock p2 = server.addPlayer("user2");
        p1.setOp(true);
        p2.setOp(true);

        DataRetention test = new DataRetention(p1, p2, plugin, server);
        List<Runnable> tests = Arrays.asList(
                test::testPluginDataRetentionMongoDB,
                test::testPluginDataRetentionPostgres,
                test::testPluginDataRetentionYAML,
                test::testPluginDataRetentionSQL
        );
        Collections.shuffle(tests);
        for (Runnable runnable : tests) {
            runnable.run();
        }
    }

}
