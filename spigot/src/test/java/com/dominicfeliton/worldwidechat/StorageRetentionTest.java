package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StorageRetentionTest extends WWCIntegrationTest {

    @Test
    void yamlPersistsTranslatorRecordsAndCacheAcrossReload() {
        assertPersistsTranslatorRecordsAndCacheAcrossReload(StorageBackend.YAML);
    }

    @Test
    void mysqlPersistsTranslatorRecordsAndCacheAcrossReload() {
        assertPersistsTranslatorRecordsAndCacheAcrossReload(StorageBackend.MYSQL);
    }

    @Test
    void postgresPersistsTranslatorRecordsAndCacheAcrossReload() {
        assertPersistsTranslatorRecordsAndCacheAcrossReload(StorageBackend.POSTGRES);
    }

    @Test
    void mongoPersistsTranslatorRecordsAndCacheAcrossReload() {
        assertPersistsTranslatorRecordsAndCacheAcrossReload(StorageBackend.MONGO);
    }

    @Test
    void postgresLeavesPlayerRecordUnsavedWhenBatchFails() throws Exception {
        WWCTestSupport.useStorageBackend(StorageBackend.POSTGRES);

        String constraintName = "wwc_test_fail_sync";
        try (Connection connection = plugin().getPostgresSession().getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE playerRecords DROP CONSTRAINT IF EXISTS " + constraintName);
            statement.executeUpdate("ALTER TABLE playerRecords ADD CONSTRAINT " + constraintName
                    + " CHECK (attemptedTranslations < 0)");
        }

        PlayerRecord record = new PlayerRecord("now", UUID.randomUUID().toString(), 1, 1);
        plugin().addPlayerRecord(record);

        try {
            assertThrows(SQLException.class, () -> DataStorageUtils.syncData(false));
            assertFalse(record.getHasBeenSaved(), "Failed DB writes must leave the record marked unsaved.");
        } finally {
            try (Connection connection = plugin().getPostgresSession().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE playerRecords DROP CONSTRAINT IF EXISTS " + constraintName);
            }
        }
    }

    private void assertPersistsTranslatorRecordsAndCacheAcrossReload(StorageBackend backend) {
        WWCTestSupport.useStorageBackend(backend);

        PlayerMock first = WWCTestSupport.addOpPlayer("StorageOne");
        PlayerMock second = WWCTestSupport.addOpPlayer("StorageTwo");
        CommonRefs refs = plugin().getServerFactory().getCommonRefs();

        first.performCommand("wwct en fr");
        first.performCommand("wwctb");
        first.performCommand("wwcti");
        first.performCommand("wwcts");
        first.performCommand("wwcte");
        first.performCommand("wwctrl 5");
        first.performCommand("wwctci");
        first.performCommand("wwcl az");
        WWCTestSupport.addCacheTerm("en", "es", "test!", "prueba!");

        second.performCommand("wwct en es");
        second.performCommand("wwctco");

        refs.translateText("Hello, how are you?", first);
        refs.translateText("Hello, how are you?", second);
        int beforeReloadCount = plugin().getPlayerRecord(second, false).getAttemptedTranslations();
        refs.translateText("Hello, how are you?", second);

        WWCTestSupport.reload();

        assertTrue(backend.isConnected(plugin()), "Storage backend should remain active after reload");

        ActiveTranslator firstTranslator = plugin().getActiveTranslator(first);
        assertEquals("en", firstTranslator.getInLangCode());
        assertEquals("fr", firstTranslator.getOutLangCode());
        assertTrue(firstTranslator.getTranslatingBook());
        assertTrue(firstTranslator.getTranslatingItem());
        assertTrue(firstTranslator.getTranslatingSign());
        assertTrue(firstTranslator.getTranslatingEntity());
        assertTrue(firstTranslator.getTranslatingChatIncoming());
        assertEquals(5, firstTranslator.getRateLimit());

        ActiveTranslator secondTranslator = plugin().getActiveTranslator(second);
        assertEquals("en", secondTranslator.getInLangCode());
        assertEquals("es", secondTranslator.getOutLangCode());
        assertFalse(secondTranslator.getTranslatingChatOutgoing());
        assertFalse(secondTranslator.getTranslatingChatIncoming());
        assertFalse(secondTranslator.getTranslatingBook());

        PlayerRecord firstRecord = plugin().getPlayerRecord(first, false);
        PlayerRecord secondRecord = plugin().getPlayerRecord(second, false);
        assertEquals("az", firstRecord.getLocalizationCode());
        assertTrue(firstRecord.getAttemptedTranslations() > 0);
        assertTrue(firstRecord.getSuccessfulTranslations() > 0);
        assertEquals(beforeReloadCount + 1, secondRecord.getAttemptedTranslations());
        assertEquals(beforeReloadCount + 1, secondRecord.getSuccessfulTranslations());
        assertNotEquals("None", secondRecord.getLastTranslationTime());

        assertEquals("prueba!", plugin().getCacheTerm(new CachedTranslation("en", "es", "test!")));

        WWCTestSupport.wipeStorageAndReload();

        assertTrue(backend.isConnected(plugin()), "Storage backend should remain active after wipe");
        assertFalse(plugin().isActiveTranslator(first));
        assertFalse(plugin().isActiveTranslator(second));
        assertEquals(-1, plugin().getPlayerRecord(first, false).getAttemptedTranslations());
        assertEquals(-1, plugin().getPlayerRecord(second, false).getAttemptedTranslations());
        assertNull(plugin().getCacheTerm(new CachedTranslation("en", "es", "test!")));
    }
}
