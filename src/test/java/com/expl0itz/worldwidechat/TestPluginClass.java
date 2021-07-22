package com.expl0itz.worldwidechat;

import org.bukkit.command.CommandSender;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.expl0itz.worldwidechat.commands.WWCTranslate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestPluginClass {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	
	@BeforeEach
	public void setUp() {
		server = MockBukkit.mock();
		plugin = (WorldwideChat) MockBukkit.load(WorldwideChat.class);
		playerMock = server.addPlayer();
	}
	
	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	@Test
    public void testPlayerStartTranslate() {
    	String[] args = {"en", "es"};
    	WWCTranslate translateInstance = new WWCTranslate((CommandSender)playerMock, null, null, args);
        translateInstance.processCommand(false);
    }
	
}
