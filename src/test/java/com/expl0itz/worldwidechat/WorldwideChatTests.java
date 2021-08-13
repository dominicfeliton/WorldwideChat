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

	/* Init all test classes */
	TestPlayerCommands testPlayerCommands = new TestPlayerCommands(server, plugin, playerMock, secondPlayerMock);
	TestPlayerGUI testPlayerGUI = new TestPlayerGUI(server, plugin, playerMock, secondPlayerMock);
	
	@BeforeAll
	public static void setUp() {
		server = MockBukkit.mock();
		plugin = (WorldwideChat) MockBukkit.load(WorldwideChat.class);
		playerMock = server.addPlayer();
		playerMock.setName("player1");
		secondPlayerMock = server.addPlayer();
		secondPlayerMock.setName("player2");
		
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
	}

	@AfterAll
	public static void tearDown() {
		MockBukkit.unmock();
	}
	
	public void resetWWC() {
		plugin.getActiveTranslators().clear();
		plugin.getPlayerRecords().clear();
		plugin.getCache().clear();
		plugin.reload(); 
		try {
			// Give the async thread a little bit to load before running more tests.
			// Normally, in game the user would just be given a message saying that the plugin is not done loading.
			// But since these tests don't like that output, we need this artificial delay on the server.
			// A race condition could occur where the 100 ms delay isn't enough for the async translator load process to finish,
			// But if the host CPU/Storage/RAM/whatever is so dogshit that it takes more than 100 ms to access a local, fake translator class,
			// We should get a better computer. Or raise the wait time if you want to deal with that kind of pain and misery.
			// Also synchronized block because we need to get the lock of the obj,
			// Otherwise IllegalMonitorStateException: current thread is not owner :(
			synchronized (server) {
				server.wait(100);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	    testPlayerCommands.testGlobalTranslateCommandPlayerSourceTarget();
	    testPlayerCommands.testGlobalTranslateCommandPlayerTarget();
	    testPlayerCommands.testBookTranslateCommandPlayer(true);
	    testPlayerCommands.testBookTranslateCommandPlayer(false);
	    testPlayerCommands.testBookTranslateCommandPlayerOther(true);
	    testPlayerCommands.testBookTranslateCommandPlayerOther(false);
	    testPlayerCommands.testSignTranslateCommandPlayer(true);
	    testPlayerCommands.testSignTranslateCommandPlayer(false);
	    testPlayerCommands.testSignTranslateCommandPlayerOther(true);
	    testPlayerCommands.testSignTranslateCommandPlayerOther(false);
	    testPlayerCommands.testItemTranslateCommandPlayer(true);
	    testPlayerCommands.testItemTranslateCommandPlayer(false);
	    testPlayerCommands.testItemTranslateCommandPlayerOther(true);
	    testPlayerCommands.testItemTranslateCommandPlayerOther(false);
	    testPlayerCommands.testRateLimitTranslateCommandPlayer(true);
	    testPlayerCommands.testRateLimitTranslateCommandPlayer(false);
	    testPlayerCommands.testRateLimitTranslateCommandPlayerOther(true);
	    testPlayerCommands.testRateLimitTranslateCommandPlayerOther(false);
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
		testPlayerGUI.testConfigurationCommandPlayerGUI();
	}
}
