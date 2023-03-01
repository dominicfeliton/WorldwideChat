package com.badskater0729.worldwidechat.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badskater0729.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.badskater0729.worldwidechat.util.ActiveTranslator;

public class TestCommands {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;

	public TestCommands(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
	}
	
    public void runPlayerTest(String inCommand, PlayerMock player1) {
		// Perform player command
		server.getLogger().info("RUNNING TEST::: " + inCommand);
    	player1.performCommand(inCommand);
	}
    
    public void runConsoleTest(String inCommand, String[] args, PlayerMock player1) {
		// Perform console command
		server.getLogger().info("RUNNING TEST::: " + inCommand + " " + args.toString());
    	server.executeConsole(inCommand, args);
	}
}