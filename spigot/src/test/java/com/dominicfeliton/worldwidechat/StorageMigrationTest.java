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
    void mysqlMigratesLegacyTables() throws Exception {
        assertLegacyJdbcSchemaMigrates(StorageBackend.MYSQL);
    }

    @Test
    void postgresMigratesLegacyTables() throws Exception {
        assertLegacyJdbcSchemaMigrates(StorageBackend.POSTGRES);
    }

    @Test
    void mongoRunsMongockStorageMigration() {
        WWCTestSupport.useStorageBackend(StorageBackend.MONGO);

        MongoDatabase database = plugin().getMongoSession().getActiveDatabase();
        database.getCollection("wwc_mongock_changelog").drop();
        database.getCollection("wwc_mongock_lock").drop();
        database.getCollection("ActiveTranslators").drop();
        database.getCollection("PlayerRecords").drop();
        database.getCollection("PersistentCache").drop();

        MongoCollection<Document> cache = database.getCollection("PersistentCache");
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
        assertEquals(1, cache.countDocuments());
        assertTrue(hasMongoIndex(cache, "idx_persistent_cache_lookup"));
    }

    private void assertLegacyJdbcSchemaMigrates(StorageBackend backend) throws Exception {
        WWCTestSupport.useStorageBackend(backend);

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS wwc_schema_history");
            statement.executeUpdate("DROP TABLE IF EXISTS activeTranslators");
            statement.executeUpdate("DROP TABLE IF EXISTS playerRecords");
            statement.executeUpdate("DROP TABLE IF EXISTS persistentCache");
            statement.executeUpdate("CREATE TABLE activeTranslators (creationDate VARCHAR(40), playerUUID VARCHAR(40), PRIMARY KEY (playerUUID))");
            statement.executeUpdate("CREATE TABLE persistentCache (randomUUID VARCHAR(40), inputLang VARCHAR(10), outputLang VARCHAR(10), inputPhrase VARCHAR(260), outputPhrase VARCHAR(260), PRIMARY KEY (randomUUID))");
            statement.executeUpdate("INSERT INTO persistentCache (randomUUID, inputLang, outputLang, inputPhrase, outputPhrase) VALUES ('one', 'en', 'es', 'hello', 'hola')");
            statement.executeUpdate("INSERT INTO persistentCache (randomUUID, inputLang, outputLang, inputPhrase, outputPhrase) VALUES ('two', 'en', 'es', 'hello', 'hola again')");
        }

        StorageMigrationUtils.migrateCurrentBackend();

        try (Connection connection = connectionFor(backend);
             Statement statement = connection.createStatement()) {
            assertTrue(tableExists(connection, "wwc_schema_history", backend));
            assertTrue(columnExists(connection, "activeTranslators", "translatingEntity", backend));
            assertTrue(tableExists(connection, "playerRecords", backend));
            assertTrue(indexExists(connection, "persistentCache", "idx_persistent_cache_lookup", backend));

            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM persistentCache")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    private Connection connectionFor(StorageBackend backend) throws Exception {
        return switch (backend) {
            case MYSQL -> plugin().getSqlSession().getConnection();
            case POSTGRES -> plugin().getPostgresSession().getConnection();
            default -> throw new IllegalArgumentException("Expected JDBC backend");
        };
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
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(catalog(connection, backend), null, normalize(tableName, backend), false, false)) {
            while (indexes.next()) {
                String existingName = indexes.getString("INDEX_NAME");
                if (existingName != null && existingName.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }
        return false;
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
        for (Document index : collection.listIndexes()) {
            if (indexName.equals(index.getString("name"))) {
                return true;
            }
        }
        return false;
    }
}
