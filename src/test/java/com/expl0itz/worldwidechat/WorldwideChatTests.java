package com.expl0itz.worldwidechat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.expl0itz.worldwidechat.commands.TestPlayerCommands;
import com.expl0itz.worldwidechat.inventory.TestPlayerGUI;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldwideChatTests {
	private static ServerMock server;
	private static WorldwideChat plugin;
	private static PlayerMock playerMock;
	private static PlayerMock secondPlayerMock;
	private static PlayerMock thirdPlayerMock;
	private static PlayerMock fourthPlayerMock;

	/* Init all test classes */
	TestPlayerCommands testPlayerCommands = new TestPlayerCommands(server, plugin, playerMock, secondPlayerMock, thirdPlayerMock, fourthPlayerMock);
	TestPlayerGUI testPlayerGUI = new TestPlayerGUI(server, plugin, playerMock, secondPlayerMock);
	
	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = (WorldwideChat) MockBukkit.load(WorldwideChat.class);
		playerMock = server.addPlayer();
		playerMock.setName("player1");
		secondPlayerMock = server.addPlayer();
		secondPlayerMock.setName("player2");
		thirdPlayerMock = server.addPlayer();
		thirdPlayerMock.setName("player3");
		fourthPlayerMock = server.addPlayer();
		fourthPlayerMock.setName("player4");
		
		/* Add perms */
		playerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcg", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctb", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctb.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcts", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcts.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcti", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcti.otherplayers", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwctrl", true);
		playerMock.addAttachment(plugin, "worldwidechat.wwcs", true);
		secondPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		secondPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		thirdPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		thirdPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
		fourthPlayerMock.addAttachment(plugin, "worldwidechat.wwct", true);
		fourthPlayerMock.addAttachment(plugin, "worldwidechat.wwct.otherplayers", true);
	}

	@AfterAll
	public static void tearDown() {
		plugin.onDisable();
		MockBukkit.unmock();
	}
	
	public void resetWWC() {
		plugin.getActiveTranslators().clear();
		plugin.getPlayerRecords().clear();
		plugin.getCache().clear();
		reloadWWC();
	}
	
	public void reloadWWC() {
		plugin.cancelBackgroundTasks();
		plugin.loadPluginConfigs();
	}
	
	/* Command Tests */
	@Order(1)
	@Test
	public void testPlayerCommands() {
		/* Run tests */
		testPlayerCommands.testTranslateCommandPlayerSourceTarget();
		testPlayerCommands.testTranslateCommandPlayerSourceTargetOther();
	    testPlayerCommands.testTranslateCommandPlayerTarget();
	    testPlayerCommands.testTranslateCommandPlayerTargetOther();
	    testPlayerCommands.testGlobalTranslateCommandPlayer();
	    testPlayerCommands.testBookTranslateCommandPlayer();
	    testPlayerCommands.testBookTranslateCommandPlayerOther();
	    testPlayerCommands.testSignTranslateCommandPlayer();
	    testPlayerCommands.testSignTranslateCommandPlayerOther();
	    testPlayerCommands.testItemTranslateCommandPlayer();
	    testPlayerCommands.testItemTranslateCommandPlayerOther();
	    testPlayerCommands.testRateLimitTranslateCommandPlayer();
	    testPlayerCommands.testRateLimitTranslateCommandPlayerOther();
	    testPlayerCommands.testStatsCommandPlayer();
	    testPlayerCommands.testStatsCommandPlayerOther();
	}
	
	/* GUI Tests */
	@Order(2)
	@Test
	public void testPlayerGUI() {
		/* Reset Translators */
	    resetWWC();
		
		/* Run tests */
		testPlayerGUI.testTranslateCommandPlayerGUI();
		testPlayerGUI.testTranslateCommandPlayerGUIActive();
		testPlayerGUI.testTranslateCommandPlayerGUIOther();
		testPlayerGUI.testTranslateCommandPlayerGUIOtherActive();
		testPlayerGUI.testGlobalTranslateCommandPlayerGUI();
		testPlayerGUI.testGlobalTranslateCommandPlayerGUIActive();
	}
}
