package com.dominicfeliton.worldwidechat.util;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.dominicfeliton.worldwidechat.WorldwideChat;
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
        assertEquals("Hola, como estas?", refs.translateText("Hello, how are you?", playerMock));
    }

    public void testTranslationFunctionTarget() {
        playerMock.performCommand("worldwidechat:wwct es");
        assertEquals("Cuantos diamantes tienes?", refs.translateText("How many diamonds do you have?", playerMock));
    }

    public void testTranslationFunctionSourceTargetOther() {
        playerMock.performCommand("worldwidechat:wwct player2 en es");
        assertEquals("Hola, como estas?", refs.translateText("Hello, how are you?", secondPlayerMock));
    }

    public void testTranslationFunctionTargetOther() {
        playerMock.performCommand("worldwidechat:wwct player2 es");
        assertEquals("Cuantos diamantes tienes?", refs.translateText("How many diamonds do you have?", secondPlayerMock));
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
        ActiveTranslator currTranslator1 = plugin.getActiveTranslator(playerMock);
        ActiveTranslator currTranslator2 = plugin.getActiveTranslator(secondPlayerMock);

        assertTrue(currTranslator1.getInLangCode().equals("en")
                && currTranslator1.getOutLangCode().equals("fr"));
        assertTrue(currTranslator1.getTranslatingBook());
        assertTrue(currTranslator1.getTranslatingEntity());
        assertTrue(currTranslator1.getTranslatingSign());
        assertTrue(currTranslator1.getTranslatingItem());
        assertEquals(currTranslator1.getRateLimit(), 5);
        assertTrue(currTranslator1.getTranslatingChatOutgoing());
        assertTrue(currTranslator1.getTranslatingChatIncoming());

        assertTrue(currTranslator2.getInLangCode().equals("en")
                && currTranslator2.getOutLangCode().equals("es"));
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingEntity());
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingItem());
        assertEquals(currTranslator2.getRateLimit(), 0);
        assertFalse(currTranslator2.getTranslatingChatOutgoing());
        assertFalse(currTranslator2.getTranslatingChatIncoming());

        // Verify PlayerRecords
        PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
        PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

        assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getUUID(), currTranslator2.getUUID());
        assertNotEquals("None", playerRecord2.getLastTranslationTime());
        assertTrue(playerRecord2.getHasBeenSaved());

        assertTrue(playerRecord1.getAttemptedTranslations() > 0);
        assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
        assertEquals(playerRecord1.getUUID(), currTranslator1.getUUID());
        assertNotEquals("None", playerRecord1.getLastTranslationTime());
        assertEquals("az", playerRecord1.getLocalizationCode());
        assertTrue(playerRecord1.getHasBeenSaved());

        // Verify Cache
        assertTrue(main.getCache().estimatedSize() > 1);
        assertEquals("prueba!", main.getCacheTerm(new CachedTranslation("en", "es", "test!")));
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
        ActiveTranslator currTranslator1 = plugin.getActiveTranslator(playerMock);
        ActiveTranslator currTranslator2 = plugin.getActiveTranslator(secondPlayerMock);
        assertTrue(main.isMongoConnValid(true));

        assertTrue(currTranslator1.getInLangCode().equals("en")
                && currTranslator1.getOutLangCode().equals("fr"));
        assertTrue(currTranslator1.getTranslatingBook());
        assertTrue(currTranslator1.getTranslatingEntity());
        assertTrue(currTranslator1.getTranslatingSign());
        assertTrue(currTranslator1.getTranslatingItem());
        assertEquals(currTranslator1.getRateLimit(), 5);
        assertTrue(currTranslator1.getTranslatingChatOutgoing());
        assertTrue(currTranslator1.getTranslatingChatIncoming());

        assertTrue(currTranslator2.getInLangCode().equals("en")
                && currTranslator2.getOutLangCode().equals("es"));
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingEntity());
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingItem());
        assertEquals(currTranslator2.getRateLimit(), 0);
        assertFalse(currTranslator2.getTranslatingChatOutgoing());
        assertFalse(currTranslator2.getTranslatingChatIncoming());

        // Verify PlayerRecords
        PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
        PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

        assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getUUID(), currTranslator2.getUUID());
        assertNotEquals("None", playerRecord2.getLastTranslationTime());
        assertTrue(playerRecord2.getHasBeenSaved());

        assertTrue(playerRecord1.getAttemptedTranslations() > 0);
        assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
        assertEquals(playerRecord1.getUUID(), currTranslator1.getUUID());
        assertNotEquals("None", playerRecord1.getLastTranslationTime());
        assertEquals("az", playerRecord1.getLocalizationCode());
        assertTrue(playerRecord1.getHasBeenSaved());

        // Verify Cache
        assertTrue(main.getCache().estimatedSize() > 1);
        assertEquals("prueba!", main.getCacheTerm(new CachedTranslation("en", "es", "test!")));
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
        ActiveTranslator currTranslator1 = plugin.getActiveTranslator(playerMock);
        ActiveTranslator currTranslator2 = plugin.getActiveTranslator(secondPlayerMock);
        assertTrue(main.isSQLConnValid(true));

        assertTrue(currTranslator1.getInLangCode().equals("en")
                && currTranslator1.getOutLangCode().equals("fr"));
        assertTrue(currTranslator1.getTranslatingBook());
        assertTrue(currTranslator1.getTranslatingEntity());
        assertTrue(currTranslator1.getTranslatingSign());
        assertTrue(currTranslator1.getTranslatingItem());
        assertEquals(currTranslator1.getRateLimit(), 5);
        assertTrue(currTranslator1.getTranslatingChatOutgoing());
        assertTrue(currTranslator1.getTranslatingChatIncoming());

        assertTrue(currTranslator2.getInLangCode().equals("en")
                && currTranslator2.getOutLangCode().equals("es"));
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingEntity());
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingItem());
        assertEquals(currTranslator2.getRateLimit(), 0);
        assertFalse(currTranslator2.getTranslatingChatOutgoing());
        assertFalse(currTranslator2.getTranslatingChatIncoming());

        // Verify PlayerRecords
        PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
        PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

        assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getUUID(), currTranslator2.getUUID());
        assertNotEquals("None", playerRecord2.getLastTranslationTime());
        assertTrue(playerRecord2.getHasBeenSaved());

        assertTrue(playerRecord1.getAttemptedTranslations() > 0);
        assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
        assertEquals(playerRecord1.getUUID(), currTranslator1.getUUID());
        assertNotEquals("None", playerRecord1.getLastTranslationTime());
        assertEquals("az", playerRecord1.getLocalizationCode());
        assertTrue(playerRecord1.getHasBeenSaved());

        // Verify Cache
        assertTrue(main.getCache().estimatedSize() > 1);
        assertEquals("prueba!", main.getCacheTerm(new CachedTranslation("en", "es", "test!")));
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
        ActiveTranslator currTranslator1 = plugin.getActiveTranslator(playerMock);
        ActiveTranslator currTranslator2 = plugin.getActiveTranslator(secondPlayerMock);
        assertTrue(main.isPostgresConnValid(true));

        assertTrue(currTranslator1.getInLangCode().equals("en")
                && currTranslator1.getOutLangCode().equals("fr"));
        assertTrue(currTranslator1.getTranslatingBook());
        assertTrue(currTranslator1.getTranslatingEntity());
        assertTrue(currTranslator1.getTranslatingSign());
        assertTrue(currTranslator1.getTranslatingItem());
        assertEquals(currTranslator1.getRateLimit(), 5);
        assertTrue(currTranslator1.getTranslatingChatOutgoing());
        assertTrue(currTranslator1.getTranslatingChatIncoming());

        assertTrue(currTranslator2.getInLangCode().equals("en")
                && currTranslator2.getOutLangCode().equals("es"));
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingEntity());
        assertFalse(currTranslator2.getTranslatingSign());
        assertFalse(currTranslator2.getTranslatingItem());
        assertEquals(currTranslator2.getRateLimit(), 0);
        assertFalse(currTranslator2.getTranslatingChatOutgoing());
        assertFalse(currTranslator2.getTranslatingChatIncoming());

        // Verify PlayerRecords
        PlayerRecord playerRecord2 = plugin.getPlayerRecord(secondPlayerMock, false);
        PlayerRecord playerRecord1 = plugin.getPlayerRecord(playerMock, false);

        assertEquals(playerRecord2.getAttemptedTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getSuccessfulTranslations(), beforeReloadCount + 1);
        assertEquals(playerRecord2.getUUID(), currTranslator2.getUUID());
        assertNotEquals("None", playerRecord2.getLastTranslationTime());
        assertTrue(playerRecord2.getHasBeenSaved());

        assertTrue(playerRecord1.getAttemptedTranslations() > 0);
        assertTrue(playerRecord1.getSuccessfulTranslations() > 0);
        assertEquals(playerRecord1.getUUID(), currTranslator1.getUUID());
        assertNotEquals("None", playerRecord1.getLastTranslationTime());
        assertEquals("az", playerRecord1.getLocalizationCode());
        assertTrue(playerRecord1.getHasBeenSaved());

        // Verify Cache
        assertTrue(main.getCache().estimatedSize() > 1);
        assertEquals("prueba!", main.getCacheTerm(new CachedTranslation("en", "es", "test!")));
    }
}