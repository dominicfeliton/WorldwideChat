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
	/* Correct inputs, expect correct outputs */
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
	
	/* Incorrect inputs, expect correct outputs regardless */
	public void testTranslateCommandInvalidAutoLangValidPlayer() {
		/* Console runs /wwct player1 test1 test2 test3*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "badlang"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandInvalidTargetLangValidPlayer() {
		/* Console runs /wwct player1 test1 test2 test3*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "en", "badlang"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandInvalidSourceLangValidPlayer() {
		/* Console runs /wwct player1 test1 test2 test3*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "badlang", "es"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandInvalidLangsValidPlayer() {
		/* Console runs /wwct player1 test1 test2 test3*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "badlang", "badlang2"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandSameLangsValidPlayer() {
		/* Console runs /wwct player1 en en */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "en", "en"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandSameLangsDifferentNamesValidPlayer() {
		/* Console runs /wwct player1 en English*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "en", "English"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testTranslateCommandTooManyArgsValidPlayer() {
		/* Console runs /wwct player1 test1 test2 test3*/
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwct", new String[] {"player1", "en", "es", "test3"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testGlobalCommandInvalidAutoLang() {
		/* Console runs /wwcg en badlang */
		server.executeConsole("wwcg", new String[] {"stop"});
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"badlang"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
	public void testGlobalCommandInvalidOutLangValidInLang() {
		/* Console runs /wwcg en badlang */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"en", "badlang"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
	
    public void testGlobalCommandValidOutLangInvalidInLang() {
    	/* Console runs /wwcg badlang */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"badlang", "es"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
    
    public void testGlobalCommandInvalidLangs() {
    	/* Console runs /wwcg badlang badlang2 */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"badlang", "badlang2"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
    
    public void testGlobalCommandSameLangs() {
    	/* Console runs /wwcg en en */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"en", "en"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
    
    public void testGlobalCommandSameLangsDiffName() {
    	/* Console runs /wwcg English en */
		int numOfTranslators = plugin.getActiveTranslators().size();
		server.executeConsole("wwcg", new String[] {"English", "en"});
		assertTrue(plugin.getActiveTranslators().size() == numOfTranslators);
	}
}