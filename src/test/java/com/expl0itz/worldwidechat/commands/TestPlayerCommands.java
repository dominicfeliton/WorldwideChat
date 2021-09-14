package com.expl0itz.worldwidechat.commands;

import static org.junit.Assert.assertTrue;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestPlayerCommands {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;

	public TestPlayerCommands(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
	}

	public void testTranslateCommandPlayerSourceTarget() {
		/* User runs /wwct en es */
		playerMock.performCommand("worldwidechat:wwct en es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));
	}

	public void testTranslateCommandPlayerSourceTargetOther() {
		/* User runs /wwct player2 en es */
		playerMock.performCommand("worldwidechat:wwct player2 en es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));
	}

	public void testTranslateCommandPlayerTarget() {
		/* User runs /wwct es */
		playerMock.performCommand("worldwidechat:wwct es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getOutLangCode().equals("es"));
	}

	public void testTranslateCommandPlayerTargetOther() {
		/* User runs /wwct player4 fr */
		playerMock.performCommand("worldwidechat:wwct player2 fr");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		assertTrue(currTranslator.getOutLangCode().equals("fr"));
	}

	public void testTranslateCommandSamePlayerTarget() {
		/* User runs /wwct player1 fr */
		playerMock.performCommand("worldwidechat:wwct player1 fr");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getOutLangCode().equals("fr"));
	}
	
	public void testTranslateCommandSamePlayerSourceTarget() {
		/* User runs /wwct player1 fr es */
		playerMock.performCommand("worldwidechat:wwct player1 fr es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		assertTrue(currTranslator.getInLangCode().equals("fr") && currTranslator.getOutLangCode().equals("es"));
	}
	
	public void testGlobalTranslateCommandPlayerSourceTarget() {
		/* User runs /wwcg en es */
		playerMock.performCommand("worldwidechat:wwcg en es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getInLangCode().equals("en") && currTranslator.getOutLangCode().equals("es"));
	}

	public void testGlobalTranslateCommandPlayerTarget() {
		/* User runs /wwcg es */
		playerMock.performCommand("worldwidechat:wwcg es");
		ActiveTranslator currTranslator = plugin.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
		assertTrue(currTranslator.getOutLangCode().equals("es"));
	}

	public void testBookTranslateCommandPlayer(boolean toggleStatus) {
		/* User runs /wwctb */
		playerMock.performCommand("worldwidechat:wwctb");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingBook());
		} else {
			assertTrue(!currTranslator.getTranslatingBook());
		}
	}

	public void testBookTranslateCommandPlayerOther(boolean toggleStatus) {
		/* User runs /wwctb player2 */
		playerMock.performCommand("worldwidechat:wwctb player2");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingBook());
		} else {
			assertTrue(!currTranslator.getTranslatingBook());
		}
	}
	
	public void testBookTranslateCommandPlayerOtherButSamePlayer(boolean toggleStatus) {
		/* User runs /wwctb player */
		playerMock.performCommand("worldwidechat:wwctb player1");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingBook());
		} else {
			assertTrue(!currTranslator.getTranslatingBook());
		}
	}

	public void testSignTranslateCommandPlayer(boolean toggleStatus) {
		/* User runs /wwcts */
		playerMock.performCommand("worldwidechat:wwcts");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingSign());
		} else {
			assertTrue(!currTranslator.getTranslatingSign());
		}
	}

	public void testSignTranslateCommandPlayerOther(boolean toggleStatus) {
		/* User runs /wwcts player2 */
		playerMock.performCommand("worldwidechat:wwcts player2");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingSign());
		} else {
			assertTrue(!currTranslator.getTranslatingSign());
		}
	}
	
	public void testSignTranslateCommandPlayerOtherButSamePlayer(boolean toggleStatus) {
		/* User runs /wwcts player */
		playerMock.performCommand("worldwidechat:wwcts player1");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingSign());
		} else {
			assertTrue(!currTranslator.getTranslatingSign());
		}
	}

	public void testItemTranslateCommandPlayer(boolean toggleStatus) {
		/* User runs /wwcti */
		playerMock.performCommand("worldwidechat:wwcti");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingItem());
		} else {
			assertTrue(!currTranslator.getTranslatingItem());
		}
	}

	public void testItemTranslateCommandPlayerOther(boolean toggleStatus) {
		/* User runs /wwcti player2 */
		playerMock.performCommand("worldwidechat:wwcti player2");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingItem());
		} else {
			assertTrue(!currTranslator.getTranslatingItem());
		}
	}
	
	public void testItemTranslateCommandPlayerOtherButSamePlayer(boolean toggleStatus) {
		/* User runs /wwcti player */
		playerMock.performCommand("worldwidechat:wwcti player1");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingItem());
		} else {
			assertTrue(!currTranslator.getTranslatingItem());
		}
	}
	
	public void testEntityTranslateCommandPlayer(boolean toggleStatus) {
		/* User runs /wwcte */
		playerMock.performCommand("worldwidechat:wwcte");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingEntity());
		} else {
			assertTrue(!currTranslator.getTranslatingEntity());
		}
	}

	public void testEntityTranslateCommandPlayerOther(boolean toggleStatus) {
		/* User runs /wwcte player2 */
		playerMock.performCommand("worldwidechat:wwcte player2");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingEntity());
		} else {
			assertTrue(!currTranslator.getTranslatingEntity());
		}
	}
	
	public void testEntityTranslateCommandPlayerOtherButSamePlayer(boolean toggleStatus) {
		/* User runs /wwcte player */
		playerMock.performCommand("worldwidechat:wwcte player1");
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (toggleStatus) {
			assertTrue(currTranslator.getTranslatingEntity());
		} else {
			assertTrue(!currTranslator.getTranslatingEntity());
		}
	}

	public void testRateLimitTranslateCommandPlayer(boolean isEnabled) {
		/* User runs /wwctrl */
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (isEnabled) {
			playerMock.performCommand("worldwidechat:wwctrl 5");
			assertTrue(currTranslator.getRateLimit() == 5);
		} else {
			playerMock.performCommand("worldwidechat:wwctrl");
			assertTrue(currTranslator.getRateLimit() == 0);
		}
	}

	public void testRateLimitTranslateCommandPlayerOther(boolean isEnabled) {
		/* User runs /wwctrl player2 */
		ActiveTranslator currTranslator = plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString());
		if (isEnabled) {
			playerMock.performCommand("worldwidechat:wwctrl player2 5");
			assertTrue(currTranslator.getRateLimit() == 5);
		} else {
			playerMock.performCommand("worldwidechat:wwctrl player2");
			assertTrue(currTranslator.getRateLimit() == 0);
		}
	}
	
	public void testRateLimitTranslateCommandOtherButSamePlayer(boolean isEnabled) {
		/* User runs /wwctrl player */
		ActiveTranslator currTranslator = plugin.getActiveTranslator(playerMock.getUniqueId().toString());
		if (isEnabled) {
			playerMock.performCommand("worldwidechat:wwctrl player1 5");
			assertTrue(currTranslator.getRateLimit() == 5);
		} else {
			playerMock.performCommand("worldwidechat:wwctrl player1");
			assertTrue(currTranslator.getRateLimit() == 0);
		}
	}
}