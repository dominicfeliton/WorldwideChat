package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StorageSyncTest extends WWCIntegrationTest {

    private static final StorageBackend[] ALL_BACKENDS = {
            StorageBackend.YAML, StorageBackend.MYSQL, StorageBackend.POSTGRES, StorageBackend.MONGO
    };

    private static final StorageBackend[] DATABASE_BACKENDS = {
            StorageBackend.MYSQL, StorageBackend.POSTGRES, StorageBackend.MONGO
    };

    @Test
    void successfulSyncMarksAllStoredObjectsSaved() throws Exception {
        for (StorageBackend backend : ALL_BACKENDS) {
            WWCTestSupport.useStorageBackend(backend);
            SyncObjects objects = addUnsavedObjects("success-" + backend.name());

            DataStorageUtils.syncData(false);

            assertTrue(objects.translator().getHasBeenSaved(), backend + " active translator should be saved.");
            assertTrue(objects.record().getHasBeenSaved(), backend + " player record should be saved.");
            assertTrue(objects.cacheTerm().hasBeenSaved(), backend + " cache term should be saved.");
        }
    }

    @Test
    void failedDatabaseWritesLeaveObjectsUnsaved() throws Exception {
        for (StorageBackend backend : DATABASE_BACKENDS) {
            for (StoredObject storedObject : StoredObject.values()) {
                WWCTestSupport.useStorageBackend(backend);
                Object objectUnderTest = addOnlyObject(storedObject, "failed-" + backend.name() + "-" + storedObject.name());

                try (AutoCloseable ignored = installFailureHook(backend, storedObject)) {
                    assertThrows(Exception.class, () -> DataStorageUtils.syncData(false));
                    assertStillUnsaved(objectUnderTest, storedObject, backend);
                }
            }
        }
    }

    @Test
    void databaseCacheSyncUsesNaturalKeyUpsert() throws Exception {
        for (StorageBackend backend : DATABASE_BACKENDS) {
            WWCTestSupport.useStorageBackend(backend);
            CachedTranslation first = new CachedTranslation("en", "es", "same phrase " + backend.name());
            plugin().addCacheTerm(first, "old output");
            DataStorageUtils.syncData(false);

            plugin().getCache().invalidateAll();
            plugin().getCache().cleanUp();
            CachedTranslation second = new CachedTranslation("en", "es", "same phrase " + backend.name());
            plugin().addCacheTerm(second, "new output");

            DataStorageUtils.syncData(false);

            assertEquals(1, storedCacheCount(backend, second));
            assertEquals("new output", storedCacheOutput(backend, second));
        }
    }

    @Test
    void syncDeletesStaleActiveTranslatorsAndCacheTerms() throws Exception {
        for (StorageBackend backend : ALL_BACKENDS) {
            WWCTestSupport.useStorageBackend(backend);
            ActiveTranslator currentTranslator = newTranslator(UUID.randomUUID().toString(), "en", "es");
            ActiveTranslator staleTranslator = newTranslator(UUID.randomUUID().toString(), "en", "fr");
            CachedTranslation currentCache = new CachedTranslation("en", "es", "current " + backend.name());
            CachedTranslation staleCache = new CachedTranslation("en", "fr", "stale " + backend.name());

            plugin().addActiveTranslator(currentTranslator);
            plugin().addActiveTranslator(staleTranslator);
            plugin().addCacheTerm(currentCache, "current output");
            plugin().addCacheTerm(staleCache, "stale output");
            DataStorageUtils.syncData(false);

            plugin().removeActiveTranslator(staleTranslator);
            plugin().removeCacheTerm(staleCache);
            DataStorageUtils.syncData(false);

            assertTrue(storedActiveTranslatorExists(backend, currentTranslator.getUUID()));
            assertFalse(storedActiveTranslatorExists(backend, staleTranslator.getUUID()));
            assertTrue(storedCacheExists(backend, currentCache));
            assertFalse(storedCacheExists(backend, staleCache));
        }
    }

    @Test
    void allPersistedFieldsRoundTripAcrossReload() throws Exception {
        for (StorageBackend backend : ALL_BACKENDS) {
            WWCTestSupport.useStorageBackend(backend);
            String activeUuid = UUID.randomUUID().toString();
            String recordUuid = UUID.randomUUID().toString();
            String previousRateLimitTime = "2024-01-02T03:04:05Z";
            String lastTranslationTime = "02/01/2024 03:04:05";

            ActiveTranslator translator = newTranslator(activeUuid, "en", "fr");
            translator.setRateLimit(12);
            translator.setRateLimitPreviousTime(Instant.parse(previousRateLimitTime));
            translator.setTranslatingChatOutgoing(false);
            translator.setTranslatingChatIncoming(true);
            translator.setTranslatingBook(true);
            translator.setTranslatingSign(true);
            translator.setTranslatingItem(true);
            translator.setTranslatingEntity(true);
            plugin().addActiveTranslator(translator);

            PlayerRecord record = new PlayerRecord(lastTranslationTime, recordUuid, 11, 10);
            record.setLocalizationCode("es");
            plugin().addPlayerRecord(record);

            CachedTranslation cacheTerm = new CachedTranslation("en", "es", "round trip " + backend.name());
            plugin().addCacheTerm(cacheTerm, "viaje");
            DataStorageUtils.syncData(false);

            WWCTestSupport.reload();

            ActiveTranslator loadedTranslator = plugin().getActiveTranslator(activeUuid);
            assertEquals(activeUuid, loadedTranslator.getUUID());
            assertEquals("en", loadedTranslator.getInLangCode());
            assertEquals("fr", loadedTranslator.getOutLangCode());
            assertEquals(12, loadedTranslator.getRateLimit());
            assertEquals(previousRateLimitTime, loadedTranslator.getRateLimitPreviousTime());
            assertFalse(loadedTranslator.getTranslatingChatOutgoing());
            assertTrue(loadedTranslator.getTranslatingChatIncoming());
            assertTrue(loadedTranslator.getTranslatingBook());
            assertTrue(loadedTranslator.getTranslatingSign());
            assertTrue(loadedTranslator.getTranslatingItem());
            assertTrue(loadedTranslator.getTranslatingEntity());

            PlayerRecord loadedRecord = plugin().getPlayerRecord(recordUuid, false);
            assertEquals(recordUuid, loadedRecord.getUUID());
            assertEquals(lastTranslationTime, loadedRecord.getLastTranslationTime());
            assertEquals(11, loadedRecord.getAttemptedTranslations());
            assertEquals(10, loadedRecord.getSuccessfulTranslations());
            assertEquals("es", loadedRecord.getLocalizationCode());

            assertEquals("viaje", plugin().getCacheTerm(cacheTerm), backend + " cache term should reload.");
        }
    }

    @Test
    void storageConfigPrecedenceMatchesCurrentLoaderOrder() {
        StorageBackend.MYSQL.assertPortReady();
        StorageBackend.MONGO.assertPortReady();
        StorageBackend.POSTGRES.assertPortReady();

        YamlConfiguration config = plugin().getConfigManager().getMainConfig();
        StorageBackend.MYSQL.applyTo(config);
        config.set("Storage.useMongoDB", true);
        config.set("Storage.usePostgreSQL", true);
        plugin().getConfigManager().saveMainConfig(false);
        WWCTestSupport.reload();

        assertTrue(plugin().isSQLConnValid(true));
        assertFalse(plugin().isMongoConnValid(true));
        assertFalse(plugin().isPostgresConnValid(true));

        config = plugin().getConfigManager().getMainConfig();
        StorageBackend.MONGO.applyTo(config);
        config.set("Storage.usePostgreSQL", true);
        plugin().getConfigManager().saveMainConfig(false);
        WWCTestSupport.reload();

        assertFalse(plugin().isSQLConnValid(true));
        assertTrue(plugin().isMongoConnValid(true));
        assertFalse(plugin().isPostgresConnValid(true));
    }

    private SyncObjects addUnsavedObjects(String suffix) {
        ActiveTranslator translator = newTranslator(UUID.randomUUID().toString(), "en", "es");
        PlayerRecord record = new PlayerRecord("now", UUID.randomUUID().toString(), 3, 2);
        record.setLocalizationCode("en");
        CachedTranslation cacheTerm = new CachedTranslation("en", "es", "cached " + suffix);

        plugin().addActiveTranslator(translator);
        plugin().addPlayerRecord(record);
        plugin().addCacheTerm(cacheTerm, "output " + suffix);
        return new SyncObjects(translator, record, cacheTerm);
    }

    private Object addOnlyObject(StoredObject storedObject, String suffix) {
        return switch (storedObject) {
            case ACTIVE_TRANSLATOR -> {
                ActiveTranslator translator = newTranslator(UUID.randomUUID().toString(), "en", "es");
                plugin().addActiveTranslator(translator);
                yield translator;
            }
            case PLAYER_RECORD -> {
                PlayerRecord record = new PlayerRecord("now", UUID.randomUUID().toString(), 3, 2);
                plugin().addPlayerRecord(record);
                yield record;
            }
            case CACHE_TERM -> {
                CachedTranslation cacheTerm = new CachedTranslation("en", "es", "failed cache " + suffix);
                plugin().addCacheTerm(cacheTerm, "allowed output");
                yield cacheTerm;
            }
        };
    }

    private ActiveTranslator newTranslator(String uuid, String inputLang, String outputLang) {
        ActiveTranslator translator = new ActiveTranslator(uuid, inputLang, outputLang);
        translator.setRateLimit(4);
        translator.setTranslatingChatIncoming(true);
        translator.setTranslatingBook(true);
        return translator;
    }

    private void assertStillUnsaved(Object objectUnderTest, StoredObject storedObject, StorageBackend backend) {
        switch (storedObject) {
            case ACTIVE_TRANSLATOR ->
                    assertFalse(((ActiveTranslator) objectUnderTest).getHasBeenSaved(), backend + " active translator should remain unsaved.");
            case PLAYER_RECORD ->
                    assertFalse(((PlayerRecord) objectUnderTest).getHasBeenSaved(), backend + " player record should remain unsaved.");
            case CACHE_TERM ->
                    assertFalse(((CachedTranslation) objectUnderTest).hasBeenSaved(), backend + " cache term should remain unsaved.");
        }
    }

    private AutoCloseable installFailureHook(StorageBackend backend, StoredObject storedObject) throws Exception {
        if (backend == StorageBackend.MONGO) {
            return installMongoFailureHook(storedObject);
        }
        return installJdbcFailureHook(backend, storedObject);
    }

    private AutoCloseable installJdbcFailureHook(StorageBackend backend, StoredObject storedObject) throws Exception {
        String constraintName = "wwc_test_fail_" + storedObject.name().toLowerCase();
        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            dropJdbcConstraint(statement, backend, storedObject.tableName(), constraintName);
            statement.executeUpdate("ALTER TABLE " + storedObject.tableName() + " ADD CONSTRAINT "
                    + constraintName + " CHECK (" + storedObject.jdbcFailureExpression() + ")");
        }
        return () -> {
            try (Connection connection = connectionFor(backend);
                 Statement statement = connection.createStatement()) {
                dropJdbcConstraint(statement, backend, storedObject.tableName(), constraintName);
            }
        };
    }

    private AutoCloseable installMongoFailureHook(StoredObject storedObject) {
        MongoDatabase database = plugin().getMongoSession().getActiveDatabase();
        database.runCommand(new Document("collMod", storedObject.mongoCollection())
                .append("validator", storedObject.mongoFailureValidator())
                .append("validationLevel", "strict")
                .append("validationAction", "error"));
        return () -> database.runCommand(new Document("collMod", storedObject.mongoCollection())
                .append("validator", new Document())
                .append("validationLevel", "off"));
    }

    private void dropJdbcConstraint(Statement statement, StorageBackend backend, String tableName, String constraintName) throws SQLException {
        if (backend == StorageBackend.MYSQL) {
            try {
                statement.executeUpdate("ALTER TABLE " + tableName + " DROP CHECK " + constraintName);
            } catch (SQLException ignored) {
            }
            return;
        }
        statement.executeUpdate("ALTER TABLE " + tableName + " DROP CONSTRAINT IF EXISTS " + constraintName);
    }

    private Connection connectionFor(StorageBackend backend) throws Exception {
        return switch (backend) {
            case MYSQL -> plugin().getSqlSession().getConnection();
            case POSTGRES -> plugin().getPostgresSession().getConnection();
            default -> throw new IllegalArgumentException("Expected JDBC backend.");
        };
    }

    private boolean storedActiveTranslatorExists(StorageBackend backend, String uuid) throws Exception {
        return switch (backend) {
            case YAML -> new File(plugin().getDataFolder(), "data" + File.separator + uuid + ".yml").exists();
            case MYSQL, POSTGRES -> jdbcCount(backend, "SELECT COUNT(*) FROM activeTranslators WHERE playerUUID = '" + uuid + "'") == 1;
            case MONGO -> plugin().getMongoSession().getActiveDatabase().getCollection("ActiveTranslators")
                    .countDocuments(new Document("playerUUID", uuid)) == 1;
        };
    }

    private boolean storedCacheExists(StorageBackend backend, CachedTranslation cacheTerm) throws Exception {
        return switch (backend) {
            case YAML -> yamlCacheExists(cacheTerm);
            case MYSQL, POSTGRES -> storedCacheCount(backend, cacheTerm) == 1;
            case MONGO -> storedCacheCount(backend, cacheTerm) == 1;
        };
    }

    private long storedCacheCount(StorageBackend backend, CachedTranslation cacheTerm) throws Exception {
        return switch (backend) {
            case MYSQL, POSTGRES -> jdbcCount(backend, "SELECT COUNT(*) FROM persistentCache WHERE inputLang = '"
                    + cacheTerm.getInputLang() + "' AND outputLang = '" + cacheTerm.getOutputLang()
                    + "' AND inputPhrase = '" + cacheTerm.getInputPhrase() + "'");
            case MONGO -> plugin().getMongoSession().getActiveDatabase().getCollection("PersistentCache")
                    .countDocuments(new Document("inputLang", cacheTerm.getInputLang())
                            .append("outputLang", cacheTerm.getOutputLang())
                            .append("inputPhrase", cacheTerm.getInputPhrase()));
            default -> throw new IllegalArgumentException("Expected database backend.");
        };
    }

    private String storedCacheOutput(StorageBackend backend, CachedTranslation cacheTerm) throws Exception {
        return switch (backend) {
            case MYSQL, POSTGRES -> jdbcString(backend, "SELECT outputPhrase FROM persistentCache WHERE inputLang = '"
                    + cacheTerm.getInputLang() + "' AND outputLang = '" + cacheTerm.getOutputLang()
                    + "' AND inputPhrase = '" + cacheTerm.getInputPhrase() + "'");
            case MONGO -> {
                MongoCollection<Document> collection = plugin().getMongoSession().getActiveDatabase().getCollection("PersistentCache");
                Document document = collection.find(new Document("inputLang", cacheTerm.getInputLang())
                        .append("outputLang", cacheTerm.getOutputLang())
                        .append("inputPhrase", cacheTerm.getInputPhrase())).first();
                yield document == null ? null : document.getString("outputPhrase");
            }
            default -> throw new IllegalArgumentException("Expected database backend.");
        };
    }

    private long jdbcCount(StorageBackend backend, String query) throws Exception {
        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private String jdbcString(StorageBackend backend, String query) throws Exception {
        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    private boolean yamlCacheExists(CachedTranslation cacheTerm) {
        File cacheDir = new File(plugin().getDataFolder(), "cache");
        File[] files = cacheDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return false;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (cacheTerm.getInputLang().equals(config.getString("inputLang"))
                    && cacheTerm.getOutputLang().equals(config.getString("outputLang"))
                    && cacheTerm.getInputPhrase().equals(config.getString("inputPhrase"))) {
                return true;
            }
        }
        return false;
    }

    private record SyncObjects(ActiveTranslator translator, PlayerRecord record, CachedTranslation cacheTerm) {
    }

    private enum StoredObject {
        ACTIVE_TRANSLATOR("activeTranslators", "ActiveTranslators", "inLangCode = 'blocked'",
                new Document("inLangCode", "blocked")),
        PLAYER_RECORD("playerRecords", "PlayerRecords", "attemptedTranslations < 0",
                new Document("attemptedTranslations", new Document("$lt", 0))),
        CACHE_TERM("persistentCache", "PersistentCache", "outputPhrase = 'blocked'",
                new Document("outputPhrase", "blocked"));

        private final String tableName;
        private final String mongoCollection;
        private final String jdbcFailureExpression;
        private final Document mongoFailureValidator;

        StoredObject(String tableName, String mongoCollection, String jdbcFailureExpression, Document mongoFailureValidator) {
            this.tableName = tableName;
            this.mongoCollection = mongoCollection;
            this.jdbcFailureExpression = jdbcFailureExpression;
            this.mongoFailureValidator = mongoFailureValidator;
        }

        String tableName() {
            return tableName;
        }

        String mongoCollection() {
            return mongoCollection;
        }

        String jdbcFailureExpression() {
            return jdbcFailureExpression;
        }

        Document mongoFailureValidator() {
            return mongoFailureValidator;
        }
    }
}
