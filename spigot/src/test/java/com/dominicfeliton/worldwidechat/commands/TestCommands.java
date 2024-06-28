package com.dominicfeliton.worldwidechat.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dominicfeliton.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

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
    
    public void runConsoleTest(String inCommand, String[] args) {
		// Perform console command
		StringBuilder outStr = new StringBuilder();
		for (String eaArg : args) {
			outStr.append(eaArg).append(" ");
		}
		server.getLogger().info("RUNNING TEST::: " + inCommand + " " + outStr);
    	server.executeConsole(inCommand, args);
	}
}