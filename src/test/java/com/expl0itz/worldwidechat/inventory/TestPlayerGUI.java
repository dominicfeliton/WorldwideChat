package com.expl0itz.worldwidechat.inventory;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import com.expl0itz.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlayerGUI {
	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;

	public TestPlayerGUI(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
	}
	
	public void testTranslateCommandPlayerGUI() {
		/* User runs /wwct */
		playerMock.performCommand("worldwidechat:wwct");
		assertTrue(playerMock.getOpenInventory().getItem(0).getType().name().equals("WHITE_STAINED_GLASS_PANE"));
	}
	
	public void testTranslateCommandPlayerGUIActive() {
		/* User runs /wwct en es, then checks GUI */
		playerMock.performCommand("worldwidechat:wwct en es");
		playerMock.performCommand("worldwidechat:wwct");
		assertTrue(playerMock.getOpenInventory().getItem(0).getType().name().equals("GREEN_STAINED_GLASS_PANE"));
	}
	
	public void testTranslateCommandPlayerGUIOther() {
		/* User runs /wwct player2 en es, then checks GUI */
		playerMock.performCommand("worldwidechat:wwct player2");
		assertTrue(playerMock.getOpenInventory().getItem(0).getType().name().equals("WHITE_STAINED_GLASS_PANE"));
	}
	
	public void testTranslateCommandPlayerGUIOtherActive() {
		/* User runs /wwct en es, then checks GUI */
		playerMock.performCommand("worldwidechat:wwct player2 en es");
		playerMock.performCommand("worldwidechat:wwct player2");
		assertTrue(playerMock.getOpenInventory().getItem(0).getType().name().equals("GREEN_STAINED_GLASS_PANE"));
	}
}
