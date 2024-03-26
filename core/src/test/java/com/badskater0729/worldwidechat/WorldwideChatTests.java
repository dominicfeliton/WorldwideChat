package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.util.ActiveTranslator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.badskater0729.worldwidechat.commands.TestCommands;
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
		// Player - /wwct en es
		testCommands.runPlayerTest("wwct en es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Player - /wwct player2 en es
		testCommands.runPlayerTest("wwct player2 en es", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Player - /wwct es
		testCommands.runPlayerTest("wwct es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals("es", currTranslator.getOutLangCode());

		// Player - /wwct player2 fr
		testCommands.runPlayerTest("wwct player2 fr", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals("fr", currTranslator.getOutLangCode());

		// Player - /wwct player1 fr es
		testCommands.runPlayerTest("wwct player1 fr es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("fr") && currTranslator.getOutLangCode().equals("es"));

		// Player - /wwct stop
		testCommands.runPlayerTest("wwct stop", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player2 stop
		testCommands.runPlayerTest("wwct player2 stop", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwcg en es
		testCommands.runPlayerTest("wwcg en es", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Player - /wwcg es
		testCommands.runPlayerTest("wwcg es", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertEquals("es", currTranslator.getOutLangCode());

		// Player - /wwcg stop
		testCommands.runPlayerTest("wwcg stop", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwctci
		// (Re-setup /wwct for player1)
		testCommands.runPlayerTest("wwct en es", player1);
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Player - /wwctci player2
		// (Re-setup /wwct for player2)
		testCommands.runPlayerTest("wwct player2 en es", player1);
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Player - /wwctci player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatIncoming();
		testCommands.runPlayerTest("wwctci player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatIncoming() != toggleStatus);

		// Player - /wwctco
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

		// Player - /wwctco player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

		// Player - /wwctco player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingChatOutgoing();
		testCommands.runPlayerTest("wwctco player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingChatOutgoing() != toggleStatus);

        // Player - /wwctb
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingBook();
		testCommands.runPlayerTest("wwctb", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Runs test comamnd /wwctb player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingBook();
		testCommands.runPlayerTest("wwctb player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Player - /wwctb player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingBook();
		testCommands.runPlayerTest("wwctb player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingBook() != toggleStatus);

		// Player - /wwcts
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingSign();
		testCommands.runPlayerTest("wwcts", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Player - /wwcts player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingSign();
		testCommands.runPlayerTest("wwcts player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Player - /wwcts player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingSign();
		testCommands.runPlayerTest("wwcts player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingSign() != toggleStatus);

		// Player - /wwcti
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingItem();
		testCommands.runPlayerTest("wwcti", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Player - /wwcti player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingItem();
		testCommands.runPlayerTest("wwcti player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Player - /wwcti player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingItem();
		testCommands.runPlayerTest("wwcti player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingItem() != toggleStatus);

		// Player - /wwcte
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Player - /wwcte player2
		toggleStatus = plugin.getActiveTranslator(player2).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Player - /wwcte player1
		toggleStatus = plugin.getActiveTranslator(player1).getTranslatingEntity();
		testCommands.runPlayerTest("wwcte player1", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getTranslatingEntity() != toggleStatus);

		// Player - /wwctrl (random number between 1 and 10)
		int randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Player - /wwctrl player2 (random number between 1 and 10)
		randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl player2 " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Player - /wwctrl player1 (random number between 1 and 10)
		randomLimit = new Random().nextInt(10) + 1;
		testCommands.runPlayerTest("wwctrl player1 " + randomLimit, player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), randomLimit);

		// Player - /wwctrl
		testCommands.runPlayerTest("wwctrl", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertEquals(currTranslator.getRateLimit(), 0);

		// Player - /wwctrl player2
		testCommands.runPlayerTest("wwctrl player2", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertEquals(currTranslator.getRateLimit(), 0);

		/* Incorrect inputs, expect correct outputs regardless */
		// Player - /wwct badlang
		// Stop all existing translators before running tests
		resetWWC();
		testCommands.runPlayerTest("wwct badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct badlang badlang
		testCommands.runPlayerTest("wwct badlang badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct en badlang
		testCommands.runPlayerTest("wwct en badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct badlang en
		testCommands.runPlayerTest("wwct badlang en", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct en es es
		testCommands.runPlayerTest("wwct en es es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct en es en badarg1 badarg2 badarg3 en es
		testCommands.runPlayerTest("wwct en es en badarg1 badarg2 badarg3 en es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player1 badlang
		testCommands.runPlayerTest("wwct player1 badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player1 badlang badlang
		testCommands.runPlayerTest("wwct player1 badlang badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player1 en badlang
		testCommands.runPlayerTest("wwct player1 en badlang", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player1 badlang en
		testCommands.runPlayerTest("wwct player1 badlang en", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player1 en es es
		testCommands.runPlayerTest("wwct player1 en es es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwct player2 badlang
		testCommands.runPlayerTest("wwct player2 badlang", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwct player2 badlang badlang
		testCommands.runPlayerTest("wwct player2 badlang badlang", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwct player2 en badlang
		testCommands.runPlayerTest("wwct player2 en badlang", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwct player2 badlang en
		testCommands.runPlayerTest("wwct player2 badlang en", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwct player2 en es es
		testCommands.runPlayerTest("wwct player2 en es es", player1);
		currTranslator = plugin.getActiveTranslator(player2);
		assertFalse(plugin.isActiveTranslator(player2));

		// Player - /wwct player1 player2
		testCommands.runPlayerTest("wwct player1 player2", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwcg badlang
		testCommands.runPlayerTest("wwcg badlang", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwcg badlang badlang
		testCommands.runPlayerTest("wwcg badlang badlang", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwcg en badlang
		testCommands.runPlayerTest("wwcg en badlang", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwcg badlang en
		testCommands.runPlayerTest("wwcg badlang en", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwcg en en en
		testCommands.runPlayerTest("wwcg en en en", player1);
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Player - /wwcg player1 en
		testCommands.runPlayerTest("wwcg player1 en", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Player - /wwcg player1 en es
		testCommands.runPlayerTest("wwcg player1 en es", player1);
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));
		
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
		// Console - /wwct player1 es
		testCommands.runConsoleTest("wwct", new String[] {"player1", "es"});
		ActiveTranslator currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("None") && currTranslator.getOutLangCode().equals("es"));

		// Console - /wwct player1 es en
		testCommands.runConsoleTest("wwct", new String[] {"player1", "es", "en"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertTrue(currTranslator.getInLangCode().equals("es") && currTranslator.getOutLangCode().equals("en"));

		// Console - /wwct player1 stop
		testCommands.runConsoleTest("wwct", new String[] {"player1", "stop"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));
		
		// Console - /wwcg en es
		testCommands.runConsoleTest("wwcg", new String[] {"en", "es"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));

		// Console - /wwcg es
		testCommands.runConsoleTest("wwcg", new String[] {"es"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("None") && currTranslator.getOutLangCode().equals("es"));

		// Console - /wwcg stop
		testCommands.runConsoleTest("wwcg", new String[] {"stop"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
		
		/* Incorrect inputs, expect correct outputs regardless */
		// Console - /wwct player1 badlang (Ensure that player1 is stopped first)
		resetWWC();
		testCommands.runConsoleTest("wwct", new String[] {"player1", "badlang"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 en badlang
		testCommands.runConsoleTest("wwct", new String[] {"player1", "en", "badlang"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 badlang es
		testCommands.runConsoleTest("wwct", new String[] {"player1", "badlang", "es"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 badlang badlang2
		testCommands.runConsoleTest("wwct", new String[] {"player1", "badlang", "badlang2"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 en en
		testCommands.runConsoleTest("wwct", new String[] {"player1", "en", "en"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 en English
		testCommands.runConsoleTest("wwct", new String[] {"player1", "en", "English"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwct player1 en es test3
		testCommands.runConsoleTest("wwct", new String[] {"player1", "en", "es", "test3"});
		currTranslator = plugin.getActiveTranslator(player1);
		assertFalse(plugin.isActiveTranslator(player1));

		// Console - /wwcg badlang
		testCommands.runConsoleTest("wwcg", new String[] {"badlang"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Console - /wwcg en badlang
		testCommands.runConsoleTest("wwcg", new String[] {"en", "badlang"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Console - /wwcg badlang badlang2
		testCommands.runConsoleTest("wwcg", new String[] {"badlang", "badlang2"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Console - /wwcg en en
		testCommands.runConsoleTest("wwcg", new String[] {"en", "en"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));

		// Console - /wwcg English en
		testCommands.runConsoleTest("wwcg", new String[] {"English", "en"});
		currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertFalse(plugin.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
		
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

		/* Run tests, only players can test GUIs */
		// User runs /wwct
		testCommands.runPlayerTest("wwct", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwct player1
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwct player1 en es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 en es", player1);
		testCommands.runPlayerTest("wwct", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct player1 es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 es", player1);
		testCommands.runPlayerTest("wwct", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct player1 en es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 en es", player1);
		testCommands.runPlayerTest("wwct player1", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct player1 en es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 es", player1);
		testCommands.runPlayerTest("wwct player1", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct player2, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player2", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwct player2 en es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player2 en es", player1);
		testCommands.runPlayerTest("wwct player2", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct player2 es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player2 es", player1);
		testCommands.runPlayerTest("wwct player2", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwct stop, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 en es", player1);
		testCommands.runPlayerTest("wwct stop", player1);
		testCommands.runPlayerTest("wwct", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwct player1 stop, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player1 en es", player1);
		testCommands.runPlayerTest("wwct player1 stop", player1);
		testCommands.runPlayerTest("wwct", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwct player2 stop, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwct player2 en es", player1);
		testCommands.runPlayerTest("wwct player2 stop", player1);
		testCommands.runPlayerTest("wwct player2", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwcg, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwcg", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwcg en es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwcg en es", player1);
		testCommands.runPlayerTest("wwcg", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwcg es, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwcg es", player1);
		testCommands.runPlayerTest("wwcg", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "GREEN_STAINED_GLASS_PANE");

		// User runs /wwcg stop, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwcg stop", player1);
		testCommands.runPlayerTest("wwcg", player1);
		assertEquals(player1.getOpenInventory().getItem(0).getType().name(), "WHITE_STAINED_GLASS_PANE");

		// User runs /wwcc, then opens GUI
		player1.closeInventory();
		testCommands.runPlayerTest("wwcc", player1);
		assertTrue(plugin.getInventoryManager().getInventory(player1).isPresent());

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
		// wwct testing
		testTranslationUtils.testTranslationFunctionSourceTarget();
		testTranslationUtils.testTranslationFunctionSourceTargetOther();
		testTranslationUtils.testTranslationFunctionTarget();
		testTranslationUtils.testTranslationFunctionTargetOther();

		// data retention testing
		testTranslationUtils.testPluginDataRetention();

		/* Print finished message */
		sendCompletedMessage();
	}
}