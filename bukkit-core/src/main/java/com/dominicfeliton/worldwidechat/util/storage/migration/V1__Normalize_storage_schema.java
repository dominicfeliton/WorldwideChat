package com.dominicfeliton.worldwidechat.util.storage.migration;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class V1__Normalize_storage_schema extends BaseJavaMigration {

    private static final String CACHE_LOOKUP_INDEX = "idx_persistent_cache_lookup";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        boolean postgres = isPostgres(connection);

        for (Map.Entry<String, Map<String, String>> entry : CommonRefs.tableSchemas.entrySet()) {
            String tableName = entry.getKey();
            String primaryKey = "persistentCache".equals(tableName) ? "randomUUID" : "playerUUID";
            createOrUpdateTable(connection, tableName, entry.getValue(), primaryKey, postgres);
        }

        dedupePersistentCache(connection, postgres);
        ensureIndex(connection, "persistentCache", CACHE_LOOKUP_INDEX,
                "inputLang, outputLang, inputPhrase", true, postgres);
    }

    private boolean isPostgres(Connection connection) throws Exception {
        return connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgres");
    }

    private void createOrUpdateTable(Connection connection, String tableName, Map<String, String> schema,
                                     String primaryKey, boolean postgres) throws Exception {
        String columns = schema.entrySet().stream()
                .map(column -> column.getKey() + " " + column.getValue())
                .collect(Collectors.joining(", "));

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns
                    + ", PRIMARY KEY (" + primaryKey + "))");
        }

        Set<String> existingColumns = getExistingColumns(connection, tableName, postgres);
        try (Statement statement = connection.createStatement()) {
            for (Map.Entry<String, String> column : schema.entrySet()) {
                String normalizedColumn = normalizeIdentifier(column.getKey(), postgres);
                if (!existingColumns.contains(normalizedColumn)) {
                    statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN "
                            + column.getKey() + " " + column.getValue());
                }
            }
        }

        ensurePrimaryKey(connection, tableName, primaryKey, postgres);
    }

    private Set<String> getExistingColumns(Connection connection, String tableName, boolean postgres) throws Exception {
        Set<String> existingColumns = new HashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(catalog(connection, postgres), null, normalizeIdentifier(tableName, postgres), null)) {
            while (columns.next()) {
                existingColumns.add(normalizeIdentifier(columns.getString("COLUMN_NAME"), postgres));
            }
        }
        return existingColumns;
    }

    private void ensurePrimaryKey(Connection connection, String tableName, String primaryKey, boolean postgres) throws Exception {
        if (hasPrimaryKey(connection, tableName, postgres)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD PRIMARY KEY (" + primaryKey + ")");
        }
    }

    private boolean hasPrimaryKey(Connection connection, String tableName, boolean postgres) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet keys = metaData.getPrimaryKeys(catalog(connection, postgres), null, normalizeIdentifier(tableName, postgres))) {
            return keys.next();
        }
    }

    private void dedupePersistentCache(Connection connection, boolean postgres) throws Exception {
        String deleteDuplicates;
        if (postgres) {
            deleteDuplicates = "DELETE FROM persistentCache pc USING persistentCache keep "
                    + "WHERE pc.inputLang = keep.inputLang "
                    + "AND pc.outputLang = keep.outputLang "
                    + "AND pc.inputPhrase = keep.inputPhrase "
                    + "AND pc.randomUUID > keep.randomUUID";
        } else {
            deleteDuplicates = "DELETE pc FROM persistentCache pc "
                    + "INNER JOIN persistentCache keep "
                    + "ON pc.inputLang = keep.inputLang "
                    + "AND pc.outputLang = keep.outputLang "
                    + "AND pc.inputPhrase = keep.inputPhrase "
                    + "AND pc.randomUUID > keep.randomUUID";
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteDuplicates);
        }
    }

    private void ensureIndex(Connection connection, String tableName, String indexName, String columns,
                             boolean unique, boolean postgres) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(catalog(connection, postgres), null, normalizeIdentifier(tableName, postgres), false, false)) {
            while (indexes.next()) {
                String existingName = indexes.getString("INDEX_NAME");
                if (existingName != null && existingName.equalsIgnoreCase(indexName)) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName
                    + " ON " + tableName + " (" + columns + ")");
        }
    }

    private String normalizeIdentifier(String identifier, boolean postgres) {
        return postgres ? identifier.toLowerCase(Locale.ROOT) : identifier;
    }

    private String catalog(Connection connection, boolean postgres) throws Exception {
        return postgres ? null : connection.getCatalog();
    }
}
