package com.badskater0729.worldwidechat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.badskater0729.worldwidechat.commands.TestConsoleCommands;
import com.badskater0729.worldwidechat.commands.TestPlayerCommands;
import com.badskater0729.worldwidechat.inventory.TestPlayerGUI;
import com.badskater0729.worldwidechat.util.TestTranslationUtils;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldwideChatTests {
	private static ServerMock server;
	private static WorldwideChat plugin;
	private static PlayerMock playerMock;
	private static PlayerMock secondPlayerMock;

	private static int testCount = 0;

	/* Init all test classes */
	TestPlayerCommands testPlayerCommands = new TestPlayerCommands(server, plugin, playerMock, secondPlayerMock);
	TestConsoleCommands testConsoleCommands = new TestConsoleCommands(server, plugin, playerMock, secondPlayerMock);
	TestPlayerGUI testPlayerGUI = new TestPlayerGUI(server, plugin, playerMock, secondPlayerMock);
	TestTranslationUtils testTranslationUtils = new TestTranslationUtils(server, plugin, playerMock, secondPlayerMock);

	@BeforeAll
	public static void setUp() {
		// Setup vars
		server = MockBukkit.mock();
		plugin = MockBukkit.load(WorldwideChat.class);
		playerMock = server.addPlayer("player1");
		secondPlayerMock = server.addPlayer("player2");

		/* Add perms */
		playerMock.setOp(true);
		secondPlayerMock.setOp(true);
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}

	public static void resetWWC() {
		// Needs to be defined this way because MockBukkit is janky
		plugin.getActiveTranslators().clear();
		plugin.getPlayerRecords().clear();
		plugin.getCache().invalidateAll();
		plugin.getCache().cleanUp();
		reloadWWC();
	}
	
	public static void reloadWWC() {
		plugin.cancelBackgroundTasks(false);
		plugin.onEnable();
	}

	public void sendCompletedMessage() {
		testCount++;
		plugin.getLogger().info("=== (Test " + testCount + ") Completed Successfully ===");
	}

	/* Player Command Tests */
	@Order(1)
	@Test
	public void testPlayerCommands() {
		/* Print start message */
		plugin.getLogger().info("=== Test Player Commands ===");

		/* Correct inputs, expect correct outputs */
		testPlayerCommands.testTranslateCommandPlayerSourceTarget();
		testPlayerCommands.testTranslateCommandPlayerSourceTargetOther();
		testPlayerCommands.testTranslateCommandPlayerTarget();
		testPlayerCommands.testTranslateCommandPlayerTargetOther();
		testPlayerCommands.testTranslateCommandSamePlayerTarget();
		testPlayerCommands.testTranslateCommandSamePlayerSourceTarget();
		testPlayerCommands.testGlobalTranslateCommandPlayerSourceTarget();
		testPlayerCommands.testGlobalTranslateCommandPlayerTarget();
		testPlayerCommands.testOutgoingTranslateCommandPlayer(false);
		testPlayerCommands.testOutgoingTranslateCommandPlayer(true);
		testPlayerCommands.testOutgoingTranslateCommandPlayerOther(false);
		testPlayerCommands.testOutgoingTranslateCommandPlayerOther(true);
		testPlayerCommands.testOutgoingTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testOutgoingTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testIncomingTranslateCommandPlayer(true);
		testPlayerCommands.testIncomingTranslateCommandPlayer(false);
		testPlayerCommands.testIncomingTranslateCommandPlayerOther(true);
		testPlayerCommands.testIncomingTranslateCommandPlayerOther(false);
		testPlayerCommands.testIncomingTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testIncomingTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testBookTranslateCommandPlayer(true);
		testPlayerCommands.testBookTranslateCommandPlayer(false);
		testPlayerCommands.testBookTranslateCommandPlayerOther(true);
		testPlayerCommands.testBookTranslateCommandPlayerOther(false);
		testPlayerCommands.testBookTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testBookTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testSignTranslateCommandPlayer(true);
		testPlayerCommands.testSignTranslateCommandPlayer(false);
		testPlayerCommands.testSignTranslateCommandPlayerOther(true);
		testPlayerCommands.testSignTranslateCommandPlayerOther(false);
		testPlayerCommands.testSignTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testSignTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testItemTranslateCommandPlayer(true);
		testPlayerCommands.testItemTranslateCommandPlayer(false);
		testPlayerCommands.testItemTranslateCommandPlayerOther(true);
		testPlayerCommands.testItemTranslateCommandPlayerOther(false);
		testPlayerCommands.testItemTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testItemTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testEntityTranslateCommandPlayer(true);
		testPlayerCommands.testEntityTranslateCommandPlayer(false);
		testPlayerCommands.testEntityTranslateCommandPlayerOther(true);
		testPlayerCommands.testEntityTranslateCommandPlayerOther(false);
		testPlayerCommands.testEntityTranslateCommandPlayerOtherButSamePlayer(true);
		testPlayerCommands.testEntityTranslateCommandPlayerOtherButSamePlayer(false);
		testPlayerCommands.testRateLimitTranslateCommandPlayer(true);
		testPlayerCommands.testRateLimitTranslateCommandPlayer(false);
		testPlayerCommands.testRateLimitTranslateCommandPlayerOther(true);
		testPlayerCommands.testRateLimitTranslateCommandPlayerOther(false);
		testPlayerCommands.testRateLimitTranslateCommandOtherButSamePlayer(true);
		testPlayerCommands.testRateLimitTranslateCommandOtherButSamePlayer(false);

		/* Incorrect inputs, expect correct outputs regardless */
		// TODO
		
		/* Print finished message */
		sendCompletedMessage();
	}

	/* Console Command Tests */
	@Order(2)
	@Test
	public void testConsoleCommands() {
		/* Reset */
		resetWWC();
		
		/* Print start message */
		plugin.getLogger().info("=== Test Console Commands ===");
		
		/* Run tests */
		testConsoleCommands.testTranslateCommandConsoleTargetOther();
		testConsoleCommands.testTranslateCommandConsoleSourceTargetOther();
		testConsoleCommands.testTranslateCommandConsoleStopOther();
		testConsoleCommands.testGlobalTranslateCommandPlayerTarget();
		testConsoleCommands.testGlobalTranslateCommandPlayerSourceTarget();
		
		/* Print finished message */
		sendCompletedMessage();
	}
	
	/* GUI Tests */
	@Order(3)
	@Test
	public void testPlayerGUI() {
		/* Reset */
		resetWWC();

		/* Print start message */
		plugin.getLogger().info("=== Test Player GUIs ===");

		/* Run tests */
		testPlayerGUI.testTranslateCommandPlayerGUI();
		testPlayerGUI.testTranslateCommandPlayerGUIActive();
		testPlayerGUI.testTranslateCommandPlayerGUIOther();
		testPlayerGUI.testTranslateCommandPlayerGUIOtherActive();
		testPlayerGUI.testGlobalTranslateCommandPlayerGUI();
		testPlayerGUI.testGlobalTranslateCommandPlayerGUIActive();
		testPlayerGUI.testConfigurationCommandPlayerGUI();

		/* Print finished message */
		sendCompletedMessage();
	}

	/* Util Tests */
	@Order(4)
	@Test
	public void testUtils() {
		/* Reset */
		//resetWWC();

		/* Print start message */
		plugin.getLogger().info("=== Test Internal Utilities ===");

		/* Run tests */
		testTranslationUtils.testTranslationFunctionSourceTarget();
		testTranslationUtils.testTranslationFunctionSourceTargetOther();
		testTranslationUtils.testTranslationFunctionTarget();
		testTranslationUtils.testTranslationFunctionTargetOther();
		//testTranslationUtils.testPluginDataRetention();

		/* Print finished message */
		sendCompletedMessage();
	}
}