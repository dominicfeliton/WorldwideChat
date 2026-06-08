package com.dominicfeliton.worldwidechat.runnables;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.storage.MongoDBUtils;
import com.dominicfeliton.worldwidechat.util.storage.PostgresUtils;
import com.dominicfeliton.worldwidechat.util.storage.SQLUtils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class LoadUserData {

    private WorldwideChat main = WorldwideChat.instance;

    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private MongoDBUtils mongo = main.getMongoSession();

    private SQLUtils sql = main.getSqlSession();

    private PostgresUtils postgres = main.getPostgresSession();

    // USE LOCALLY PASSED TRANSLATOR NAME.
    private String transName;

    public LoadUserData(String transName) {

        this.transName = transName;
        loadData();
    }

    private void loadData() {
        //TODO: Sanitize for bad inputs/accommodate for obj upgrades; if data is bad, we definitely shouldn't add it
        /* Load all saved user data */
        refs.debugMsg("Starting LoadUserData!!!");
        YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
        File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
        File statsFolder = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
        File cacheFolder = new File(main.getDataFolder() + File.separator + "cache" + File.separator);

        if (!userDataFolder.exists() && !userDataFolder.mkdir()) {
            main.getLogger().severe(refs.getPlainMsg("wwcLoadUserDataFolderFail"));
            main.getServer().getPluginManager().disablePlugin(main);
            return;
        }

        if (!statsFolder.exists() && !statsFolder.mkdir()) {
            main.getLogger().severe(refs.getPlainMsg("wwcLoadStatsFolderFail"));
            main.getServer().getPluginManager().disablePlugin(main);
            return;
        }

        if (!cacheFolder.exists() && !cacheFolder.mkdir()) {
            main.getLogger().severe(refs.getPlainMsg("wwcLoadCacheFolderFail"));
            main.getServer().getPluginManager().disablePlugin(main);
            return;
        }

        /* Load user records (/wwcs) */
        refs.debugMsg("Loading user records or /wwcs...");
        if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
            try (Connection sqlConnection = sql.getConnection()) {
                // Load PlayerRecord using SQL
                try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM playerRecords")) {
                    while (rs.next()) {
                        PlayerRecord recordToAdd = new PlayerRecord(
                                rs.getString("lastTranslationTime"),
                                rs.getString("playerUUID"),
                                rs.getInt("attemptedTranslations"),
                                rs.getInt("successfulTranslations")
                        );
                        recordToAdd.setLocalizationCode("");
                        if (rs.getString("localizationCode") != null &&
                                !rs.getString("localizationCode").isEmpty() &&
                                refs.isSupportedLang(rs.getString("localizationCode"), CommonRefs.LangType.LOCAL)) {
                            recordToAdd.setLocalizationCode(rs.getString("localizationCode"));
                        }
                        recordToAdd.setHasBeenSaved(true);
                        main.addPlayerRecord(recordToAdd);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                main.getServer().getPluginManager().disablePlugin(main);
                return;
            }
        } else if (mainConfig.getBoolean("Storage.useMongoDB") && main.isMongoConnValid(false)) {
            MongoDatabase database = mongo.getActiveDatabase();
            MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");

            // Load PlayerRecord using MongoDB
            FindIterable<Document> iterDoc = playerRecordCol.find();
            Iterator<Document> it = iterDoc.iterator();
            while (it.hasNext()) {
                Document currDoc = it.next();
                PlayerRecord recordToAdd = new PlayerRecord(
                        currDoc.getString("lastTranslationTime"),
                        currDoc.getString("playerUUID"),
                        currDoc.getInteger("attemptedTranslations"),
                        currDoc.getInteger("successfulTranslations")
                );
                recordToAdd.setLocalizationCode("");
                if (currDoc.getString("localizationCode") != null && !currDoc.getString("localizationCode").isEmpty()) {
                    recordToAdd.setLocalizationCode(currDoc.getString("localizationCode"));
                }
                recordToAdd.setHasBeenSaved(true);
                main.addPlayerRecord(recordToAdd);
            }
        } else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
            try (Connection postgresConnection = postgres.getConnection()) {
                // Load PlayerRecord using Postgres
                try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM playerRecords")) {
                    while (rs.next()) {
                        PlayerRecord recordToAdd = new PlayerRecord(
                                rs.getString("lastTranslationTime"),
                                rs.getString("playerUUID"),
                                rs.getInt("attemptedTranslations"),
                                rs.getInt("successfulTranslations")
                        );
                        recordToAdd.setLocalizationCode("");
                        if (rs.getString("localizationCode") != null &&
                                !rs.getString("localizationCode").isEmpty() &&
                                refs.isSupportedLang(rs.getString("localizationCode"), CommonRefs.LangType.LOCAL)) {
                            recordToAdd.setLocalizationCode(rs.getString("localizationCode"));
                        }
                        recordToAdd.setHasBeenSaved(true);
                        main.addPlayerRecord(recordToAdd);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                main.getServer().getPluginManager().disablePlugin(main);
                return;
            }
        } else {
            for (File eaFile : statsFolder.listFiles()) {
                if (!(eaFile.isFile() && eaFile.getName().endsWith(".yml"))) {
                    refs.debugMsg("invalid record file: " + eaFile.getName());
                    continue;
                }

                // Load current file
                YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
                try {
                    Reader currConfigStream = new InputStreamReader(main.getResource("default-player-record.yml"), "UTF-8");
                    currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
                } catch (Exception e) {
                    main.getLogger().warning(refs.getPlainMsg("wwcConfigBadYaml", eaFile.getAbsolutePath()));
                    continue;
                }
                currFileConfig.options().copyDefaults(true);

                // Create new PlayerRecord from current file
                main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
                PlayerRecord currRecord = new PlayerRecord(currFileConfig.getString("lastTranslationTime"),
                        eaFile.getName().substring(0, eaFile.getName().indexOf(".")),
                        currFileConfig.getInt("attemptedTranslations"),
                        currFileConfig.getInt("successfulTranslations"));
                currRecord.setLocalizationCode("");
                if (currFileConfig.getString("localizationCode") != null &&
                        !currFileConfig.getString("localizationCode").isEmpty() &&
                        refs.isSupportedLang(currFileConfig.getString("localizationCode"), CommonRefs.LangType.LOCAL)) {
                    currRecord.setLocalizationCode(currFileConfig.getString("localizationCode"));
                }
                currRecord.setHasBeenSaved(true);
                main.addPlayerRecord(currRecord);
            }
        }

        /* If translator settings are invalid, do not do anything else... */
        if (transName.equalsIgnoreCase("Invalid")) {
            return;
        }

        /* Load user files (last translation session, etc.) */
        refs.debugMsg("Loading user data or /wwct...");
        if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
            try (Connection sqlConnection = sql.getConnection()) {
                // Load ActiveTranslator using SQL
                try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
                    while (rs.next()) {
                        String inLang = rs.getString("inLangCode");
                        String outLang = rs.getString("outLangCode");
                        if (!validLangCodes(inLang, outLang)) {
                            inLang = "en";
                            outLang = "es";
                        }
                        ActiveTranslator translatorToAdd = new ActiveTranslator(
                                rs.getString("playerUUID"),
                                inLang,
                                outLang
                        );
                        applyRateLimitPreviousTime(translatorToAdd, rs.getString("rateLimitPreviousTime"));
                        translatorToAdd.setTranslatingChatOutgoing(rs.getBoolean("translatingChatOutgoing"));
                        translatorToAdd.setTranslatingChatIncoming(rs.getBoolean("translatingChatIncoming"));
                        translatorToAdd.setTranslatingBook(rs.getBoolean("translatingBook"));
                        translatorToAdd.setTranslatingSign(rs.getBoolean("translatingSign"));
                        translatorToAdd.setTranslatingItem(rs.getBoolean("translatingItem"));
                        translatorToAdd.setTranslatingEntity(rs.getBoolean("translatingEntity"));
                        translatorToAdd.setRateLimit(rs.getInt("rateLimit"));
                        translatorToAdd.setHasBeenSaved(true);
                        main.addActiveTranslator(translatorToAdd);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                main.getServer().getPluginManager().disablePlugin(main);
                return;
            }
        } else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
            try (Connection postgresConnection = postgres.getConnection()) {
                // Load ActiveTranslator using Postgres
                try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
                    while (rs.next()) {
                        String inLang = rs.getString("inLangCode");
                        String outLang = rs.getString("outLangCode");
                        if (!validLangCodes(inLang, outLang)) {
                            inLang = "en";
                            outLang = "es";
                        }
                        ActiveTranslator translatorToAdd = new ActiveTranslator(
                                rs.getString("playerUUID"),
                                inLang,
                                outLang
                        );
                        applyRateLimitPreviousTime(translatorToAdd, rs.getString("rateLimitPreviousTime"));
                        translatorToAdd.setTranslatingChatOutgoing(rs.getBoolean("translatingChatOutgoing"));
                        translatorToAdd.setTranslatingChatIncoming(rs.getBoolean("translatingChatIncoming"));
                        translatorToAdd.setTranslatingBook(rs.getBoolean("translatingBook"));
                        translatorToAdd.setTranslatingSign(rs.getBoolean("translatingSign"));
                        translatorToAdd.setTranslatingItem(rs.getBoolean("translatingItem"));
                        translatorToAdd.setTranslatingEntity(rs.getBoolean("translatingEntity"));
                        translatorToAdd.setRateLimit(rs.getInt("rateLimit"));
                        translatorToAdd.setHasBeenSaved(true);
                        main.addActiveTranslator(translatorToAdd);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                main.getServer().getPluginManager().disablePlugin(main);
                return;
            }
        } else if (mainConfig.getBoolean("Storage.useMongoDB") && main.isMongoConnValid(false)) {
            // Load Active Translator using MongoDB
            MongoDatabase database = mongo.getActiveDatabase();
            MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
            FindIterable<Document> iterDoc = activeTranslatorCol.find();
            Iterator<Document> it = iterDoc.iterator();
            while (it.hasNext()) {
                Document currDoc = it.next();
                String inLang = currDoc.getString("inLangCode");
                String outLang = currDoc.getString("outLangCode");
                if (!validLangCodes(inLang, outLang)) {
                    inLang = "en";
                    outLang = "es";
                }
                ActiveTranslator translatorToAdd = new ActiveTranslator(
                        currDoc.getString("playerUUID"),
                        inLang,
                        outLang
                );
                applyRateLimitPreviousTime(translatorToAdd, currDoc.getString("rateLimitPreviousTime"));
                translatorToAdd.setTranslatingChatOutgoing(currDoc.getBoolean("translatingChatOutgoing"));
                translatorToAdd.setTranslatingChatIncoming(currDoc.getBoolean("translatingChatIncoming"));
                translatorToAdd.setTranslatingBook(currDoc.getBoolean("translatingBook"));
                translatorToAdd.setTranslatingSign(currDoc.getBoolean("translatingSign"));
                translatorToAdd.setTranslatingItem(currDoc.getBoolean("translatingItem"));
                translatorToAdd.setTranslatingEntity(currDoc.getBoolean("translatingEntity"));
                translatorToAdd.setRateLimit(currDoc.getInteger("rateLimit"));
                translatorToAdd.setHasBeenSaved(true);
                main.addActiveTranslator(translatorToAdd);
            }
        } else {
            for (File eaFile : userDataFolder.listFiles()) {
                if (!(eaFile.isFile() && eaFile.getName().endsWith(".yml"))) {
                    refs.debugMsg("invalid active trans file: " + eaFile.getName());
                    continue;
                }

                // Load current user translation file
                YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
                try {
                    Reader currConfigStream = new InputStreamReader(main.getResource("default-active-translator.yml"), "UTF-8");
                    currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
                } catch (Exception e) {
                    main.getLogger().warning(refs.getPlainMsg("wwcConfigBadYaml", eaFile.getAbsolutePath()));
                    continue;
                }
                currFileConfig.options().copyDefaults(true);

                /* Sanity checks on inLang and outLang */
                String inLang = currFileConfig.getString("inLang");
                String outLang = currFileConfig.getString("outLang");
                if (!validLangCodes(inLang, outLang)) {
                    currFileConfig.set("inLang", "en");
                    currFileConfig.set("outLang", "es");

                    main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
                }

                // Create new ActiveTranslator with current file data
                ActiveTranslator currentTranslator = new ActiveTranslator(
                        eaFile.getName().substring(0, eaFile.getName().indexOf(".")), // add active translator to arraylist
                        currFileConfig.getString("inLang"), currFileConfig.getString("outLang"));
                currentTranslator.setTranslatingSign(currFileConfig.getBoolean("signTranslation"));
                currentTranslator.setTranslatingBook(currFileConfig.getBoolean("bookTranslation"));
                currentTranslator.setTranslatingItem(currFileConfig.getBoolean("itemTranslation"));
                currentTranslator.setTranslatingEntity(currFileConfig.getBoolean("entityTranslation"));
                currentTranslator.setTranslatingChatOutgoing(currFileConfig.getBoolean("chatTranslationOutgoing"));
                currentTranslator.setTranslatingChatIncoming(currFileConfig.getBoolean("chatTranslationIncoming"));
                currentTranslator.setRateLimit(currFileConfig.getInt("rateLimit"));
                applyRateLimitPreviousTime(currentTranslator, currFileConfig.getString("rateLimitPreviousRecordedTime"));
                currentTranslator.setHasBeenSaved(true);
                main.addActiveTranslator(currentTranslator);
            }
        }

        /* Load Persistent Cache (if enabled) */
        if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && main.isPersistentCache()) {
            refs.debugMsg("Loading persistent cache data...");
            if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
                // Load Cached Terms using SQL
                try (Connection sqlConnection = sql.getConnection()) {
                    try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM persistentCache")) {
                        while (rs.next()) {
                            String inLang = rs.getString("inputLang");
                            String outLang = rs.getString("outputLang");
                            if (!validLangCodes(inLang, outLang)) {
                                continue;
                            }
                            CachedTranslation cacheTmp = new CachedTranslation(
                                    inLang,
                                    outLang,
                                    rs.getString("inputPhrase")
                            );
                            cacheTmp.setHasBeenSaved(true);
                            main.addCacheTerm(cacheTmp, rs.getString("outputPhrase"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    main.getServer().getPluginManager().disablePlugin(main);
                    return;
                }
            } else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
                try (Connection postgresConnection = postgres.getConnection()) {
                    // Load Cached Terms using Postgres
                    try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM persistentCache")) {
                        while (rs.next()) {
                            String inLang = rs.getString("inputLang");
                            String outLang = rs.getString("outputLang");
                            if (!validLangCodes(inLang, outLang)) {
                                continue;
                            }
                            CachedTranslation cacheTmp = new CachedTranslation(
                                    inLang,
                                    outLang,
                                    rs.getString("inputPhrase")
                            );
                            cacheTmp.setHasBeenSaved(true);
                            main.addCacheTerm(cacheTmp, rs.getString("outputPhrase"));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    main.getServer().getPluginManager().disablePlugin(main);
                    return;
                }
            } else if (mainConfig.getBoolean("Storage.useMongoDB") && main.isMongoConnValid(false)) {
                // Load Cached Terms using MongoDB
                MongoDatabase database = mongo.getActiveDatabase();
                MongoCollection<Document> cacheCol = database.getCollection("PersistentCache");
                FindIterable<Document> iterDoc = cacheCol.find();
                Iterator<Document> it = iterDoc.iterator();
                while (it.hasNext()) {
                    Document currDoc = it.next();
                    String inLang = currDoc.getString("inputLang");
                    String outLang = currDoc.getString("outputLang");
                    if (!validLangCodes(inLang, outLang)) {
                        continue;
                    }
                    CachedTranslation cacheTmp = new CachedTranslation(
                            inLang,
                            outLang,
                            currDoc.getString("inputPhrase")
                    );
                    cacheTmp.setHasBeenSaved(true);
                    main.addCacheTerm(cacheTmp, currDoc.getString("outputPhrase"));
                }
            } else {
                for (File eaFile : cacheFolder.listFiles()) {
                    if (!(eaFile.isFile() && eaFile.getName().endsWith(".yml"))) {
                        refs.debugMsg("invalid cache file: " + eaFile.getName());
                        continue;
                    }

                    // Load current user translation file
                    YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
                    try {
                        Reader currConfigStream = new InputStreamReader(main.getResource("default-persistent-cache.yml"), "UTF-8");
                        currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
                    } catch (Exception e) {
                        main.getLogger().warning(refs.getPlainMsg("wwcConfigBadYaml", eaFile.getAbsolutePath()));
                        continue;
                    }
                    currFileConfig.options().copyDefaults(true);

                    /* Sanity checks on inLang and outLang */
                    String inLang = currFileConfig.getString("inputLang");
                    String outLang = currFileConfig.getString("outputLang");
                    if (!validLangCodes(inLang, outLang)) {
                        continue;
                    }
                    String inputPhrase = currFileConfig.getString("inputPhrase");
                    String outputPhrase = currFileConfig.getString("outputPhrase");

                    // Create new CachedTranslation with current file data
                    CachedTranslation currCache = new CachedTranslation(
                            inLang, outLang, inputPhrase
                    );
                    currCache.setHasBeenSaved(true);
                    main.addCacheTerm(currCache, outputPhrase);
                }
            }
        }

        main.getLogger().info(refs.getPlainMsg("wwcUserDataReloaded",
                "",
                "&d"));
    }

    private boolean validLangCodes(String inLang, String outLang) {
        if (inLang == null || outLang == null) {
            return false;
        }
        // If inLang is invalid, or None is associated with Amazon Translate
        if ((!inLang.equalsIgnoreCase("None") && !refs.isSupportedLang(inLang, CommonRefs.LangType.LOCAL))
                || (inLang.equalsIgnoreCase("None") && transName.equalsIgnoreCase("Amazon Translate"))) {
            return false;
        }
        // If outLang code is not supported with current translator
        if (!refs.isSupportedLang(outLang, CommonRefs.LangType.OUTPUT)) {
            return false;
        }
        // If inLang and outLang codes are equal
        if (refs.getSupportedLang(outLang, CommonRefs.LangType.OUTPUT) == null
                || refs.getSupportedLang(inLang, CommonRefs.LangType.INPUT) == null
                || refs.getSupportedLang(outLang, CommonRefs.LangType.OUTPUT).getLangCode().equals(refs.getSupportedLang(inLang, CommonRefs.LangType.INPUT).getLangCode())) {
            refs.debugMsg("Langs are the same?");
            return false;
        }
        return true;
    }

    private void applyRateLimitPreviousTime(ActiveTranslator translator, String value) {
        Instant parsed = parseInstantOrNull(value);
        if (parsed != null) {
            translator.setRateLimitPreviousTime(parsed);
        }
    }

    private Instant parseInstantOrNull(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("None")) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

}
