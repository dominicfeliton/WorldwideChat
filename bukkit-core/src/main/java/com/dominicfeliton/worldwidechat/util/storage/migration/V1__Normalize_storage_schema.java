package com.dominicfeliton.worldwidechat.util.storage.migration;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class V1__Normalize_storage_schema extends BaseJavaMigration {

    private static final Logger LOGGER = Logger.getLogger(V1__Normalize_storage_schema.class.getName());
    private static final DateTimeFormatter PLAYER_RECORD_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String CACHE_DEDUP_INDEX = "idx_persistent_cache_dedup_temp";
    private static final String CACHE_LOOKUP_INDEX = "idx_persistent_cache_lookup";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        boolean postgres = isPostgres(connection);

        for (Map.Entry<String, Map<String, String>> entry : CommonRefs.tableSchemas.entrySet()) {
            String tableName = entry.getKey();
            createOrUpdateTable(connection, tableName, entry.getValue(), primaryKeyFor(tableName), postgres);
        }

        dedupePlayerRecords(connection);
        dedupeActiveTranslators(connection);

        for (String tableName : CommonRefs.tableSchemas.keySet()) {
            ensurePrimaryKey(connection, tableName, primaryKeyFor(tableName), postgres);
        }

        dedupePersistentCache(connection, postgres);
        ensureIndex(connection, "persistentCache", CACHE_LOOKUP_INDEX,
                "inputLang, outputLang, inputPhrase", true, postgres);
    }

    private String primaryKeyFor(String tableName) {
        return "persistentCache".equals(tableName) ? "randomUUID" : "playerUUID";
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

    private void dedupePlayerRecords(Connection connection) throws Exception {
        requireNoMissingPrimaryKey(connection, "playerRecords", "playerUUID");

        List<String> duplicateUuids = duplicatePrimaryKeyValues(connection, "playerRecords", "playerUUID");
        int removedRows = 0;
        for (String uuid : duplicateUuids) {
            List<PlayerRecordRow> rows = playerRecordRows(connection, uuid);
            if (rows.size() < 2) {
                continue;
            }

            replacePlayerRecordRows(connection, uuid, mergePlayerRecordRows(uuid, rows));
            removedRows += rows.size() - 1;
        }
        logDedupe("playerRecords", duplicateUuids.size(), removedRows);
    }

    private void dedupeActiveTranslators(Connection connection) throws Exception {
        requireNoMissingPrimaryKey(connection, "activeTranslators", "playerUUID");

        List<String> duplicateUuids = duplicatePrimaryKeyValues(connection, "activeTranslators", "playerUUID");
        int removedRows = 0;
        for (String uuid : duplicateUuids) {
            List<ActiveTranslatorRow> rows = activeTranslatorRows(connection, uuid);
            if (rows.size() < 2) {
                continue;
            }

            replaceActiveTranslatorRows(connection, uuid, mergeActiveTranslatorRows(uuid, rows));
            removedRows += rows.size() - 1;
        }
        logDedupe("activeTranslators", duplicateUuids.size(), removedRows);
    }

    private void requireNoMissingPrimaryKey(Connection connection, String tableName, String primaryKey) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE " + primaryKey
                + " IS NULL OR TRIM(" + primaryKey + ") = ''";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            rs.next();
            int missingRows = rs.getInt(1);
            if (missingRows > 0) {
                throw new IllegalStateException("Cannot migrate " + tableName + ": found " + missingRows
                        + " rows with a missing " + primaryKey
                        + ". Remove or repair those legacy rows before starting WorldwideChat.");
            }
        }
    }

    private List<String> duplicatePrimaryKeyValues(Connection connection, String tableName, String primaryKey)
            throws SQLException {
        String query = "SELECT " + primaryKey + " FROM " + tableName + " WHERE " + primaryKey
                + " IS NOT NULL AND TRIM(" + primaryKey + ") <> '' GROUP BY " + primaryKey
                + " HAVING COUNT(*) > 1";
        List<String> duplicateValues = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                duplicateValues.add(rs.getString(1));
            }
        }
        return duplicateValues;
    }

    private List<PlayerRecordRow> playerRecordRows(Connection connection, String uuid) throws SQLException {
        List<PlayerRecordRow> rows = new ArrayList<>();
        String query = "SELECT creationDate, playerUUID, attemptedTranslations, successfulTranslations, "
                + "lastTranslationTime, localizationCode FROM playerRecords WHERE playerUUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new PlayerRecordRow(
                            rs.getString("creationDate"),
                            rs.getString("playerUUID"),
                            nullableInt(rs, "attemptedTranslations"),
                            nullableInt(rs, "successfulTranslations"),
                            rs.getString("lastTranslationTime"),
                            rs.getString("localizationCode")));
                }
            }
        }
        return rows;
    }

    private PlayerRecordRow mergePlayerRecordRows(String uuid, List<PlayerRecordRow> rows) {
        PlayerRecordRow preferred = rows.stream()
                .max(playerRecordPreference())
                .orElseThrow();

        int attemptedTranslations = rows.stream()
                .map(PlayerRecordRow::attemptedTranslations)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int successfulTranslations = rows.stream()
                .map(PlayerRecordRow::successfulTranslations)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        String creationDate = newestInstantValue(rows.stream()
                .map(PlayerRecordRow::creationDate)
                .toList());
        String lastTranslationTime = hasText(preferred.lastTranslationTime()) ? preferred.lastTranslationTime()
                : firstNonBlank(rows.stream().map(PlayerRecordRow::lastTranslationTime).toList(), "");
        String localizationCode = hasText(preferred.localizationCode()) ? preferred.localizationCode()
                : firstNonBlank(rows.stream().map(PlayerRecordRow::localizationCode).toList(), "");

        return new PlayerRecordRow(creationDate, uuid, attemptedTranslations, successfulTranslations,
                lastTranslationTime, localizationCode);
    }

    private Comparator<PlayerRecordRow> playerRecordPreference() {
        return Comparator
                .comparing((PlayerRecordRow row) -> parsePlayerRecordTime(row.lastTranslationTime())
                        .orElse(Instant.MIN))
                .thenComparingInt(row -> nullableNumber(row.attemptedTranslations()))
                .thenComparingInt(row -> nullableNumber(row.successfulTranslations()))
                .thenComparing(row -> text(row.creationDate()))
                .thenComparing(row -> text(row.lastTranslationTime()))
                .thenComparing(row -> text(row.localizationCode()));
    }

    private void replacePlayerRecordRows(Connection connection, String uuid, PlayerRecordRow row) throws SQLException {
        try (PreparedStatement deleteRows = connection.prepareStatement(
                "DELETE FROM playerRecords WHERE playerUUID = ?")) {
            deleteRows.setString(1, uuid);
            deleteRows.executeUpdate();
        }

        String insert = "INSERT INTO playerRecords (creationDate, playerUUID, attemptedTranslations, "
                + "successfulTranslations, lastTranslationTime, localizationCode) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setString(1, row.creationDate());
            statement.setString(2, row.playerUUID());
            setNullableInt(statement, 3, row.attemptedTranslations());
            setNullableInt(statement, 4, row.successfulTranslations());
            statement.setString(5, row.lastTranslationTime());
            statement.setString(6, row.localizationCode());
            statement.executeUpdate();
        }
    }

    private List<ActiveTranslatorRow> activeTranslatorRows(Connection connection, String uuid) throws SQLException {
        List<ActiveTranslatorRow> rows = new ArrayList<>();
        String query = "SELECT creationDate, playerUUID, inLangCode, outLangCode, rateLimit, "
                + "rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, translatingBook, "
                + "translatingSign, translatingItem, translatingEntity FROM activeTranslators WHERE playerUUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ActiveTranslatorRow(
                            rs.getString("creationDate"),
                            rs.getString("playerUUID"),
                            rs.getString("inLangCode"),
                            rs.getString("outLangCode"),
                            nullableInt(rs, "rateLimit"),
                            rs.getString("rateLimitPreviousTime"),
                            nullableBoolean(rs, "translatingChatOutgoing"),
                            nullableBoolean(rs, "translatingChatIncoming"),
                            nullableBoolean(rs, "translatingBook"),
                            nullableBoolean(rs, "translatingSign"),
                            nullableBoolean(rs, "translatingItem"),
                            nullableBoolean(rs, "translatingEntity")));
                }
            }
        }
        return rows;
    }

    private ActiveTranslatorRow mergeActiveTranslatorRows(String uuid, List<ActiveTranslatorRow> rows) {
        ActiveTranslatorRow preferred = rows.stream()
                .max(activeTranslatorPreference())
                .orElseThrow();

        return new ActiveTranslatorRow(
                newestInstantValue(rows.stream().map(ActiveTranslatorRow::creationDate).toList()),
                uuid,
                text(preferred.inLangCode()),
                text(preferred.outLangCode()),
                nullableNumber(preferred.rateLimit()),
                hasText(preferred.rateLimitPreviousTime()) ? preferred.rateLimitPreviousTime() : "None",
                booleanOrDefault(preferred.translatingChatOutgoing(), true),
                booleanOrDefault(preferred.translatingChatIncoming(), false),
                booleanOrDefault(preferred.translatingBook(), false),
                booleanOrDefault(preferred.translatingSign(), false),
                booleanOrDefault(preferred.translatingItem(), false),
                booleanOrDefault(preferred.translatingEntity(), false));
    }

    private Comparator<ActiveTranslatorRow> activeTranslatorPreference() {
        return Comparator
                .comparing(this::activeTranslatorActivityTime)
                .thenComparing(row -> text(row.creationDate()))
                .thenComparing(row -> text(row.inLangCode()))
                .thenComparing(row -> text(row.outLangCode()))
                .thenComparingInt(row -> nullableNumber(row.rateLimit()))
                .thenComparing(row -> text(row.rateLimitPreviousTime()));
    }

    private Instant activeTranslatorActivityTime(ActiveTranslatorRow row) {
        Optional<Instant> rateLimitTime = parseInstantValue(row.rateLimitPreviousTime());
        return rateLimitTime.orElseGet(() -> parseInstantValue(row.creationDate()).orElse(Instant.MIN));
    }

    private void replaceActiveTranslatorRows(Connection connection, String uuid, ActiveTranslatorRow row)
            throws SQLException {
        try (PreparedStatement deleteRows = connection.prepareStatement(
                "DELETE FROM activeTranslators WHERE playerUUID = ?")) {
            deleteRows.setString(1, uuid);
            deleteRows.executeUpdate();
        }

        String insert = "INSERT INTO activeTranslators (creationDate, playerUUID, inLangCode, outLangCode, "
                + "rateLimit, rateLimitPreviousTime, translatingChatOutgoing, translatingChatIncoming, "
                + "translatingBook, translatingSign, translatingItem, translatingEntity) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setString(1, row.creationDate());
            statement.setString(2, row.playerUUID());
            statement.setString(3, row.inLangCode());
            statement.setString(4, row.outLangCode());
            setNullableInt(statement, 5, row.rateLimit());
            statement.setString(6, row.rateLimitPreviousTime());
            setNullableBoolean(statement, 7, row.translatingChatOutgoing());
            setNullableBoolean(statement, 8, row.translatingChatIncoming());
            setNullableBoolean(statement, 9, row.translatingBook());
            setNullableBoolean(statement, 10, row.translatingSign());
            setNullableBoolean(statement, 11, row.translatingItem());
            setNullableBoolean(statement, 12, row.translatingEntity());
            statement.executeUpdate();
        }
    }

    private void logDedupe(String tableName, int duplicateKeys, int removedRows) {
        if (duplicateKeys == 0) {
            return;
        }

        LOGGER.warning("WorldwideChat storage migration deduplicated " + duplicateKeys + " " + tableName
                + " keys and removed " + removedRows + " duplicate rows before adding the primary key.");
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Boolean nullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableBoolean(PreparedStatement statement, int index, Boolean value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BOOLEAN);
        } else {
            statement.setBoolean(index, value);
        }
    }

    private Optional<Instant> parseInstantValue(String value) {
        if (!hasText(value) || value.equalsIgnoreCase("None")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private Optional<Instant> parsePlayerRecordTime(String value) {
        if (!hasText(value)) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.parse(value, PLAYER_RECORD_TIME).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private String newestInstantValue(List<String> values) {
        String newestValue = null;
        Instant newestInstant = Instant.MIN;
        for (String value : values) {
            Optional<Instant> parsed = parseInstantValue(value);
            if (parsed.isPresent() && parsed.get().compareTo(newestInstant) > 0) {
                newestInstant = parsed.get();
                newestValue = value;
            }
        }
        if (newestValue != null) {
            return newestValue;
        }
        return firstNonBlank(values, Instant.now().toString());
    }

    private String firstNonBlank(List<String> values, String fallback) {
        return values.stream()
                .filter(this::hasText)
                .findFirst()
                .orElse(fallback);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private int nullableNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean booleanOrDefault(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
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

        createDedupeIndex(connection, postgres);
        try {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(deleteDuplicates);
            }
        } finally {
            dropDedupeIndex(connection, postgres);
        }
    }

    private void createDedupeIndex(Connection connection, boolean postgres) throws Exception {
        String columns = postgres ? "inputLang, outputLang, inputPhrase" : "inputLang(10), outputLang(10), inputPhrase(255)";
        ensureIndex(connection, "persistentCache", CACHE_DEDUP_INDEX, columns, false, postgres);
    }

    private void dropDedupeIndex(Connection connection, boolean postgres) throws Exception {
        if (!indexExists(connection, "persistentCache", CACHE_DEDUP_INDEX, postgres)) {
            return;
        }

        String dropIndex = postgres ? "DROP INDEX IF EXISTS " + CACHE_DEDUP_INDEX
                : "DROP INDEX " + CACHE_DEDUP_INDEX + " ON persistentCache";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(dropIndex);
        }
    }

    private void ensureIndex(Connection connection, String tableName, String indexName, String columns,
                             boolean unique, boolean postgres) throws Exception {
        if (indexExists(connection, tableName, indexName, postgres)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + indexName
                    + " ON " + tableName + " (" + columns + ")");
        }
    }

    private boolean indexExists(Connection connection, String tableName, String indexName, boolean postgres) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(catalog(connection, postgres), null, normalizeIdentifier(tableName, postgres), false, false)) {
            while (indexes.next()) {
                String existingName = indexes.getString("INDEX_NAME");
                if (existingName != null && existingName.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private record PlayerRecordRow(String creationDate, String playerUUID, Integer attemptedTranslations,
                                   Integer successfulTranslations, String lastTranslationTime,
                                   String localizationCode) {
    }

    private record ActiveTranslatorRow(String creationDate, String playerUUID, String inLangCode, String outLangCode,
                                       Integer rateLimit, String rateLimitPreviousTime,
                                       Boolean translatingChatOutgoing, Boolean translatingChatIncoming,
                                       Boolean translatingBook, Boolean translatingSign, Boolean translatingItem,
                                       Boolean translatingEntity) {
    }

    private String normalizeIdentifier(String identifier, boolean postgres) {
        return postgres ? identifier.toLowerCase(Locale.ROOT) : identifier;
    }

    private String catalog(Connection connection, boolean postgres) throws Exception {
        return postgres ? null : connection.getCatalog();
    }
}
