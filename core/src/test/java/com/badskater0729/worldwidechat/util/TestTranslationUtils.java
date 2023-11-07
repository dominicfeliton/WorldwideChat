package com.badskater0729.worldwidechat.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badskater0729.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

public class TestTranslationUtils {

	private ServerMock server;
	private WorldwideChat plugin;
	private PlayerMock playerMock;
	private PlayerMock secondPlayerMock;

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	public TestTranslationUtils(ServerMock server, WorldwideChat plugin, PlayerMock p1, PlayerMock p2) {
		this.server = server;
		this.plugin = plugin;
		playerMock = p1;
		secondPlayerMock = p2;
	}

	public void testTranslationFunctionSourceTarget() {
		playerMock.performCommand("worldwidechat:wwct en es");
		assertTrue(refs.translateText("Hello, how are you?", playerMock).equals("Hola, como estas?"));
	}

	public void testTranslationFunctionTarget() {
		playerMock.performCommand("worldwidechat:wwct es");
		assertTrue(refs.translateText("How many diamonds do you have?", playerMock)
				.equals("Cuantos diamantes tienes?"));
	}

	public void testTranslationFunctionSourceTargetOther() {
		playerMock.performCommand("worldwidechat:wwct player2 en es");
		assertTrue(
				refs.translateText("Hello, how are you?", secondPlayerMock).equals("Hola, como estas?"));
	}

	public void testTranslationFunctionTargetOther() {
		playerMock.performCommand("worldwidechat:wwct player2 es");
		assertTrue(refs.translateText("How many diamonds do you have?", secondPlayerMock)
				.equals("Cuantos diamantes tienes?"));
	}



	public void testPluginDataRetention() {
		/*
		assertTrue(plugin.getActiveTranslator(playerMock.getUniqueId().toString()).getOutLangCode().equals("es")
				&& plugin.getPlayerRecord(playerMock.getUniqueId().toString(), false).getAttemptedTranslations() > 0);
		assertTrue(plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString()).getOutLangCode().equals("es")
				&& plugin.getPlayerRecord(secondPlayerMock.getUniqueId().toString(), false)
						.getAttemptedTranslations() > 0);
		playerMock.performCommand("worldwidechat:wwct en fr");
		secondPlayerMock.performCommand("worldwidechat:wwct es en");
		WorldwideChatTests.reloadWWC();
		assertTrue(plugin.getActiveTranslator(playerMock.getUniqueId().toString()).getInLangCode().equals("en")
				&& plugin.getActiveTranslator(playerMock.getUniqueId().toString()).getOutLangCode().equals("fr"));
		assertTrue(plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString()).getInLangCode().equals("es")
				&& plugin.getActiveTranslator(secondPlayerMock.getUniqueId().toString()).getOutLangCode().equals("en"));
	    */
		
		//TODO: Rewrite
	}
}