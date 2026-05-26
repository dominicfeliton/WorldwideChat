package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.storage.StorageMigrationUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class StorageMigrationTest extends WWCIntegrationTest {

    @Test
    void mysqlMigratesProductionLikeLegacyTables() throws Exception {
        assertProductionLikeLegacyJdbcSchemaMigrates(StorageBackend.MYSQL);
    }

    @Test
    void postgresMigratesProductionLikeLegacyTables() throws Exception {
        assertProductionLikeLegacyJdbcSchemaMigrates(StorageBackend.POSTGRES);
    }

    @Test
    void mysqlMigrationIsIdempotent() throws Exception {
        assertJdbcMigrationIsIdempotent(StorageBackend.MYSQL);
    }

    @Test
    void postgresMigrationIsIdempotent() throws Exception {
        assertJdbcMigrationIsIdempotent(StorageBackend.POSTGRES);
    }

    @Test
    void mysqlDeduplicatesLegacyPlayerRows() throws Exception {
        assertDuplicateLegacyPlayerRowsMigrate(StorageBackend.MYSQL);
    }

    @Test
    void postgresDeduplicatesLegacyPlayerRows() throws Exception {
        assertDuplicateLegacyPlayerRowsMigrate(StorageBackend.POSTGRES);
    }

    @Test
    void mysqlDeduplicatesLegacyActiveTranslatorRows() throws Exception {
        assertDuplicateLegacyActiveTranslatorRowsMigrate(StorageBackend.MYSQL);
    }

    @Test
    void postgresDeduplicatesLegacyActiveTranslatorRows() throws Exception {
        assertDuplicateLegacyActiveTranslatorRowsMigrate(StorageBackend.POSTGRES);
    }

    @Test
    void mongoMigratesLegacyCollectionsAndBadCacheData() {
        WWCTestSupport.useStorageBackend(StorageBackend.MONGO);

        MongoDatabase database = plugin().getMongoSession().getActiveDatabase();
        resetMongoSchema(database);
        database.getCollection("ActiveTranslators").insertOne(new Document("creationDate", "old")
                .append("playerUUID", "mongo-active")
                .append("inLangCode", "en")
                .append("outLangCode", "fr"));
        database.getCollection("PlayerRecords").insertOne(new Document("creationDate", "old")
                .append("playerUUID", "mongo-record")
                .append("attemptedTranslations", 7)
                .append("successfulTranslations", 6)
                .append("lastTranslationTime", "last")
                .append("localizationCode", "es"));
        MongoCollection<Document> cache = database.getCollection("PersistentCache");
        cache.insertOne(new Document("inputLang", "en")
                .append("outputLang", "de")
                .append("inputPhrase", "missing uuid")
                .append("outputPhrase", "fehlt"));
        cache.insertOne(new Document("randomUUID", "one")
                .append("inputLang", "en")
                .append("outputLang", "es")
                .append("inputPhrase", "hello")
                .append("outputPhrase", "hola"));
        cache.insertOne(new Document("randomUUID", "two")
                .append("inputLang", "en")
                .append("outputLang", "es")
                .append("inputPhrase", "hello")
                .append("outputPhrase", "hola again"));

        StorageMigrationUtils.migrateCurrentBackend();

        assertTrue(collectionExists(database, "ActiveTranslators"));
        assertTrue(collectionExists(database, "PlayerRecords"));
        assertTrue(collectionExists(database, "PersistentCache"));
        assertTrue(collectionExists(database, "wwc_mongock_changelog"));
        assertEquals(1, cache.countDocuments(new Document("inputLang", "en")
                .append("outputLang", "es")
                .append("inputPhrase", "hello")));
        Document missingUuid = cache.find(new Document("inputPhrase", "missing uuid")).first();
        assertNotNull(missingUuid);
        assertNotNull(missingUuid.getString("randomUUID"));
        assertEquals("mongo-active", database.getCollection("ActiveTranslators").find().first().getString("playerUUID"));
        assertEquals(7, database.getCollection("PlayerRecords").find().first().getInteger("attemptedTranslations"));
        assertTrue(hasMongoIndex(cache, "idx_persistent_cache_lookup"));
        assertTrue(hasMongoIndex(cache, "idx_persistent_cache_random"));
    }

    @Test
    void mongoMigrationIsIdempotent() {
        WWCTestSupport.useStorageBackend(StorageBackend.MONGO);

        MongoDatabase database = plugin().getMongoSession().getActiveDatabase();
        resetMongoSchema(database);
        MongoCollection<Document> cache = database.getCollection("PersistentCache");
        cache.insertOne(new Document("randomUUID", "one")
                .append("inputLang", "en")
                .append("outputLang", "es")
                .append("inputPhrase", "hello")
                .append("outputPhrase", "hola"));

        StorageMigrationUtils.migrateCurrentBackend();
        long changelogCount = database.getCollection("wwc_mongock_changelog").countDocuments();
        int lookupIndexCount = mongoIndexCount(cache, "idx_persistent_cache_lookup");
        long cacheCount = cache.countDocuments();

        StorageMigrationUtils.migrateCurrentBackend();
        StorageMigrationUtils.migrateCurrentBackend();

        assertEquals(changelogCount, database.getCollection("wwc_mongock_changelog").countDocuments());
        assertEquals(lookupIndexCount, mongoIndexCount(cache, "idx_persistent_cache_lookup"));
        assertEquals(cacheCount, cache.countDocuments());
        assertEquals("hola", cache.find(new Document("inputPhrase", "hello")).first().getString("outputPhrase"));
    }

    @Test
    void migrationFailureDuringStartupDisablesBeforeDataLoad() throws Exception {
        WWCTestSupport.useStorageBackend(StorageBackend.POSTGRES);
        try (Connection connection = connectionFor(StorageBackend.POSTGRES);
             Statement statement = connection.createStatement()) {
            resetJdbcSchema(statement);
            createFullActiveTranslatorsTable(statement);
            createInvalidPlayerRecordsTable(statement);
            createFullPersistentCacheTable(statement);
        }

        try {
            WWCTestSupport.clearRuntimeState();
            WWCTestSupport.reloadExpectingInvalidTranslator();

            assertEquals("Invalid", plugin().getTranslatorName());
            assertTrue(plugin().getActiveTranslators().isEmpty());
            assertTrue(plugin().getPlayerRecords().isEmpty());
        } finally {
            try (Connection connection = StorageBackend.POSTGRES.adminConnection(
                    WWCTestSupport.storageDatabaseName(StorageBackend.POSTGRES));
                 Statement statement = connection.createStatement()) {
                resetJdbcSchema(statement);
            }
        }
    }

    private void assertProductionLikeLegacyJdbcSchemaMigrates(StorageBackend backend) throws Exception {
        WWCTestSupport.useStorageBackend(backend);

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            resetJdbcSchema(statement);
            createLegacyActiveTranslatorsTable(statement);
            createLegacyPlayerRecordsTable(statement);
            createLegacyPersistentCacheTable(statement);
        }

        StorageMigrationUtils.migrateCurrentBackend();

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            assertTrue(tableExists(connection, "wwc_schema_history", backend));
            assertTrue(columnExists(connection, "activeTranslators", "translatingEntity", backend));
            assertTrue(columnExists(connection, "activeTranslators", "extraNote", backend));
            assertTrue(columnExists(connection, "playerRecords", "localizationCode", backend));
            assertTrue(columnExists(connection, "playerRecords", "extraNote", backend));
            assertTrue(columnExists(connection, "persistentCache", "extraCacheNote", backend));
            assertTrue(indexExists(connection, "persistentCache", "idx_persistent_cache_lookup", backend));

            assertEquals("en", jdbcString(statement, "SELECT inLangCode FROM activeTranslators WHERE playerUUID = 'legacy-active'"));
            assertEquals("active-extra", jdbcString(statement, "SELECT extraNote FROM activeTranslators WHERE playerUUID = 'legacy-active'"));
            assertEquals(7, jdbcInt(statement, "SELECT attemptedTranslations FROM playerRecords WHERE playerUUID = 'legacy-record'"));
            assertEquals("player-extra", jdbcString(statement, "SELECT extraNote FROM playerRecords WHERE playerUUID = 'legacy-record'"));
            assertEquals(1, jdbcInt(statement, "SELECT COUNT(*) FROM persistentCache WHERE inputLang = 'en' AND outputLang = 'es' AND inputPhrase = 'hello'"));
            assertEquals("hola", jdbcString(statement, "SELECT outputPhrase FROM persistentCache WHERE randomUUID = 'cache-one'"));
            assertEquals("keep", jdbcString(statement, "SELECT extraCacheNote FROM persistentCache WHERE randomUUID = 'cache-one'"));
        }
    }

    private void assertJdbcMigrationIsIdempotent(StorageBackend backend) throws Exception {
        WWCTestSupport.useStorageBackend(backend);

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO activeTranslators (creationDate, playerUUID, inLangCode, outLangCode, rateLimit, rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, translatingBook, translatingSign, translatingItem, translatingEntity) VALUES ('now', 'active-idem', 'en', 'es', 2, 'None', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE)");
            statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime, localizationCode) VALUES ('now', 'record-idem', 3, 2, 'last', 'en')");
            statement.executeUpdate("INSERT INTO persistentCache (randomUUID, inputLang, outputLang, inputPhrase, outputPhrase) VALUES ('cache-idem', 'en', 'es', 'hello idem', 'hola')");
        }

        long historyCount = jdbcCount(backend, "SELECT COUNT(*) FROM wwc_schema_history");
        int lookupIndexCount;
        try (Connection connection = connectionFor(backend)) {
            lookupIndexCount = indexOccurrenceCount(connection, "persistentCache", "idx_persistent_cache_lookup", backend);
        }

        StorageMigrationUtils.migrateCurrentBackend();
        StorageMigrationUtils.migrateCurrentBackend();

        assertEquals(historyCount, jdbcCount(backend, "SELECT COUNT(*) FROM wwc_schema_history"));
        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            assertEquals(lookupIndexCount, indexOccurrenceCount(connection, "persistentCache", "idx_persistent_cache_lookup", backend));
            assertEquals(1, jdbcInt(statement, "SELECT COUNT(*) FROM activeTranslators WHERE playerUUID = 'active-idem'"));
            assertEquals(1, jdbcInt(statement, "SELECT COUNT(*) FROM playerRecords WHERE playerUUID = 'record-idem'"));
            assertEquals("hola", jdbcString(statement, "SELECT outputPhrase FROM persistentCache WHERE randomUUID = 'cache-idem'"));
        }
    }

    private void assertDuplicateLegacyPlayerRowsMigrate(StorageBackend backend) throws Exception {
        WWCTestSupport.useStorageBackend(backend);

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            resetJdbcSchema(statement);
            createDuplicatePlayerRecordsTable(statement);
        }

        try {
            StorageMigrationUtils.migrateCurrentBackend();

            try (Connection connection = connectionFor(backend);
                 Statement statement = connection.createStatement()) {
                assertEquals(1, jdbcInt(statement,
                        "SELECT COUNT(*) FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
                assertEquals("2025-01-02T00:00:00Z", jdbcString(statement,
                        "SELECT creationDate FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
                assertEquals(4, jdbcInt(statement,
                        "SELECT attemptedTranslations FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
                assertEquals(3, jdbcInt(statement,
                        "SELECT successfulTranslations FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
                assertEquals("02/01/2025 00:00:00", jdbcString(statement,
                        "SELECT lastTranslationTime FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
                assertEquals("fr", jdbcString(statement,
                        "SELECT localizationCode FROM playerRecords WHERE playerUUID = 'duplicate-player'"));
            }
        } finally {
            try (Connection connection = connectionFor(backend);
                 Statement statement = connection.createStatement()) {
                resetJdbcSchema(statement);
            }
            StorageMigrationUtils.migrateCurrentBackend();
        }
    }

    private void assertDuplicateLegacyActiveTranslatorRowsMigrate(StorageBackend backend) throws Exception {
        WWCTestSupport.useStorageBackend(backend);

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            resetJdbcSchema(statement);
            createDuplicateActiveTranslatorsTable(statement);
        }

        try {
            StorageMigrationUtils.migrateCurrentBackend();

            try (Connection connection = connectionFor(backend);
                 Statement statement = connection.createStatement()) {
                assertEquals(1, jdbcInt(statement,
                        "SELECT COUNT(*) FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertEquals("2025-01-02T00:00:00Z", jdbcString(statement,
                        "SELECT creationDate FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertEquals("fr", jdbcString(statement,
                        "SELECT inLangCode FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertEquals("de", jdbcString(statement,
                        "SELECT outLangCode FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertEquals(9, jdbcInt(statement,
                        "SELECT rateLimit FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertEquals("2025-01-02T01:00:00Z", jdbcString(statement,
                        "SELECT rateLimitPreviousTime FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertFalse(jdbcBoolean(statement,
                        "SELECT translatingChatOutgoing FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertTrue(jdbcBoolean(statement,
                        "SELECT translatingChatIncoming FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
                assertTrue(jdbcBoolean(statement,
                        "SELECT translatingEntity FROM activeTranslators WHERE playerUUID = 'duplicate-active'"));
            }
        } finally {
            try (Connection connection = connectionFor(backend);
                 Statement statement = connection.createStatement()) {
                resetJdbcSchema(statement);
            }
            StorageMigrationUtils.migrateCurrentBackend();
        }
    }

    private void resetJdbcSchema(Statement statement) throws Exception {
        statement.executeUpdate("DROP TABLE IF EXISTS wwc_schema_history");
        statement.executeUpdate("DROP TABLE IF EXISTS activeTranslators");
        statement.executeUpdate("DROP TABLE IF EXISTS playerRecords");
        statement.executeUpdate("DROP TABLE IF EXISTS persistentCache");
    }

    private void createLegacyActiveTranslatorsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE activeTranslators (creationDate VARCHAR(40), playerUUID VARCHAR(40), inLangCode VARCHAR(10), outLangCode VARCHAR(10), rateLimit INT, rateLimitPreviousTime VARCHAR(40), translatingChatOutgoing BOOLEAN, translatingChatIncoming BOOLEAN, translatingBook BOOLEAN, translatingSign BOOLEAN, translatingItem BOOLEAN, extraNote VARCHAR(40), PRIMARY KEY (playerUUID))");
        statement.executeUpdate("INSERT INTO activeTranslators (creationDate, playerUUID, inLangCode, outLangCode, rateLimit, rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, translatingBook, translatingSign, translatingItem, extraNote) VALUES ('old', 'legacy-active', 'en', 'fr', 5, 'None', TRUE, TRUE, TRUE, FALSE, TRUE, 'active-extra')");
    }

    private void createLegacyPlayerRecordsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE playerRecords (creationDate VARCHAR(40), playerUUID VARCHAR(40), attemptedTranslations INT, successfulTranslations INT, lastTranslationTime VARCHAR(40), extraNote VARCHAR(40), PRIMARY KEY (playerUUID))");
        statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime, extraNote) VALUES ('old', 'legacy-record', 7, 6, 'last', 'player-extra')");
    }

    private void createLegacyPersistentCacheTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE persistentCache (randomUUID VARCHAR(40), inputLang VARCHAR(10), outputLang VARCHAR(10), inputPhrase VARCHAR(260), outputPhrase VARCHAR(260), extraCacheNote VARCHAR(40), PRIMARY KEY (randomUUID))");
        statement.executeUpdate("INSERT INTO persistentCache (randomUUID, inputLang, outputLang, inputPhrase, outputPhrase, extraCacheNote) VALUES ('cache-one', 'en', 'es', 'hello', 'hola', 'keep')");
        statement.executeUpdate("INSERT INTO persistentCache (randomUUID, inputLang, outputLang, inputPhrase, outputPhrase, extraCacheNote) VALUES ('cache-two', 'en', 'es', 'hello', 'hola again', 'delete')");
    }

    private void createFullActiveTranslatorsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE activeTranslators (creationDate VARCHAR(40), playerUUID VARCHAR(40), inLangCode VARCHAR(10), outLangCode VARCHAR(10), rateLimit INT, rateLimitPreviousTime VARCHAR(40), translatingChatOutgoing BOOLEAN, translatingChatIncoming BOOLEAN, translatingBook BOOLEAN, translatingSign BOOLEAN, translatingItem BOOLEAN, translatingEntity BOOLEAN, PRIMARY KEY (playerUUID))");
    }

    private void createFullPersistentCacheTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE persistentCache (randomUUID VARCHAR(40), inputLang VARCHAR(10), outputLang VARCHAR(10), inputPhrase VARCHAR(260), outputPhrase VARCHAR(260), PRIMARY KEY (randomUUID))");
    }

    private void createDuplicatePlayerRecordsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE playerRecords (creationDate VARCHAR(40), playerUUID VARCHAR(40), attemptedTranslations INT, successfulTranslations INT, lastTranslationTime VARCHAR(40), localizationCode VARCHAR(10))");
        statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime, localizationCode) VALUES ('2024-01-02T00:00:00Z', 'duplicate-player', 1, 1, '01/01/2024 00:00:00', '')");
        statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime, localizationCode) VALUES ('2025-01-02T00:00:00Z', 'duplicate-player', 4, 3, '02/01/2025 00:00:00', 'fr')");
    }

    private void createDuplicateActiveTranslatorsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE activeTranslators (creationDate VARCHAR(40), playerUUID VARCHAR(40), inLangCode VARCHAR(10), outLangCode VARCHAR(10), rateLimit INT, rateLimitPreviousTime VARCHAR(40), translatingChatOutgoing BOOLEAN, translatingChatIncoming BOOLEAN, translatingBook BOOLEAN, translatingSign BOOLEAN, translatingItem BOOLEAN, translatingEntity BOOLEAN)");
        statement.executeUpdate("INSERT INTO activeTranslators (creationDate, playerUUID, inLangCode, outLangCode, rateLimit, rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, translatingBook, translatingSign, translatingItem, translatingEntity) VALUES ('2024-01-02T00:00:00Z', 'duplicate-active', 'en', 'es', 1, '2024-01-02T01:00:00Z', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE)");
        statement.executeUpdate("INSERT INTO activeTranslators (creationDate, playerUUID, inLangCode, outLangCode, rateLimit, rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, translatingBook, translatingSign, translatingItem, translatingEntity) VALUES ('2025-01-02T00:00:00Z', 'duplicate-active', 'fr', 'de', 9, '2025-01-02T01:00:00Z', FALSE, TRUE, TRUE, TRUE, TRUE, TRUE)");
    }

    private void createInvalidPlayerRecordsTable(Statement statement) throws Exception {
        statement.executeUpdate("CREATE TABLE playerRecords (creationDate VARCHAR(40), playerUUID VARCHAR(40), attemptedTranslations INT, successfulTranslations INT, lastTranslationTime VARCHAR(40))");
        statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime) VALUES ('old', '', 1, 1, 'first')");
        statement.executeUpdate("INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, successfulTranslations, lastTranslationTime) VALUES ('old', NULL, 2, 2, 'second')");
    }

    private void resetMongoSchema(MongoDatabase database) {
        database.getCollection("wwc_mongock_changelog").drop();
        database.getCollection("wwc_mongock_lock").drop();
        database.getCollection("ActiveTranslators").drop();
        database.getCollection("PlayerRecords").drop();
        database.getCollection("PersistentCache").drop();
    }

    private Connection connectionFor(StorageBackend backend) throws Exception {
        return switch (backend) {
            case MYSQL -> plugin().getSqlSession().getConnection();
            case POSTGRES -> plugin().getPostgresSession().getConnection();
            default -> throw new IllegalArgumentException("Expected JDBC backend");
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

    private int jdbcInt(Statement statement, String query) throws Exception {
        try (ResultSet rs = statement.executeQuery(query)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }

    private String jdbcString(Statement statement, String query) throws Exception {
        try (ResultSet rs = statement.executeQuery(query)) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    private boolean jdbcBoolean(Statement statement, String query) throws Exception {
        try (ResultSet rs = statement.executeQuery(query)) {
            assertTrue(rs.next());
            return rs.getBoolean(1);
        }
    }

    private boolean tableExists(Connection connection, String tableName, StorageBackend backend) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet tables = metaData.getTables(catalog(connection, backend), null, normalize(tableName, backend), new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName, StorageBackend backend) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(catalog(connection, backend), null, normalize(tableName, backend), normalize(columnName, backend))) {
            return columns.next();
        }
    }

    private boolean indexExists(Connection connection, String tableName, String indexName, StorageBackend backend) throws Exception {
        return indexOccurrenceCount(connection, tableName, indexName, backend) > 0;
    }

    private int indexOccurrenceCount(Connection connection, String tableName, String indexName, StorageBackend backend) throws Exception {
        int count = 0;
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(catalog(connection, backend), null, normalize(tableName, backend), false, false)) {
            while (indexes.next()) {
                String existingName = indexes.getString("INDEX_NAME");
                if (existingName != null && existingName.equalsIgnoreCase(indexName)) {
                    count++;
                }
            }
        }
        return count;
    }

    private String catalog(Connection connection, StorageBackend backend) throws Exception {
        return backend == StorageBackend.POSTGRES ? null : connection.getCatalog();
    }

    private String normalize(String identifier, StorageBackend backend) {
        return backend == StorageBackend.POSTGRES ? identifier.toLowerCase(Locale.ROOT) : identifier;
    }

    private boolean collectionExists(MongoDatabase database, String collectionName) {
        for (String existingName : database.listCollectionNames()) {
            if (existingName.equals(collectionName)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMongoIndex(MongoCollection<Document> collection, String indexName) {
        return mongoIndexCount(collection, indexName) > 0;
    }

    private int mongoIndexCount(MongoCollection<Document> collection, String indexName) {
        int count = 0;
        for (Document index : collection.listIndexes()) {
            if (indexName.equals(index.getString("name"))) {
                count++;
            }
        }
        return count;
    }
}
