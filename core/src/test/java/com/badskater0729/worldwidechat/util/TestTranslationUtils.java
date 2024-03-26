package com.badskater0729.worldwidechat.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badskater0729.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.badskater0729.worldwidechat.WorldwideChatTests;

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
		assertTrue(plugin.getActiveTranslator(playerMock).getOutLangCode().equals("es")
				&& plugin.getPlayerRecord(playerMock, false).getAttemptedTranslations() > 0);
		assertTrue(plugin.getActiveTranslator(secondPlayerMock).getOutLangCode().equals("es")
				&& plugin.getPlayerRecord(secondPlayerMock, false)
						.getAttemptedTranslations() > 0);

		server.getLogger().info("RUNNING DATA RETENTION TEST: wwct en fr");
		playerMock.performCommand("worldwidechat:wwct en fr");
		server.getLogger().info("RUNNING DATA RETENTION TEST: wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwct en es");
		int beforeReloadCount = plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations();
		assertTrue(refs.translateText("Hello, how are you?", secondPlayerMock).equals("Hola, como estas?"));

		WorldwideChatTests.reloadWWC();

		assertTrue(plugin.getActiveTranslator(playerMock).getInLangCode().equals("en")
				&& plugin.getActiveTranslator(playerMock).getOutLangCode().equals("fr"));
		assertTrue(plugin.getActiveTranslator(secondPlayerMock).getInLangCode().equals("en")
				&& plugin.getActiveTranslator(secondPlayerMock).getOutLangCode().equals("es"));
		assertTrue(plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations() == beforeReloadCount+1);
		assertTrue(plugin.getPlayerRecord(playerMock, false).getUUID().toString().equals(playerMock.getUniqueId().toString()));
	}
}