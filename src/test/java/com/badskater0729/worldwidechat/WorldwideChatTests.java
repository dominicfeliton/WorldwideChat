package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.util.ActiveTranslator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.badskater0729.worldwidechat.commands.TestConsoleCommands;
import com.badskater0729.worldwidechat.commands.TestCommands;
import com.badskater0729.worldwidechat.inventory.TestPlayerGUI;
import com.badskater0729.worldwidechat.util.TestTranslationUtils;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldwideChatTests {
	private static ServerMock server;
	private static WorldwideChat plugin;
	private static PlayerMock player1;
	private static PlayerMock player2;

	private static int testCount = 0;

	/* Init all test classes */
	TestCommands testCommands = new TestCommands(server, plugin, player1, player2);
	TestConsoleCommands testConsoleCommands = new TestConsoleCommands(server, plugin, player1, player2);
	TestPlayerGUI testPlayerGUI = new TestPlayerGUI(server, plugin, player1, player2);
	TestTranslationUtils testTranslationUtils = new TestTranslationUtils(server, plugin, player1, player2);
	ActiveTranslator currTranslator;

	boolean toggleStatus;
	@BeforeAll
	public static void setUp() {
		// Setup vars
		server = MockBukkit.mock();
		plugin = MockBukkit.load(WorldwideChat.class);
		player1 = server.addPlayer("player1");
		player2 = server.addPlayer("player2");

		/* Add perms */
		player1.setOp(true);
		player2.setOp(true);
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
		// Runs test command /wwct en es
		testCommands.runPlayerTest("wwct en es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Runs test command /wwct player2 en es
		testCommands.runPlayerTest("wwct player2 en es", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Runs test command /wwct es
		testCommands.runPlayerTest("wwct es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals("es", currTranslator.getOutLangCode());

		// Runs test command /wwct player2 fr
		testCommands.runPlayerTest("wwct player2 fr", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals("fr", currTranslator.getOutLangCode());

		// Runs test command /wwct player1 fr es
		testCommands.runPlayerTest("wwct player1 fr es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("fr") && currTranslator.getOutLangCode().equals("es"));

		// Runs test command /wwct stop
		testCommands.runPlayerTest("wwct stop", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Runs test command /wwct player2 stop
		testCommands.runPlayerTest("wwct player2 stop", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Runs test command /wwcg en es
		testCommands.runPlayerTest("wwcg en es", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Runs test command /wwcg es
		testCommands.runPlayerTest("wwcg es", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertEquals("es", currTranslator.getOutLangCode());

		// Runs test command /wwcg stop
		testCommands.runPlayerTest("wwcg stop", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Runs test command /wwctci
		// (Re-setup /wwct for player1)
		testCommands.runPlayerTest("wwct en es", player1);
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Runs test command /wwctci player2
		// (Re-setup /wwct for player2)
		testCommands.runPlayerTest("wwct player2 en es", player1);
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Runs test command /wwctci player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Runs test command /wwctco
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

		// Runs test command /wwctco player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

		// Runs test command /wwctco player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

        // Runs test command /wwctb
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingBook();
		testCommands.runPlayerTest("wwctb", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Runs test comamnd /wwctb player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingBook();
		testCommands.runPlayerTest("wwctb player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Runs test command /wwctb player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingBook();
		testCommands.runPlayerTest("wwctb player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Runs test command /wwcts
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingSign();
		testCommands.runPlayerTest("wwcts", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Runs test command /wwcts player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingSign();
		testCommands.runPlayerTest("wwcts player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Runs test command /wwcts player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingSign();
		testCommands.runPlayerTest("wwcts player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Runs test command /wwcti
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingItem();
		testCommands.runPlayerTest("wwcti", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Runs test command /wwcti player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingItem();
		testCommands.runPlayerTest("wwcti player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Runs test command /wwcti player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingItem();
		testCommands.runPlayerTest("wwcti player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Runs test command /wwcte
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Runs test command /wwcte player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Runs test command /wwcte player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Runs test command /wwctrl (random number between 1 and 10)
		int randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Runs test command /wwctrl player2 (random number between 1 and 10)
		randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl player2 " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Runs test command /wwctrl player1 (random number between 1 and 10)
		randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl player1 " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Runs test command /wwctrl
		testCommands.runPlayerTest("wwctrl", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), 0);

		// Runs test command /wwctrl player2
		testCommands.runPlayerTest("wwctrl player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals(currTranslator.getRateLimit(), 0);

		/* Incorrect inputs, expect correct outputs regardless */
		// TODO
		
		/* Print finished message */
		sendCompletedMessage();
	}

	/* Console Command Tests */
	@Order(2)
	@Test
	public void testConsoleCommands() {
		/* Print start message */
		plugin.getLogger().info("=== Test Console Commands ===");
		
		/* Reset */
		resetWWC();
		
		/* Correct inputs, expect correct outputs */
		testConsoleCommands.testTranslateCommandConsoleTargetOther();
		testConsoleCommands.testTranslateCommandConsoleSourceTargetOther();
		testConsoleCommands.testTranslateCommandConsoleStopOther();
		testConsoleCommands.testGlobalTranslateCommandPlayerTarget();
		testConsoleCommands.testGlobalTranslateCommandPlayerSourceTarget();
		
		/* Incorrect inputs, expect correct outputs regardless */
		testConsoleCommands.testTranslateCommandInvalidAutoLangValidPlayer();
		testConsoleCommands.testTranslateCommandInvalidTargetLangValidPlayer();
		testConsoleCommands.testTranslateCommandInvalidSourceLangValidPlayer();
		testConsoleCommands.testTranslateCommandInvalidLangsValidPlayer();
		testConsoleCommands.testTranslateCommandSameLangsValidPlayer();
		testConsoleCommands.testTranslateCommandTooManyArgsValidPlayer();
		testConsoleCommands.testGlobalCommandInvalidAutoLang();
		testConsoleCommands.testGlobalCommandInvalidOutLangValidInLang();
		testConsoleCommands.testGlobalCommandValidOutLangInvalidInLang();
		testConsoleCommands.testGlobalCommandInvalidLangs();
		testConsoleCommands.testGlobalCommandSameLangs();
		testConsoleCommands.testGlobalCommandSameLangsDiffName();
		
		/* Print finished message */
		sendCompletedMessage();
	}
	
	/* GUI Tests */
	@Order(3)
	@Test
	public void testPlayerGUI() {
		/* Print start message */
		plugin.getLogger().info("=== Test Player GUIs ===");
		
		/* Reset */
		resetWWC();

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
		/* Print start message */
		plugin.getLogger().info("=== Test Internal Utilities ===");
		
		/* Reset */
		resetWWC();

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