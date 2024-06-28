package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.dominicfeliton.worldwidechat.WorldwideChatTests;

import static org.junit.jupiter.api.Assertions.*;

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

	public void testPluginDataRetentionYAML() {
		// YAML
		main.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
		main.getConfigManager().getMainConfig().set("Storage.useSQL", false);
		main.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
		main.getConfigManager().saveMainConfig(false);

		WorldwideChatTests.reloadWWC();

		server.getLogger().info("(YAML) RUNNING DATA RETENTION TEST: wwct en fr");
		playerMock.performCommand("worldwidechat:wwct en fr");
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.performCommand("worldwidechat:wwcte");
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.performCommand("worldwidechat:wwctci");
		playerMock.performCommand("worldwidechat:wwcl az");
		main.addCacheTerm(new CachedTranslation("en", "es", "test!"), "prueba!");

		server.getLogger().info("(YAML) RUNNING DATA RETENTION TEST: wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwctco");

		// Get stats initialized
		refs.translateText("Hello, how are you?", playerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);

		int beforeReloadCount = plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations();
		refs.translateText("Hello, how are you?", secondPlayerMock);

		WorldwideChatTests.reloadWWC();

		// Verify ActiveTranslators
		ActiveTranslator activeTrans1 = plugin.getActiveTranslator(playerMock);
		ActiveTranslator activeTrans2 = plugin.getActiveTranslator(secondPlayerMock);

		assertTrue(activeTrans1.getInLangCode().equals("en")
				&& activeTrans1.getOutLangCode().equals("fr"));
		assertTrue(activeTrans1.getTranslatingBook());
		assertTrue(activeTrans1.getTranslatingEntity());
		assertTrue(activeTrans1.getTranslatingSign());
		assertTrue(activeTrans1.getTranslatingItem());
		assertEquals(activeTrans1.getRateLimit(), 5);
		assertTrue(activeTrans1.getTranslatingChatOutgoing());
		assertTrue(activeTrans1.getTranslatingChatIncoming());

		assertTrue(activeTrans2.getInLangCode().equals("en")
				&& activeTrans2.getOutLangCode().equals("es"));
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingEntity());
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingItem());
		assertEquals(activeTrans2.getRateLimit(), 0);
		assertFalse(activeTrans2.getTranslatingChatOutgoing());
		assertFalse(activeTrans2.getTranslatingChatIncoming());

		// Verify PlayerRecords
		PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
		PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

		assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getUUID(), activeTrans2.getUUID());
		assertNotEquals("None", playerRecord2.getLastTranslationTime());
		assertTrue(playerRecord2.getHasBeenSaved());

		assertTrue(playerRecord1.getAttemptedTranslations() > 0);
		assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
		assertEquals(playerRecord1.getUUID(), activeTrans1.getUUID());
		assertNotEquals("None", playerRecord1.getLastTranslationTime());
		assertTrue(playerRecord1.getLocalizationCode().equals("az"));
		assertTrue(playerRecord1.getHasBeenSaved());

		// Verify Cache
		assertTrue(main.getCache().estimatedSize() > 1);
		assertTrue(main.getCacheTerm(new CachedTranslation("en", "es", "test!")).equals("prueba!"));
	}

	public void testPluginDataRetentionMongoDB() {
		// Switch to Mongo
		main.getConfigManager().getMainConfig().set("Storage.useMongoDB", true);
		main.getConfigManager().getMainConfig().set("Storage.useSQL", false);
		main.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
		main.getConfigManager().saveMainConfig(false);

		WorldwideChatTests.reloadWWC();

		assertTrue(main.isMongoConnValid(true));
		server.getLogger().info("(MongoDB) RUNNING DATA RETENTION TEST: wwct en fr");
		playerMock.performCommand("worldwidechat:wwct en fr");
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.performCommand("worldwidechat:wwcte");
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.performCommand("worldwidechat:wwctci");
		playerMock.performCommand("worldwidechat:wwcl az");
		main.addCacheTerm(new CachedTranslation("en", "es", "test!"), "prueba!");

		server.getLogger().info("(MongoDB) RUNNING DATA RETENTION TEST: wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwctco");

		// Get stats initialized
		refs.translateText("Hello, how are you?", playerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);

		int beforeReloadCount = plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations();
		refs.translateText("Hello, how are you?", secondPlayerMock);

        WorldwideChatTests.reloadWWC();

		// Verify ActiveTranslators
		ActiveTranslator activeTrans1 = plugin.getActiveTranslator(playerMock);
		ActiveTranslator activeTrans2 = plugin.getActiveTranslator(secondPlayerMock);
		assertTrue(main.isMongoConnValid(true));

		assertTrue(activeTrans1.getInLangCode().equals("en")
				&& activeTrans1.getOutLangCode().equals("fr"));
		assertTrue(activeTrans1.getTranslatingBook());
		assertTrue(activeTrans1.getTranslatingEntity());
		assertTrue(activeTrans1.getTranslatingSign());
		assertTrue(activeTrans1.getTranslatingItem());
		assertEquals(activeTrans1.getRateLimit(), 5);
		assertTrue(activeTrans1.getTranslatingChatOutgoing());
		assertTrue(activeTrans1.getTranslatingChatIncoming());

		assertTrue(activeTrans2.getInLangCode().equals("en")
				&& activeTrans2.getOutLangCode().equals("es"));
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingEntity());
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingItem());
		assertEquals(activeTrans2.getRateLimit(), 0);
		assertFalse(activeTrans2.getTranslatingChatOutgoing());
		assertFalse(activeTrans2.getTranslatingChatIncoming());

		// Verify PlayerRecords
		PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
		PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

        assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getUUID(), activeTrans2.getUUID());
        assertNotEquals("None", playerRecord2.getLastTranslationTime());
		assertTrue(playerRecord2.getHasBeenSaved());

		assertTrue(playerRecord1.getAttemptedTranslations() > 0);
		assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
		assertEquals(playerRecord1.getUUID(), activeTrans1.getUUID());
		assertNotEquals("None", playerRecord1.getLastTranslationTime());
		assertTrue(playerRecord1.getLocalizationCode().equals("az"));
		assertTrue(playerRecord1.getHasBeenSaved());

		// Verify Cache
		assertTrue(main.getCache().estimatedSize() > 1);
		assertTrue(main.getCacheTerm(new CachedTranslation("en", "es", "test!")).equals("prueba!"));
	}

	public void testPluginDataRetentionSQL() {
		// Switch to SQL
		main.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
		main.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
		main.getConfigManager().getMainConfig().set("Storage.useSQL", true);
		main.getConfigManager().saveMainConfig(false);

		WorldwideChatTests.reloadWWC();

		assertTrue(main.isSQLConnValid(true));
		server.getLogger().info("(SQL) RUNNING DATA RETENTION TEST: wwct en fr");
		playerMock.performCommand("worldwidechat:wwct en fr");
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.performCommand("worldwidechat:wwcte");
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.performCommand("worldwidechat:wwctci");
		playerMock.performCommand("worldwidechat:wwcl az");
		main.addCacheTerm(new CachedTranslation("en", "es", "test!"), "prueba!");

		server.getLogger().info("(SQL) RUNNING DATA RETENTION TEST: wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwctco");

		// Get stats initialized
		refs.translateText("Hello, how are you?", playerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);

		int beforeReloadCount = plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations();
		refs.translateText("Hello, how are you?", secondPlayerMock);

		WorldwideChatTests.reloadWWC();

		// Verify ActiveTranslators
		ActiveTranslator activeTrans1 = plugin.getActiveTranslator(playerMock);
		ActiveTranslator activeTrans2 = plugin.getActiveTranslator(secondPlayerMock);
		assertTrue(main.isSQLConnValid(true));

		assertTrue(activeTrans1.getInLangCode().equals("en")
				&& activeTrans1.getOutLangCode().equals("fr"));
		assertTrue(activeTrans1.getTranslatingBook());
		assertTrue(activeTrans1.getTranslatingEntity());
		assertTrue(activeTrans1.getTranslatingSign());
		assertTrue(activeTrans1.getTranslatingItem());
		assertEquals(activeTrans1.getRateLimit(), 5);
		assertTrue(activeTrans1.getTranslatingChatOutgoing());
		assertTrue(activeTrans1.getTranslatingChatIncoming());

		assertTrue(activeTrans2.getInLangCode().equals("en")
				&& activeTrans2.getOutLangCode().equals("es"));
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingEntity());
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingItem());
		assertEquals(activeTrans2.getRateLimit(), 0);
		assertFalse(activeTrans2.getTranslatingChatOutgoing());
		assertFalse(activeTrans2.getTranslatingChatIncoming());

		// Verify PlayerRecords
		PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
		PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

		assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getUUID(), activeTrans2.getUUID());
		assertNotEquals("None", playerRecord2.getLastTranslationTime());
		assertTrue(playerRecord2.getHasBeenSaved());

		assertTrue(playerRecord1.getAttemptedTranslations() > 0);
		assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
		assertEquals(playerRecord1.getUUID(), activeTrans1.getUUID());
		assertNotEquals("None", playerRecord1.getLastTranslationTime());
		assertTrue(playerRecord1.getLocalizationCode().equals("az"));
		assertTrue(playerRecord1.getHasBeenSaved());

		// Verify Cache
		assertTrue(main.getCache().estimatedSize() > 1);
		assertTrue(main.getCacheTerm(new CachedTranslation("en", "es", "test!")).equals("prueba!"));
	}

	public void testPluginDataRetentionPostgres() {
		// Switch to SQL
		main.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
		main.getConfigManager().getMainConfig().set("Storage.useSQL", false);
		main.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", true);
		main.getConfigManager().saveMainConfig(false);

		WorldwideChatTests.reloadWWC();

		assertTrue(main.isPostgresConnValid(true));
		server.getLogger().info("(Postgres) RUNNING DATA RETENTION TEST: wwct en fr");
		playerMock.performCommand("worldwidechat:wwct en fr");
		playerMock.performCommand("worldwidechat:wwctb");
		playerMock.performCommand("worldwidechat:wwcti");
		playerMock.performCommand("worldwidechat:wwcts");
		playerMock.performCommand("worldwidechat:wwcte");
		playerMock.performCommand("worldwidechat:wwctrl 5");
		playerMock.performCommand("worldwidechat:wwctci");
		playerMock.performCommand("worldwidechat:wwcl az");
		main.addCacheTerm(new CachedTranslation("en", "es", "test!"), "prueba!");

		server.getLogger().info("(Postgres) RUNNING DATA RETENTION TEST: wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwct en es");
		secondPlayerMock.performCommand("worldwidechat:wwctco");

		// Get stats initialized
		refs.translateText("Hello, how are you?", playerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);
		refs.translateText("Hello, how are you?", secondPlayerMock);

		int beforeReloadCount = plugin.getPlayerRecord(secondPlayerMock, false).getAttemptedTranslations();
		refs.translateText("Hello, how are you?", secondPlayerMock);

		WorldwideChatTests.reloadWWC();

		// Verify ActiveTranslators
		ActiveTranslator activeTrans1 = plugin.getActiveTranslator(playerMock);
		ActiveTranslator activeTrans2 = plugin.getActiveTranslator(secondPlayerMock);
		assertTrue(main.isPostgresConnValid(true));

		assertTrue(activeTrans1.getInLangCode().equals("en")
				&& activeTrans1.getOutLangCode().equals("fr"));
		assertTrue(activeTrans1.getTranslatingBook());
		assertTrue(activeTrans1.getTranslatingEntity());
		assertTrue(activeTrans1.getTranslatingSign());
		assertTrue(activeTrans1.getTranslatingItem());
		assertEquals(activeTrans1.getRateLimit(), 5);
		assertTrue(activeTrans1.getTranslatingChatOutgoing());
		assertTrue(activeTrans1.getTranslatingChatIncoming());

		assertTrue(activeTrans2.getInLangCode().equals("en")
				&& activeTrans2.getOutLangCode().equals("es"));
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingEntity());
		assertFalse(activeTrans2.getTranslatingSign());
		assertFalse(activeTrans2.getTranslatingItem());
		assertEquals(activeTrans2.getRateLimit(), 0);
		assertFalse(activeTrans2.getTranslatingChatOutgoing());
		assertFalse(activeTrans2.getTranslatingChatIncoming());

		// Verify PlayerRecords
		PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
		PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

		assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
		assertEquals(playerRecord2.getUUID(), activeTrans2.getUUID());
		assertNotEquals("None", playerRecord2.getLastTranslationTime());
		assertTrue(playerRecord2.getHasBeenSaved());

		assertTrue(playerRecord1.getAttemptedTranslations() > 0);
		assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
		assertEquals(playerRecord1.getUUID(), activeTrans1.getUUID());
		assertNotEquals("None", playerRecord1.getLastTranslationTime());
		assertTrue(playerRecord1.getLocalizationCode().equals("az"));
		assertTrue(playerRecord1.getHasBeenSaved());

		// Verify Cache
		assertTrue(main.getCache().estimatedSize() > 1);
		assertTrue(main.getCacheTerm(new CachedTranslation("en", "es", "test!")).equals("prueba!"));
	}
}