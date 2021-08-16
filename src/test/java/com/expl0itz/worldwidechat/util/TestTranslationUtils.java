package com.expl0itz.worldwidechat.util;

import static org.junit.Assert.assertTrue;

import com.expl0itz.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestTranslationUtils {
	
	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;

	public TestTranslationUtils(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
	}
	
	public void testTranslationFunctionSourceTarget() {
		playerMock.performCommand("worldwidechat:wwct en es");
		assertTrue(CommonDefinitions.translateText("Hello, how are you?", playerMock).equals("Hola, como estas?"));
	}
}
