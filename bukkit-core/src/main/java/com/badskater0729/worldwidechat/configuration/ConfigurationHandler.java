package com.badskater0729.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.badskater0729.worldwidechat.util.*;
import com.badskater0729.worldwidechat.util.storage.PostgresUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.mongodb.Block;
import com.mongodb.client.model.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.storage.MongoDBUtils;
import com.badskater0729.worldwidechat.util.storage.SQLUtils;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.badskater0729.worldwidechat.util.CommonRefs.pluginLangConfigs;
import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class ConfigurationHandler {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private File configFile;
	private YamlConfiguration mainConfig;

	/* Init Main Config Method */
	public void initMainConfig() {
		/* Init config file */
		configFile = new File(main.getDataFolder(), "config.yml");

		/* Generate config file, if it does not exist */
		if (!configFile.exists()) {
			main.saveResource("config.yml", false);
		}

		/* Load main config */
		mainConfig = YamlConfiguration.loadConfiguration(configFile);

		/* Add default options, if they do not exist */
		Reader mainConfigStream = new InputStreamReader(main.getResource("config.yml"), StandardCharsets.UTF_8);
		mainConfig.setDefaults(YamlConfiguration.loadConfiguration(mainConfigStream));

		mainConfig.options().copyDefaults(true);
		saveMainConfig(false);

		/* Get plugin lang */
		if (refs.isSupportedLang(mainConfig.getString("General.pluginLang"), "local")) {
			main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + mainConfig.getString("General.pluginLang") + ".");
			return;
		}

		mainConfig.set("General.pluginLang", "en");
		main.getLogger().warning("Unable to detect a valid language in your config.yml. Defaulting to en...");
	}

	/* Init Messages Method */
	public void initMessagesConfigs() {
		// Init ALL message configs
		main.getLogger().warning("Importing/upgrading localization files...");
		for (String eaStr : supportedPluginLangCodes.keySet()) {
			refs.debugMsg("Checking " + eaStr + "...");
			pluginLangConfigs.put(eaStr, generateMessagesConfig(eaStr));
		}
		main.getLogger().warning("Done.");
	}

	public YamlConfiguration generateMessagesConfig(String inLocalLang) {
		/* Init config file */
		File msgFile = new File(main.getDataFolder(), "messages-" + inLocalLang + ".yml");

		/* Save default messages file if it does not exist */
		if (!msgFile.exists()) {
			main.saveResource("messages-" + inLocalLang + ".yml", true);

			YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(msgFile);

			tempConfig.set("DoNotTouchThis.Version", WorldwideChat.messagesConfigVersion);

			saveCustomConfig(tempConfig, msgFile, false);
		}

		/* Load config */
		YamlConfiguration msgConfig = YamlConfiguration.loadConfiguration(msgFile);

		/* Check if version value is out of date...*/
		if (msgConfig.getString("DoNotTouchThis.Version") == null || !msgConfig.getString("DoNotTouchThis.Version").equals(WorldwideChat.messagesConfigVersion)) {
			refs.debugMsg("Upgrading out-of-date messages config!");
			HashMap<String, String> oldOverrides = new HashMap<>();

			/* Copy overrides section */
			if (msgConfig.getConfigurationSection("Overrides") != null) {
				for (String eaKey : msgConfig.getConfigurationSection("Overrides").getKeys(true)) {
					oldOverrides.put(eaKey, msgConfig.getString("Overrides." + eaKey));
				}
			}

			/* Delete old config */
			msgFile.delete();

			/* Copy newest config */
			main.saveResource("messages-" + inLocalLang + ".yml", true);
			msgConfig = YamlConfiguration.loadConfiguration(msgFile);
			msgConfig.set("DoNotTouchThis.Version", WorldwideChat.messagesConfigVersion);

			/* Paste overrides section */
			if (!oldOverrides.isEmpty()) {
				for (Map.Entry<String, String> entry : oldOverrides.entrySet()) {
					msgConfig.set("Overrides." + entry.getKey(), entry.getValue());
				}
			}

			/* Save messages config */
			// TODO: Perhaps make sure this method is ran async in /wwcl
			saveCustomConfig(msgConfig, msgFile, false);

			/* Success :) */
			refs.debugMsg("Upgrade successful.");
		}

		return msgConfig;
	}

	public YamlConfiguration getCustomMessagesConfig(String inLocalLang) {
		return pluginLangConfigs.get(inLocalLang);
	}

	/* Load Main Settings Method */
	public void loadMainSettings() {
		/* Get rest of General Settings */
		// Debug Mode
		if (mainConfig.getBoolean("General.enableDebugMode")) {
			main.getLogger().warning(refs.getMsg("wwcConfigEnabledDebugMode", null));
		}
		// Prefix
		try {
			if (!mainConfig.getString("General.prefixName").equalsIgnoreCase("Default")
					&& !mainConfig.getString("General.prefixName").equalsIgnoreCase("WWC")) {
				main.setPrefixName(mainConfig.getString("General.prefixName"));
			} else {
				main.setPrefixName("WWC"); // If default the entry for prefix, interpret as WWC
			}
		} catch (Exception e) {
			main.setPrefixName("WWC");
			main.getLogger().warning(refs.getMsg("wwcConfigInvalidPrefixSettings", null));
		}
		// Fatal Async Timeout Delay
		try {
			if (mainConfig.getInt("General.fatalAsyncTaskTimeout") > 7) {
				WorldwideChat.translatorFatalAbortSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout");
				WorldwideChat.translatorConnectionTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
				WorldwideChat.asyncTasksTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
			} else {
				main.getLogger().warning(refs.getMsg("wwcConfigInvalidFatalAsyncTimeout", null));
			}
		} catch (Exception e) {
			main.getLogger().warning(refs.getMsg("wwcConfigInvalidFatalAsyncTimeout", null));
		}
		// bStats
		if (mainConfig.getBoolean("General.enablebStats")) {
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigEnabledbStats", null));
		} else {
			main.getLogger().warning(refs.getMsg("wwcConfigDisabledbStats", null));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				mainConfig.set("General.updateCheckerDelay", 86400);
				main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay", null));
			}
		} catch (Exception e) {
			mainConfig.set("General.updateCheckerDelay", 86400);
			main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay", null));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigSyncDelayEnabled", mainConfig.getInt("General.syncUserDataDelay") + "", null));
			} else {
				mainConfig.set("General.syncUserDataDelay", 7200);
				main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid", null));
			}
		} catch (Exception e) {
			mainConfig.set("General.syncUserDataDelay", 7200);
			main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid", null));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigRateLimitEnabled", "" + mainConfig.getInt("Translator.rateLimit"), null));
			} else {
				mainConfig.set("Translator.rateLimit", 0);
				main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid", null));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.rateLimit", 0);
			main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid", null));
		}
		// Per-message char limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigMessageCharLimitEnabled", "" + mainConfig.getInt("Translator.messageCharLimit"), null));
			} else {
				mainConfig.set("Translator.messageCharLimit", 255);
				main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid", null));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.messageCharLimit", 255);
			main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid", null));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigCacheEnabled", mainConfig.getInt("Translator.translatorCacheSize") + "", null));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				mainConfig.set("Translator.translatorCacheSize", 0);
				main.setCacheProperties(0);
				main.getLogger().warning(refs.getMsg("wwcConfigCacheDisabled", null));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.translatorCacheSize", 100);
			main.setCacheProperties(100);
			main.getLogger().warning(refs.getMsg("wwcConfigCacheInvalid", null));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorLimitEnabled", mainConfig.getInt("Translator.errorLimit") + "", null));
			} else {
				mainConfig.set("Translator.errorLimit", 5);
				main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid", null));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.errorLimit", 5);
			main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid", null));
		}
		// List of Errors to Ignore Settings
		try {
			ArrayList<String> errorsToIgnore = (ArrayList<String>) mainConfig.getList("Storage.errorsToIgnore");
			main.getLogger().info(
					ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorsToIgnoreSuccess", null));
		} catch (Exception e) {
			mainConfig.set("Storage.errorsToIgnore", Arrays.asList("confidence", "same as target", "detect the source language"));
			main.getLogger().warning(refs.getMsg("wwcConfigErrorsToIgnoreInvalid", null));
		}
	}
	
	/* Storage Settings */
	public void loadStorageSettings() {
		if (mainConfig.getBoolean("Storage.useSQL")) {
			try {
				SQLUtils sql = new SQLUtils(mainConfig.getString("Storage.sqlHostname"), mainConfig.getString("Storage.sqlPort"),
						mainConfig.getString("Storage.sqlDatabaseName"), mainConfig.getString("Storage.sqlUsername"), mainConfig.getString("Storage.sqlPassword"),
						(List<String>) mainConfig.getList("Storage.sqlOptionalArgs"), mainConfig.getBoolean("Storage.sqlUseSSL"));
				sql.connect();
				main.setSqlSession(sql);
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "SQL", null));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "SQL", null));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getSqlSession() != null) {
					main.getSqlSession().disconnect();
					main.setSqlSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback", null));
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB")) {
			try {
				MongoDBUtils mongo = new MongoDBUtils(mainConfig.getString("Storage.mongoHostname"), mainConfig.getString("Storage.mongoPort"),
						mainConfig.getString("Storage.mongoDatabaseName"), mainConfig.getString("Storage.mongoUsername"),
						mainConfig.getString("Storage.mongoPassword"), (List<String>) mainConfig.getList("Storage.mongoOptionalArgs"));
				mongo.connect();
				main.setMongoSession(mongo);
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "MongoDB", null));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "MongoDB", null));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getMongoSession() != null) {
					main.getMongoSession().disconnect();
					main.setMongoSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback", null));
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL")) {
			try {
				PostgresUtils postgres = new PostgresUtils(mainConfig.getString("Storage.postgresHostname"), mainConfig.getString("Storage.postgresPort"),
						mainConfig.getString("Storage.postgresDatabaseName"), mainConfig.getString("Storage.postgresUsername"), mainConfig.getString("Storage.postgresPassword"),
						(List<String>) mainConfig.getList("Storage.postgresOptionalArgs"), mainConfig.getBoolean("Storage.postgresSSL"));
				postgres.connect();
				main.setPostgresSession(postgres);
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "Postgres", null));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "Postgres", null));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getPostgresSession() != null) {
					main.getPostgresSession().disconnect();
					main.setPostgresSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback", null));
			}
		} else {
			main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigYAMLDefault", null));
		}
	}

	/* Translator Settings */
	public String loadTranslatorSettings() {
		String outName = "Invalid";
		String attemptedTranslator = "";
		final int maxTries = 3;
		for (int tryNumber = 1; tryNumber <= maxTries; tryNumber++) {
			if (refs.serverIsStopping()) return outName;
			try {
				main.getLogger().warning(refs.getMsg("wwcTranslatorAttempt", new String[] {tryNumber + "", maxTries + ""}, null));
				for (Map.Entry<String, String> eaPair : CommonRefs.translatorPairs.entrySet()) {
					if (mainConfig.getBoolean(eaPair.getKey())) {
						attemptedTranslator = eaPair.getValue();
						refs.getTranslatorResult(eaPair.getValue(), true);
						outName = eaPair.getValue();
						break;
					}
				}
				if (!outName.equals("Invalid")) break;
			} catch (Exception e) {
				main.getLogger().severe("(" + attemptedTranslator + ") " + e.getMessage());
				e.printStackTrace();
				outName = "Invalid";
			}
		}
		if (outName.equals("Invalid")) {
			main.getLogger().severe(refs.getMsg("wwcInvalidTranslator", null));
		} else {
			main.getLogger().info(ChatColor.GREEN
					+ refs.getMsg("wwcConfigConnectionSuccess", outName, null));
		}
		return outName;
	}
	
	/* Main config save method */
	public void saveMainConfig(boolean async) {
		if (async) {
			refs.debugMsg("Saving main config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMainConfig(false);
				}
			};
			refs.runAsync(out);
			return;
		}
		refs.debugMsg("Saving main config sync!");
		saveCustomConfig(mainConfig, configFile, false);
	}
	
	/* Messages config save method */
	public void saveMessagesConfig(boolean async) {
		saveMessagesConfig(mainConfig.getString("General.pluginLang"), async);
	}

	/* Specific message config save method */
	public void saveMessagesConfig(String inLang, boolean async) {
		if (async) {
			refs.debugMsg("Saving messages config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMessagesConfig(inLang, false);
				}
			};
			refs.runAsync(out);
			return;
		}
		refs.debugMsg("Saving messages config sync!");
		saveCustomConfig(pluginLangConfigs.get(inLang), new File(main.getDataFolder(), "messages-" + inLang + ".yml"), false);
	}

	/* Custom config save method */
	public synchronized void saveCustomConfig(YamlConfiguration inConfig, File dest, boolean async) {
		if (async && main.isEnabled()) {
			refs.debugMsg("Saving custom config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveCustomConfig(inConfig, dest, false);
				}
			};
			refs.runAsync(out);
			return;
		}
		if (inConfig != null && dest != null) {
			refs.debugMsg("Saving custom config sync!");
			try {
				inConfig.save(dest);
			} catch (IOException e) {
				e.printStackTrace();
				Bukkit.getPluginManager().disablePlugin(main);
				return;
			}
		}
	}
	
	/* Sync user data to storage default */
	public void syncData() throws SQLException, MongoException {
		syncData(main.getTranslatorName().equalsIgnoreCase("Invalid"));
	}
	
	/* Sync user data to storage */
	public void syncData(boolean wasPreviouslyInvalid) throws SQLException, MongoException {
		/* If our translator is Invalid, do not run this code */
		if (wasPreviouslyInvalid) {
			return;
		}

		SQLUtils sql = main.getSqlSession();
		MongoDBUtils mongo = main.getMongoSession();
		PostgresUtils postgres = main.getPostgresSession();

		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
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
				if (mainConfig.getInt("Translator.translatorCacheSize") > 0 && mainConfig.getBoolean("Translator.enablePersistentCache")) {
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
				if (mainConfig.getInt("Translator.translatorCacheSize") >0 && mainConfig.getBoolean("Translator.enablePersistentCache")) {
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
			if (mainConfig.getInt("Translator.translatorCacheSize") >0 && mainConfig.getBoolean("Translator.enablePersistentCache")) {
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
			for (String eaName : userSettingsDir.list()) {
				File currFile = new File(userSettingsDir, eaName);
				String fileUUID = currFile.getName().substring(0, currFile.getName().indexOf("."));
				if (!main.isActiveTranslator(fileUUID)) {
					refs.debugMsg("(YAML) Deleted user data config of "
							+ fileUUID + ".");
					currFile.delete();
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

			// TODO: Make sure cache still saves when turning it on/off live
			// TODO: Everything besides YAML
			if (mainConfig.getInt("Translator.translatorCacheSize") >0 && mainConfig.getBoolean("Translator.enablePersistentCache")) {
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
				for (String eaName : cacheDir.list()) {
					File currFile = new File(cacheDir, eaName);
					YamlConfiguration conf = YamlConfiguration.loadConfiguration(currFile);
					CachedTranslation test = new CachedTranslation(conf.getString("inputLang"), conf.getString("outputLang"), conf.getString("inputPhrase"));
					if (!main.hasCacheTerm(test)) {
						refs.debugMsg("(YAML) Deleted cache term.");
						currFile.delete();
					}
				}
			}
		}
	}
	
	/* Translator YAML File Saver */
	public void createUserDataConfig(ActiveTranslator inTranslator) {
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
		
		saveCustomConfig(userSettingsConfig, userSettingsFile, false);
	}

	/* Stats YAML File Saver */
	public void createStatsConfig(PlayerRecord inRecord) {
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

		saveCustomConfig(userStatsConfig, userStatsFile, false);
	}

	/* Cache YAML File Saver */
    public void createCacheConfig(CachedTranslation trans, String out) {
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

		saveCustomConfig(cacheConfig, cacheFile, false);
	}

	/* Getters */
	public YamlConfiguration getMainConfig() {
		return mainConfig;
	}

	public YamlConfiguration getMsgsConfig() {
		return pluginLangConfigs.get(mainConfig.getString("General.pluginLang"));
	}

	public File getConfigFile() {
		return configFile;
	}

}