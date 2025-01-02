package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.TestCommon;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests plugin data retention across reload cycles and
 * for each storage mode (YAML, MongoDB, SQL, Postgres).
 */
public class DataRetention {

    private WorldwideChat plugin;
    private PlayerMock p1, p2;
    private CommonRefs refs;
    private ServerMock server;

    public DataRetention(PlayerMock p1, PlayerMock p2, WorldwideChat plugin, ServerMock server) {
        this.p1 = p1;
        this.p2 = p2;
        this.plugin = plugin;
        this.server = server;
        this.refs = plugin.getServerFactory().getCommonRefs();
    }

    private void assertConnected() {
        YamlConfiguration config = plugin.getConfigManager().getMainConfig();
        boolean mongo = plugin.isMongoConnValid(false);
        boolean sql = plugin.isSQLConnValid(false);
        boolean postgres = plugin.isPostgresConnValid(false);

        if (config.getBoolean("Storage.useMongoDB")) {
            assert mongo;
        } else if (config.getBoolean("Storage.useSQL")) {
            assert sql;
        } else if (config.getBoolean("Storage.usePostgreSQL")) {
            assert postgres;
        } else {
            assert !(mongo || sql || postgres);
        }
    }

    private void dataTest() {
        // Reload to apply config changes
        TestCommon.reload(server, plugin);
        assertConnected();

        // Setup some translator states
        p1.performCommand("wwct en fr");
        p1.performCommand("wwctb");
        p1.performCommand("wwcti");
        p1.performCommand("wwcts");
        p1.performCommand("wwcte");
        p1.performCommand("wwctrl 5");
        p1.performCommand("wwctci");
        p1.performCommand("wwcl az");
        plugin.addCacheTerm(new CachedTranslation("en", "es", "test!"), "prueba!");

        p2.performCommand("wwct en es");
        p2.performCommand("wwctco");

        // Trigger some translations so stats update
        refs.translateText("Hello, how are you?", p1);
        refs.translateText("Hello, how are you?", p2);
        refs.translateText("Hello, how are you?", p2);

        int beforeReloadCount = plugin.getPlayerRecord(p2, false).getAttemptedTranslations();
        refs.translateText("Hello, how are you?", p2); // once more

        // Trigger save
        TestCommon.reload(server, plugin);
        assertConnected();

        // Validate p1's translator
        ActiveTranslator t1 = plugin.getActiveTranslator(p1);
        assertTrue(t1.getInLangCode().equals("en") && t1.getOutLangCode().equals("fr"));
        assertTrue(t1.getTranslatingBook());
        assertTrue(t1.getTranslatingEntity());
        assertTrue(t1.getTranslatingSign());
        assertTrue(t1.getTranslatingItem());
        assertEquals(5, t1.getRateLimit());
        assertTrue(t1.getTranslatingChatOutgoing());
        assertTrue(t1.getTranslatingChatIncoming());

        // Validate p2's translator
        ActiveTranslator t2 = plugin.getActiveTranslator(p2);
        assertTrue(t2.getInLangCode().equals("en") && t2.getOutLangCode().equals("es"));
        assertFalse(t2.getTranslatingSign());
        assertFalse(t2.getTranslatingEntity());
        assertFalse(t2.getTranslatingItem());
        assertEquals(0, t2.getRateLimit());
        assertFalse(t2.getTranslatingChatOutgoing());
        assertFalse(t2.getTranslatingChatIncoming());

        // Validate records
        PlayerRecord rec2 = plugin.getPlayerRecord(p2, false);
        PlayerRecord rec1 = plugin.getPlayerRecord(p1, false);
        assertEquals(beforeReloadCount + 1, rec2.getAttemptedTranslations());
        assertEquals(beforeReloadCount + 1, rec2.getSuccessfulTranslations());
        assertEquals(t2.getUUID(), rec2.getUUID());
        assertNotEquals("None", rec2.getLastTranslationTime());
        assertTrue(rec2.getHasBeenSaved());

        assertTrue(rec1.getAttemptedTranslations() > 0);
        assertTrue(rec1.getSuccessfulTranslations() > 0);
        assertEquals(t1.getUUID(), rec1.getUUID());
        assertNotEquals("None", rec1.getLastTranslationTime());
        assertEquals("az", rec1.getLocalizationCode());
        assertTrue(rec1.getHasBeenSaved());

        // Validate cache
        assertTrue(plugin.getCache().estimatedSize() > 0);
        assertEquals("prueba!", plugin.getCacheTerm(new CachedTranslation("en", "es", "test!")));

        // Wipe test DB
        TestCommon.reload(server, plugin, true);
    }

    public void testPluginDataRetentionYAML() {
        // Force YAML usage
        plugin.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
        plugin.getConfigManager().getMainConfig().set("Storage.useSQL", false);
        plugin.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
        plugin.getConfigManager().saveMainConfig(false);

        dataTest();
    }

    public void testPluginDataRetentionMongoDB() {
        // Switch to Mongo
        plugin.getConfigManager().getMainConfig().set("Storage.useMongoDB", true);
        plugin.getConfigManager().getMainConfig().set("Storage.useSQL", false);
        plugin.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
        plugin.getConfigManager().saveMainConfig(false);

        dataTest();
    }

    public void testPluginDataRetentionSQL() {
        // Switch to SQL
        plugin.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
        plugin.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", false);
        plugin.getConfigManager().getMainConfig().set("Storage.useSQL", true);
        plugin.getConfigManager().saveMainConfig(false);

        dataTest();
    }

    public void testPluginDataRetentionPostgres() {
        // Switch to Postgres
        plugin.getConfigManager().getMainConfig().set("Storage.useMongoDB", false);
        plugin.getConfigManager().getMainConfig().set("Storage.useSQL", false);
        plugin.getConfigManager().getMainConfig().set("Storage.usePostgreSQL", true);
        plugin.getConfigManager().saveMainConfig(false);

        dataTest();
    }
}