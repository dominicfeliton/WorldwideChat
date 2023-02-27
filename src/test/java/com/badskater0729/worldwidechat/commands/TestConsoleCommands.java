package com.badskater0729.worldwidechat.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestConsoleCommands {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;

	public TestConsoleCommands(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
	}
	
	//TODO: Contribute console.hasPermission() to MockBukkit
	public void testTranslateCommandConsoleTargetOther() {
		/* Console runs /wwct player1 es */
		server.executeConsole("wwct", new String[] {"player1", "es"});
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getInLangCode().equals("None") && currTranslator.getOutLangCode().equals("es"));
	}
	
	public void testTranslateCommandConsoleSourceTargetOther() {
		/* Console runs /wwct player1 en es*/
		server.executeConsole("wwct", new String[] {"player1", "en", "es"});
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));
	}
	
	public void testTranslateCommandConsoleStopOther() {
		/* Console stops another player's active translation session */
		server.executeConsole("wwct", new String[] {"player1", "stop"});
		assertFalse(plugin.isActiveTranslator(playerMock));
	}
	
	public void testGlobalTranslateCommandPlayerSourceTarget() {
		/* Console runs /wwcg en es */
		server.executeConsole("wwcg", new String[] {"en", "es"});
		ActiveTranslator currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));
	}

	public void testGlobalTranslateCommandPlayerTarget() {
		/* Console runs /wwcg es */
		server.executeConsole("wwcg", new String[] {"es"});
		ActiveTranslator currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getOutLangCode().equals("es"));
	}
	
}