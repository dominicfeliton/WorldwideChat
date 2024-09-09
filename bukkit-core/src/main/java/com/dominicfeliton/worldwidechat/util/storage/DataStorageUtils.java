package com.dominicfeliton.worldwidechat.util.storage;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.configuration.ConfigurationHandler;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataStorageUtils {

    private static WorldwideChat main = WorldwideChat.instance;
    private static CommonRefs refs = main.getServerFactory().getCommonRefs();
    private static ConfigurationHandler handler = main.getConfigManager();
    private static YamlConfiguration mainConfig = handler.getMainConfig();

    /* Sync user data to storage default */
    public static void syncData() throws SQLException, MongoException {
        syncData(main.getTranslatorName().equalsIgnoreCase("Invalid"));
    }

    /* Sync user data to storage */
    public static void syncData(boolean wasPreviouslyInvalid) throws SQLException, MongoException {
        /* If our translator is Invalid, do not run this code */
        if (wasPreviouslyInvalid) {
            return;
        }

        SQLUtils sql = main.getSqlSession();
        MongoDBUtils mongo = main.getMongoSession();
        PostgresUtils postgres = main.getPostgresSession();

        if (main.isSQLConnValid(true)) {
            // Our Generic Table Layout:
            // | Creation Date | Object Properties |
            try (Connection sqlConnection = sql.getConnection()) {
                /* Sync ActiveTranslator data to corresponding table */
                // Dynamically construct the SQL statement based on the schema
                String tableName = "activeTranslators";
                Map<String, String> schema = CommonRefs.tableSchemas.get(tableName);

                // Columns and placeholders for the INSERT part
                String columns = String.join(",", schema.keySet());
                String placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                // Dynamically create the ON DUPLICATE KEY UPDATE part
                String onUpdate = schema.keySet().stream()
                        .map(column -> String.format("%s = VALUES(%s)", column, column))
                        .collect(Collectors.joining(", "));

                String sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                        tableName, columns, placeholders, onUpdate);

                try (PreparedStatement newActiveTranslator = sqlConnection.prepareStatement(sqlStatement)) {
                    for (Map.Entry<String, ActiveTranslator> entry : main.getActiveTranslators().entrySet()) {
                        String key = entry.getKey();
                        ActiveTranslator val = entry.getValue();
                        if (!val.getHasBeenSaved()) {
                            int i = 1;
                            newActiveTranslator.setString(i++, Instant.now().toString());
                            newActiveTranslator.setString(i++, val.getUUID());
                            newActiveTranslator.setString(i++, val.getInLangCode());
                            newActiveTranslator.setString(i++, val.getOutLangCode());
                            newActiveTranslator.setInt(i++, val.getRateLimit());
                            newActiveTranslator.setString(i++, val.getRateLimitPreviousTime());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingChatOutgoing());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingChatIncoming());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingBook());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingSign());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingItem());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingEntity());

                            // Add to batch
                            newActiveTranslator.addBatch();
                            refs.debugMsg("(SQL) Prepared batch entry for " + key + ".");
                            val.setHasBeenSaved(true);
                        }
                    }

                    // Execute the batch
                    newActiveTranslator.executeBatch();
                    refs.debugMsg("(SQL) Batch executed, data saved or updated.");
                }

                /* Delete any old ActiveTranslators */
                String deleteSql = "DELETE FROM activeTranslators WHERE playerUUID = ?";
                try (PreparedStatement deleteOldItem = sqlConnection.prepareStatement(deleteSql);
                     ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
                    while (rs.next()) {
                        String uuid = rs.getString("playerUUID");
                        if (!main.isActiveTranslator(uuid)) {
                            deleteOldItem.setString(1, uuid);
                            deleteOldItem.addBatch();
                            refs.debugMsg("(SQL) Prepared delete batch entry for " + uuid + ".");
                        }
                    }

                    // Execute all deletions in a single batch
                    deleteOldItem.executeBatch();
                    refs.debugMsg("(SQL) Batch delete executed, old active translators removed.");
                }


                /* Sync PlayerRecord data to corresponding table */
                // Dynamically construct the SQL statement based on the schema
                tableName = "playerRecords";
                schema = CommonRefs.tableSchemas.get(tableName);

                // Columns and placeholders for the INSERT part
                columns = String.join(",", schema.keySet());
                placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                // Dynamically create the ON DUPLICATE KEY UPDATE part
                onUpdate = schema.keySet().stream()
                        .map(column -> String.format("%s = VALUES(%s)", column, column))
                        .collect(Collectors.joining(", "));

                sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                        tableName, columns, placeholders, onUpdate);

                try (PreparedStatement newPlayerRecord = sqlConnection.prepareStatement(sqlStatement)) {
                    for (Map.Entry<String, PlayerRecord> entry : main.getPlayerRecords().entrySet()) {
                        String key = entry.getKey();
                        PlayerRecord val = entry.getValue();
                        if (!val.getHasBeenSaved()) {
                            int i = 1;
                            newPlayerRecord.setString(i++, Instant.now().toString());
                            newPlayerRecord.setString(i++, val.getUUID());
                            newPlayerRecord.setInt(i++, val.getAttemptedTranslations());
                            newPlayerRecord.setInt(i++, val.getSuccessfulTranslations());
                            newPlayerRecord.setString(i++, val.getLastTranslationTime());
                            newPlayerRecord.setString(i++, val.getLocalizationCode());

                            // Add to batch
                            newPlayerRecord.addBatch();
                            refs.debugMsg("(SQL) Prepared batch entry for " + key + ".");
                            val.setHasBeenSaved(true);
                        }
                    }

                    // Execute the batch
                    newPlayerRecord.executeBatch();
                    refs.debugMsg("(SQL) Batch executed, player records saved or updated.");
                }

                /* Sync Cache data to corresponding table */
                if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                    /* Add Cache Terms */
                    tableName = "persistentCache";
                    schema = CommonRefs.tableSchemas.get(tableName);

                    columns = String.join(",", schema.keySet());
                    placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                    onUpdate = schema.keySet().stream()
                            .map(column -> String.format("%s = VALUES(%s)", column, column))
                            .collect(Collectors.joining(", "));

                    sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                            tableName, columns, placeholders, onUpdate);

                    try (PreparedStatement newCacheTerm = sqlConnection.prepareStatement(sqlStatement)) {
                        for (Map.Entry<CachedTranslation, String> entry : main.getCache().asMap().entrySet()) {
                            CachedTranslation val = entry.getKey();
                            String value = entry.getValue();
                            if (!val.hasBeenSaved()) {
                                int i = 1;
                                newCacheTerm.setString(i++, UUID.randomUUID().toString());
                                newCacheTerm.setString(i++, val.getInputLang());
                                newCacheTerm.setString(i++, val.getOutputLang());
                                newCacheTerm.setString(i++, val.getInputPhrase());
                                newCacheTerm.setString(i++, value);

                                newCacheTerm.addBatch();
                                refs.debugMsg("(SQL) Prepared batch entry for cache data.");
                                val.setHasBeenSaved(true);
                            }
                        }

                        // Execute all updates/inserts in a single batch
                        newCacheTerm.executeBatch();
                        refs.debugMsg("(SQL) Batch executed, cache data saved or updated.");
                    }
                }

                /* Delete Old Cache Terms */
                deleteSql = "DELETE FROM persistentCache WHERE inputLang = ? AND outputLang = ? AND inputPhrase = ?";
                try (PreparedStatement deleteOldItems = sqlConnection.prepareStatement(deleteSql);
                     ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM persistentCache")) {
                    while (rs.next()) {
                        String inputLang = rs.getString("inputLang");
                        String outputLang = rs.getString("outputLang");
                        String inputPhrase = rs.getString("inputPhrase");

                        if (!main.hasCacheTerm(new CachedTranslation(inputLang, outputLang, inputPhrase))) {
                            deleteOldItems.setString(1, inputLang);
                            deleteOldItems.setString(2, outputLang);
                            deleteOldItems.setString(3, inputPhrase);
                            deleteOldItems.addBatch();
                            refs.debugMsg("(SQL) Prepared delete batch entry for cache entry.");
                        }
                    }

                    // Execute all deletions in a single batch
                    deleteOldItems.executeBatch();
                    refs.debugMsg("(SQL) Batch delete executed, old cache entries removed.");
                }
            }
        } else if (main.isPostgresConnValid(true)) {
            // Our Generic Table Layout:
            // | Creation Date | Object Properties |
            try (Connection postgresConnection = postgres.getConnection()) {
                /* Sync ActiveTranslator data to corresponding table */
                String tableName = "activeTranslators";
                Map<String, String> schema = CommonRefs.tableSchemas.get(tableName);

                // Columns and placeholders for the INSERT part
                String columns = String.join(",", schema.keySet());
                String placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                // Dynamically create the ON CONFLICT DO UPDATE part
                String onConflictUpdate = schema.keySet().stream()
                        .filter(column -> !column.equals("playerUUID")) // Exclude the conflict target column
                        .map(column -> String.format("%s = EXCLUDED.%s", column, column))
                        .collect(Collectors.joining(", "));

                String sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (playerUUID) DO UPDATE SET %s",
                        tableName, columns, placeholders, onConflictUpdate);

                try (PreparedStatement newActiveTranslator = postgresConnection.prepareStatement(sqlStatement)) {
                    for (Map.Entry<String, ActiveTranslator> entry : main.getActiveTranslators().entrySet()) {
                        String key = entry.getKey();
                        ActiveTranslator val = entry.getValue();
                        if (!val.getHasBeenSaved()) {
                            int i = 1;
                            newActiveTranslator.setString(i++, Instant.now().toString());
                            newActiveTranslator.setString(i++, val.getUUID());
                            newActiveTranslator.setString(i++, val.getInLangCode());
                            newActiveTranslator.setString(i++, val.getOutLangCode());
                            newActiveTranslator.setInt(i++, val.getRateLimit());
                            newActiveTranslator.setString(i++, val.getRateLimitPreviousTime());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingChatOutgoing());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingChatIncoming());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingBook());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingSign());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingItem());
                            newActiveTranslator.setBoolean(i++, val.getTranslatingEntity());

                            // Add to batch
                            newActiveTranslator.addBatch();
                            refs.debugMsg("(Postgres) Prepared batch entry for " + key + ".");
                            val.setHasBeenSaved(true);
                        }
                    }

                    // Execute the batch
                    newActiveTranslator.executeBatch();
                    refs.debugMsg("(Postgres) Batch executed, data saved or updated.");
                }

                /* Delete any old ActiveTranslators */
                try (PreparedStatement deleteOldItem = postgresConnection.prepareStatement("DELETE FROM activeTranslators WHERE playerUUID = ?");
                     ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT playerUUID FROM activeTranslators")) {
                    while (rs.next()) {
                        if (!main.isActiveTranslator(rs.getString("playerUUID"))) {
                            String uuid = rs.getString("playerUUID");
                            deleteOldItem.setString(1, uuid);
                            deleteOldItem.addBatch();
                            refs.debugMsg("(Postgres) Prepared delete batch entry for " + uuid + ".");
                        }
                    }
                    // Execute all deletions in a single batch
                    deleteOldItem.executeBatch();
                    refs.debugMsg("(Postgres) Batch delete executed, old active translators removed.");
                }

                /* Sync PlayerRecord data to corresponding table */
                tableName = "playerRecords";
                schema = CommonRefs.tableSchemas.get(tableName);

                // Columns and placeholders for the INSERT part
                columns = String.join(",", schema.keySet());
                placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                // Dynamically create the ON CONFLICT DO UPDATE part
                onConflictUpdate = schema.keySet().stream()
                        .filter(column -> !column.equals("playerUUID")) // Exclude the conflict target column
                        .map(column -> String.format("%s = EXCLUDED.%s", column, column))
                        .collect(Collectors.joining(", "));

                sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (playerUUID) DO UPDATE SET %s",
                        tableName, columns, placeholders, onConflictUpdate);

                try (PreparedStatement newPlayerRecord = postgresConnection.prepareStatement(sqlStatement)) {
                    for (Map.Entry<String, PlayerRecord> entry : main.getPlayerRecords().entrySet()) {
                        String key = entry.getKey();
                        PlayerRecord val = entry.getValue();
                        if (!val.getHasBeenSaved()) {
                            int i = 1;
                            newPlayerRecord.setString(i++, Instant.now().toString());
                            newPlayerRecord.setString(i++, val.getUUID());
                            newPlayerRecord.setInt(i++, val.getAttemptedTranslations());
                            newPlayerRecord.setInt(i++, val.getSuccessfulTranslations());
                            newPlayerRecord.setString(i++, val.getLastTranslationTime());
                            newPlayerRecord.setString(i++, val.getLocalizationCode());

                            // Add to batch
                            newPlayerRecord.addBatch();
                            refs.debugMsg("(Postgres) Prepared batch entry for " + key + ".");
                            val.setHasBeenSaved(true);
                        }
                    }

                    // Execute the batch
                    newPlayerRecord.executeBatch();
                    refs.debugMsg("(Postgres) Batch executed, player records saved or updated.");
                }


                /* Sync Cache data to corresponding table */
                if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                    tableName = "persistentCache";
                    schema = CommonRefs.tableSchemas.get(tableName);

                    // Columns and placeholders for the INSERT part
                    columns = String.join(",", schema.keySet());
                    placeholders = schema.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

                    // Dynamically create the ON CONFLICT DO UPDATE part
                    onConflictUpdate = schema.keySet().stream()
                            .filter(column -> !column.equals("randomUUID")) // Exclude the conflict target column
                            .map(column -> String.format("%s = EXCLUDED.%s", column, column))
                            .collect(Collectors.joining(", "));

                    sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (randomUUID) DO UPDATE SET %s",
                            tableName, columns, placeholders, onConflictUpdate);

                    try (PreparedStatement newCacheTerm = postgresConnection.prepareStatement(sqlStatement)) {
                        for (Map.Entry<CachedTranslation, String> entry : main.getCache().asMap().entrySet()) {
                            CachedTranslation val = entry.getKey();
                            String value = entry.getValue();
                            if (!val.hasBeenSaved()) {
                                int i = 1;
                                newCacheTerm.setString(i++, UUID.randomUUID().toString());
                                newCacheTerm.setString(i++, val.getInputLang());
                                newCacheTerm.setString(i++, val.getOutputLang());
                                newCacheTerm.setString(i++, val.getInputPhrase());
                                newCacheTerm.setString(i++, value);

                                // Add to batch
                                newCacheTerm.addBatch();
                                refs.debugMsg("(Postgres) Prepared batch entry for cache data.");
                                val.setHasBeenSaved(true);
                            }
                        }

                        // Execute all updates/inserts in a single batch
                        newCacheTerm.executeBatch();
                        refs.debugMsg("(Postgres) Batch executed, cache data saved or updated.");
                    }

                    /* Delete Old Cache Terms */
                    try (PreparedStatement deleteOldItems = postgresConnection.prepareStatement(
                            "DELETE FROM persistentCache WHERE inputLang = ? AND outputLang = ? AND inputPhrase = ?")) {
                        ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM persistentCache");
                        while (rs.next()) {
                            String inputLang = rs.getString("inputLang");
                            String outputLang = rs.getString("outputLang");
                            String inputPhrase = rs.getString("inputPhrase");

                            if (!main.hasCacheTerm(new CachedTranslation(inputLang, outputLang, inputPhrase))) {
                                deleteOldItems.setString(1, inputLang);
                                deleteOldItems.setString(2, outputLang);
                                deleteOldItems.setString(3, inputPhrase);
                                deleteOldItems.addBatch();
                                refs.debugMsg("(Postgres) Prepared delete batch entry for cache entry.");
                            }
                        }

                        // Execute all deletions in a single batch
                        deleteOldItems.executeBatch();
                        refs.debugMsg("(Postgres) Batch delete executed, old cache entries removed.");
                    }
                }
            }
        } else if (main.isMongoConnValid(true)) {
            /* Initialize collections */
            MongoDatabase database = mongo.getActiveDatabase();
            MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
            MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");
            MongoCollection<Document> cacheCol = database.getCollection("PersistentCache");

            // Preparing bulk operations
            final List<WriteModel<Document>> writes = new ArrayList<>();

            /* Add New Active Translators */
            main.getActiveTranslators().forEach((key, val) -> {
                refs.debugMsg("(MongoDB) Translation data of " + key + " save status: " + val.getHasBeenSaved());
                if (!val.getHasBeenSaved()) {
                    Document currTranslator = new Document()
                            .append("creationDate", Instant.now().toString())
                            .append("playerUUID", val.getUUID())
                            .append("inLangCode", val.getInLangCode())
                            .append("outLangCode", val.getOutLangCode())
                            .append("rateLimit", val.getRateLimit())
                            .append("rateLimitPreviousTime", val.getRateLimitPreviousTime())
                            .append("translatingChatOutgoing", val.getTranslatingChatOutgoing())
                            .append("translatingChatIncoming", val.getTranslatingChatIncoming())
                            .append("translatingBook", val.getTranslatingBook())
                            .append("translatingSign", val.getTranslatingSign())
                            .append("translatingItem", val.getTranslatingItem())
                            .append("translatingEntity", val.getTranslatingEntity());

                    Bson filter = Filters.eq("playerUUID", val.getUUID());
                    ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
                    writes.add(new ReplaceOneModel<>(filter, currTranslator, replaceOptions));

                    val.setHasBeenSaved(true);
                }
            });

            if (!writes.isEmpty()) {
                activeTranslatorCol.bulkWrite(writes);
                refs.debugMsg("(MongoDB) Bulk operation executed, data saved or updated.");
            }

            /* Delete Old Active Translators */
            FindIterable<Document> iterDoc = activeTranslatorCol.find();
            final List<WriteModel<Document>> deleteWrites = new ArrayList<>();
            Consumer<Document> action = currDoc -> {
                String uuid = currDoc.getString("playerUUID");
                if (!main.isActiveTranslator(uuid)) {
                    Bson query = Filters.eq("playerUUID", uuid);
                    deleteWrites.add(new DeleteOneModel<>(query));
                    refs.debugMsg("(MongoDB) Prepared delete batch entry for " + uuid + ".");
                }
            };

            iterDoc.forEach(action);

            if (!deleteWrites.isEmpty()) {
                activeTranslatorCol.bulkWrite(deleteWrites);
                refs.debugMsg("(MongoDB) Bulk delete executed, old active translators removed.");
            }

            /* Write PlayerRecords to DB */
            final List<WriteModel<Document>> recordWrites = new ArrayList<>();
            main.getPlayerRecords().forEach((key, val) -> {
                refs.debugMsg("(MongoDB) Record of " + key + " save status: " + val.getHasBeenSaved());
                if (!val.getHasBeenSaved()) {
                    Document currPlayerRecord = new Document()
                            .append("creationDate", Instant.now().toString())
                            .append("playerUUID", val.getUUID())
                            .append("attemptedTranslations", val.getAttemptedTranslations())
                            .append("successfulTranslations", val.getSuccessfulTranslations())
                            .append("lastTranslationTime", val.getLastTranslationTime())
                            .append("localizationCode", val.getLocalizationCode());

                    Bson filter = Filters.eq("playerUUID", val.getUUID());
                    ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
                    recordWrites.add(new ReplaceOneModel<>(filter, currPlayerRecord, replaceOptions));

                    val.setHasBeenSaved(true);
                }
            });

            if (!recordWrites.isEmpty()) {
                playerRecordCol.bulkWrite(recordWrites);
                refs.debugMsg("(MongoDB) Bulk operation executed, player records saved or updated.");
            }

            /* Write Cache to DB */
            if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                final List<WriteModel<Document>> cacheWrites = new ArrayList<>();

                /* Add New Cache Terms */
                main.getCache().asMap().entrySet().forEach(entry -> {
                    refs.debugMsg("(MongoDB) Cached entry " + entry.getValue() + " save status: " + entry.getKey().hasBeenSaved());
                    if (!entry.getKey().hasBeenSaved()) {
                        CachedTranslation val = entry.getKey();
                        UUID random = UUID.randomUUID();
                        Document currCache = new Document()
                                .append("randomUUID", random.toString())
                                .append("inputLang", val.getInputLang())
                                .append("outputLang", val.getOutputLang())
                                .append("inputPhrase", val.getInputPhrase())
                                .append("outputPhrase", entry.getValue());

                        Bson filter = Filters.eq("randomUUID", random.toString());
                        ReplaceOptions replaceOptions = new ReplaceOptions().upsert(true);
                        cacheWrites.add(new ReplaceOneModel<>(filter, currCache, replaceOptions));

                        entry.getKey().setHasBeenSaved(true);
                    }
                });

                // Execute all updates/inserts in a single batch if there are entries to write
                if (!cacheWrites.isEmpty()) {
                    cacheCol.bulkWrite(cacheWrites);
                    refs.debugMsg("(MongoDB) Bulk operation executed, cache data saved or updated.");
                }

                /* Delete old Cache from DB */
                final List<WriteModel<Document>> deleteCacheWrites = new ArrayList<>();

                FindIterable<Document> cacheIterDoc = cacheCol.find();
                cacheIterDoc.forEach(document -> {
                    CachedTranslation temp = new CachedTranslation(document.getString("inputLang"), document.getString("outputLang"), document.getString("inputPhrase"));
                    if (!main.hasCacheTerm(temp)) {
                        String uuid = document.getString("randomUUID");
                        Bson query = Filters.eq("randomUUID", uuid);
                        deleteCacheWrites.add(new DeleteOneModel<>(query));
                        refs.debugMsg("(MongoDB) Prepared delete batch entry for cache term.");
                    }
                });

                // Execute all deletions in a single batch if there are entries to delete
                if (!deleteCacheWrites.isEmpty()) {
                    cacheCol.bulkWrite(deleteCacheWrites);
                    refs.debugMsg("(MongoDB) Bulk delete executed, old cache entries removed.");
                }
            }
        } else {
            /* Last resort, sync activeTranslators to disk via YAML */
            // Save all new activeTranslators
            main.getActiveTranslators().entrySet().forEach((entry) -> {
                refs.debugMsg("(YAML) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
                if (!entry.getValue().getHasBeenSaved()) {
                    refs.debugMsg("(YAML) Created/updated unsaved user data config of " + entry.getKey() + ".");
                    entry.getValue().setHasBeenSaved(true);
                    createUserDataConfig(entry.getValue());
                }
            });

            // Delete any old activeTranslators
            File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
            if (userSettingsDir.exists()) {
                for (String eaName : userSettingsDir.list()) {
                    if (!eaName.endsWith(".yml")) {
                        refs.debugMsg("NOT deleting current old active trans file: " + eaName);
                        continue;
                    }
                    File currFile = new File(userSettingsDir, eaName);
                    String fileUUID = currFile.getName().substring(0, currFile.getName().indexOf("."));
                    if (!main.isActiveTranslator(fileUUID)) {
                        refs.debugMsg("(YAML) Deleted user data config of "
                                + fileUUID + ".");
                        currFile.delete();
                    }
                }
            }

            /* Sync playerRecords to disk */
            main.getPlayerRecords().entrySet().forEach((entry) -> {
                refs.debugMsg("(YAML) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
                if (!entry.getValue().getHasBeenSaved()) {
                    refs.debugMsg("(YAML) Created/updated unsaved user record of " + entry.getKey() + ".");
                    entry.getValue().setHasBeenSaved(true);
                    createStatsConfig(entry.getValue());
                }
            });

            if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                /* Sync cache to disk */
                //main.getLogger().warning(refs.getMsg("wwcPersistentCacheLoad", null));
                main.getCache().asMap().entrySet().forEach((eaCache) -> {
                    if (!eaCache.getKey().hasBeenSaved()) {
                        refs.debugMsg("(YAML) Created/updated cache term " + eaCache.getValue());
                        eaCache.getKey().setHasBeenSaved(true);
                        createCacheConfig(eaCache.getKey(), eaCache.getValue());
                    }
                });

                /* Delete any old cache files */
                File cacheDir = new File(main.getDataFolder() + File.separator + "cache" + File.separator);
                if (cacheDir.exists()) {
                    for (String eaName : cacheDir.list()) {
                        if (!eaName.endsWith(".yml")) {
                            refs.debugMsg("NOT deleting current old cache file: " + eaName);
                            continue;
                        }
                        File currFile = new File(cacheDir, eaName);
                        try {
                            YamlConfiguration conf = YamlConfiguration.loadConfiguration(currFile);
                            CachedTranslation test = new CachedTranslation(conf.getString("inputLang"), conf.getString("outputLang"), conf.getString("inputPhrase"));
                            if (!main.hasCacheTerm(test)) {
                                refs.debugMsg("(YAML) Deleted cache term.");
                                currFile.delete();
                            }
                        } catch (Exception e) {
                            refs.debugMsg("Invalid cache file detected, ignoring (" + eaName + ")");
                        }
                    }
                }
            }
        }
    }

    // PSA:
    // Generally, NEVER USE THIS!
    // We only use this in /wwcd.
    public static void fullDataWipe() throws SQLException, MongoException {
        SQLUtils sql = main.getSqlSession();
        MongoDBUtils mongo = main.getMongoSession();
        PostgresUtils postgres = main.getPostgresSession();

        if (main.isSQLConnValid(true)) {
            // SQL database wipe
            try (Connection sqlConnection = sql.getConnection()) {
                /* Wipe ActiveTranslators */
                try (PreparedStatement deleteStatement = sqlConnection.prepareStatement("DELETE FROM activeTranslators")) {
                    int rowsDeleted = deleteStatement.executeUpdate();
                    refs.debugMsg("(SQL) Wiped " + rowsDeleted + " records from ActiveTranslators.");
                }

                /* Wipe PlayerRecords */
                try (PreparedStatement deleteStatement = sqlConnection.prepareStatement("DELETE FROM playerRecords")) {
                    int rowsDeleted = deleteStatement.executeUpdate();
                    refs.debugMsg("(SQL) Wiped " + rowsDeleted + " records from PlayerRecords.");
                }

                /* Wipe Cache */
                if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                    try (PreparedStatement deleteStatement = sqlConnection.prepareStatement("DELETE FROM persistentCache")) {
                        int rowsDeleted = deleteStatement.executeUpdate();
                        refs.debugMsg("(SQL) Wiped " + rowsDeleted + " records from PersistentCache.");
                    }
                }
            }
        } else if (main.isPostgresConnValid(true)) {
            // Postgres database wipe
            try (Connection postgresConnection = postgres.getConnection()) {
                /* Wipe ActiveTranslators */
                try (PreparedStatement deleteStatement = postgresConnection.prepareStatement("DELETE FROM activeTranslators")) {
                    int rowsDeleted = deleteStatement.executeUpdate();
                    refs.debugMsg("(Postgres) Wiped " + rowsDeleted + " records from ActiveTranslators.");
                }

                /* Wipe PlayerRecords */
                try (PreparedStatement deleteStatement = postgresConnection.prepareStatement("DELETE FROM playerRecords")) {
                    int rowsDeleted = deleteStatement.executeUpdate();
                    refs.debugMsg("(Postgres) Wiped " + rowsDeleted + " records from PlayerRecords.");
                }

                /* Wipe Cache */
                if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                    try (PreparedStatement deleteStatement = postgresConnection.prepareStatement("DELETE FROM persistentCache")) {
                        int rowsDeleted = deleteStatement.executeUpdate();
                        refs.debugMsg("(Postgres) Wiped " + rowsDeleted + " records from PersistentCache.");
                    }
                }
            }
        } else if (main.isMongoConnValid(true)) {
            // MongoDB wipe
            MongoDatabase database = mongo.getActiveDatabase();
            MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
            MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");
            MongoCollection<Document> cacheCol = database.getCollection("PersistentCache");

            /* Wipe ActiveTranslators */
            DeleteResult result = activeTranslatorCol.deleteMany(new Document());
            refs.debugMsg("(MongoDB) Wiped " + result.getDeletedCount() + " records from ActiveTranslators.");

            /* Wipe PlayerRecords */
            result = playerRecordCol.deleteMany(new Document());
            refs.debugMsg("(MongoDB) Wiped " + result.getDeletedCount() + " records from PlayerRecords.");

            /* Wipe Cache */
            if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
                result = cacheCol.deleteMany(new Document());
                refs.debugMsg("(MongoDB) Wiped " + result.getDeletedCount() + " records from PersistentCache.");
            }
        } else {
            // Last resort: Wipe from disk storage via YAML
            File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
            if (userSettingsDir.exists()) {
                for (File file : userSettingsDir.listFiles((dir, name) -> name.endsWith(".yml"))) {
                    if (file.delete()) {
                        refs.debugMsg("(YAML) Deleted user data config file: " + file.getName());
                    }
                }
            }

            File playerRecordsDir = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
            if (playerRecordsDir.exists()) {
                for (File file : playerRecordsDir.listFiles((dir, name) -> name.endsWith(".yml"))) {
                    if (file.delete()) {
                        refs.debugMsg("(YAML) Deleted user record config file: " + file.getName());
                    }
                }
            }

            File cacheDir = new File(main.getDataFolder() + File.separator + "cache" + File.separator);
            if (cacheDir.exists()) {
                for (File file : cacheDir.listFiles((dir, name) -> name.endsWith(".yml"))) {
                    if (file.delete()) {
                        refs.debugMsg("(YAML) Deleted cache config file: " + file.getName());
                    }
                }
            }
        }
    }

    /* Translator YAML File Saver */
    public static void createUserDataConfig(ActiveTranslator inTranslator) {
        File userSettingsFile;
        YamlConfiguration userSettingsConfig;
        userSettingsFile = new File(main.getDataFolder() + File.separator + "data" + File.separator,
                inTranslator.getUUID() + ".yml");

        /* Load config */
        userSettingsConfig = YamlConfiguration.loadConfiguration(userSettingsFile);

        /* Set data */
        userSettingsConfig.createSection("inLang");
        userSettingsConfig.set("inLang", inTranslator.getInLangCode());

        userSettingsConfig.createSection("outLang");
        userSettingsConfig.set("outLang", inTranslator.getOutLangCode());

        userSettingsConfig.createSection("bookTranslation");
        userSettingsConfig.set("bookTranslation", inTranslator.getTranslatingBook());

        userSettingsConfig.createSection("signTranslation");
        userSettingsConfig.set("signTranslation", inTranslator.getTranslatingSign());

        userSettingsConfig.createSection("itemTranslation");
        userSettingsConfig.set("itemTranslation", inTranslator.getTranslatingItem());

        userSettingsConfig.createSection("entityTranslation");
        userSettingsConfig.set("entityTranslation", inTranslator.getTranslatingEntity());

        userSettingsConfig.createSection("chatTranslationOutgoing");
        userSettingsConfig.set("chatTranslationOutgoing", inTranslator.getTranslatingChatOutgoing());

        userSettingsConfig.createSection("chatTranslationIncoming");
        userSettingsConfig.set("chatTranslationIncoming", inTranslator.getTranslatingChatIncoming());

        userSettingsConfig.createSection("rateLimit");
        userSettingsConfig.set("rateLimit", inTranslator.getRateLimit());

        userSettingsConfig.createSection("rateLimitPreviousRecordedTime");
        userSettingsConfig.set("rateLimitPreviousRecordedTime", inTranslator.getRateLimitPreviousTime());

        handler.saveCustomConfig(userSettingsConfig, userSettingsFile, false);
    }

    /* Stats YAML File Saver */
    public static void createStatsConfig(PlayerRecord inRecord) {
        File userStatsDir = new File(main.getDataFolder() + File.separator + "stats");
        File userStatsFile;
        YamlConfiguration userStatsConfig;

        userStatsFile = new File(userStatsDir + File.separator,
                inRecord.getUUID() + ".yml");

        /* Load config */
        userStatsConfig = YamlConfiguration.loadConfiguration(userStatsFile);

        /* Set data */
        userStatsConfig.createSection("lastTranslationTime");
        userStatsConfig.set("lastTranslationTime", inRecord.getLastTranslationTime());

        userStatsConfig.createSection("attemptedTranslations");
        userStatsConfig.set("attemptedTranslations", inRecord.getAttemptedTranslations());

        userStatsConfig.createSection("successfulTranslations");
        userStatsConfig.set("successfulTranslations", inRecord.getSuccessfulTranslations());

        userStatsConfig.createSection("localizationCode");
        userStatsConfig.set("localizationCode", inRecord.getLocalizationCode());

        handler.saveCustomConfig(userStatsConfig, userStatsFile, false);
    }

    /* Cache YAML File Saver */
    public static void createCacheConfig(CachedTranslation trans, String out) {
        File cacheDir = new File(main.getDataFolder() + File.separator + "cache");
        File cacheFile;
        YamlConfiguration cacheConfig;

        cacheFile = new File(cacheDir + File.separator,
                UUID.randomUUID() + ".yml");

        cacheConfig = YamlConfiguration.loadConfiguration(cacheFile);

        /* Set data */
        cacheConfig.createSection("inputLang");
        cacheConfig.set("inputLang", trans.getInputLang());

        cacheConfig.createSection("outputLang");
        cacheConfig.set("outputLang", trans.getOutputLang());

        cacheConfig.createSection("inputPhrase");
        cacheConfig.set("inputPhrase", trans.getInputPhrase());

        cacheConfig.createSection("outputPhrase");
        cacheConfig.set("outputPhrase", out);

        handler.saveCustomConfig(cacheConfig, cacheFile, false);
    }
}
