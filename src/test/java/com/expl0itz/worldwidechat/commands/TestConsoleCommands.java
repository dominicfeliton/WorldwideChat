package com.expl0itz.worldwidechat.commands;

import com.expl0itz.worldwidechat.WorldwideChat;

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
	
	public void testTranslateCommandConsoleTargetOther() {
		/* Console runs /wwct player1 es */
		
	}
	
	public void testTranslateCommandConsoleSourceTargetOther() {
		/* Console runs /wwct player1 en es */
	}
	
}
