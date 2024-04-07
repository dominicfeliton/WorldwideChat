package com.badskater0729.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import com.badskater0729.worldwidechat.translators.*;
import com.badskater0729.worldwidechat.util.storage.PostgresUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.Metrics;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import com.badskater0729.worldwidechat.util.storage.MongoDBUtils;
import com.badskater0729.worldwidechat.util.storage.SQLUtils;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;
import org.apache.commons.lang3.tuple.Pair;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class ConfigurationHandler {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private File messagesFile;
	private File configFile;
	private YamlConfiguration messagesConfig;
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
		for (String supportedPluginLangCode : supportedPluginLangCodes) {
			if (supportedPluginLangCode
					.equalsIgnoreCase(mainConfig.getString("General.pluginLang"))) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + mainConfig.getString("General.pluginLang") + ".");
				return;
			}
		}

		mainConfig.set("General.pluginLang", "en");
		main.getLogger().warning("Unable to detect a valid language in your config.yml. Defaulting to en...");
	}

	/* Init Messages Method */
	public void initMessagesConfig() {
		/* Init config file */
		messagesFile = new File(main.getDataFolder(), "messages-" + mainConfig.getString("General.pluginLang") + ".yml");

		/* Save default messages file if it does not exist */
		if (!messagesFile.exists()) {
			main.saveResource("messages-" + mainConfig.getString("General.pluginLang") + ".yml", true);

			YamlConfiguration tempConfig = YamlConfiguration.loadConfiguration(messagesFile);

			tempConfig.set("DoNotTouchThis.Version", WorldwideChat.messagesConfigVersion);

			saveCustomConfig(tempConfig, messagesFile, false);
		}

		/* Load config */
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

		/* Check if version value is out of date...*/
		if (messagesConfig.getString("DoNotTouchThis.Version") == null || !messagesConfig.getString("DoNotTouchThis.Version").equals(WorldwideChat.messagesConfigVersion)) {
			main.getLogger().warning("Upgrading out-of-date messages config!");
			HashMap<String, String> oldOverrides = new HashMap<>();

			/* Copy overrides section */
			if (messagesConfig.getConfigurationSection("Overrides") != null) {
				for (String eaKey : messagesConfig.getConfigurationSection("Overrides").getKeys(true)) {
					oldOverrides.put(eaKey, messagesConfig.getString("Overrides." + eaKey));
				}
			}

			/* Delete old config */
			messagesFile.delete();

			/* Copy newest config */
			main.saveResource("messages-" + mainConfig.getString("General.pluginLang") + ".yml", true);
			messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
			messagesConfig.set("DoNotTouchThis.Version", WorldwideChat.messagesConfigVersion);

			/* Paste overrides section */
			if (!oldOverrides.isEmpty()) {
				for (Map.Entry<String, String> entry : oldOverrides.entrySet()) {
					messagesConfig.set("Overrides." + entry.getKey(), entry.getValue());
				}
			}

			/* Save messages config */
			saveMessagesConfig(false);

			/* Success :) */
			main.getLogger().warning("Upgrade successful.");
		}
	}

	public void generateMessagesConfig(String inLocalLang) {
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
			main.getLogger().warning("Upgrading out-of-date messages config!");
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
			main.getLogger().warning("Upgrade successful.");
		}
	}

	/* Load Main Settings Method */
	public void loadMainSettings() {
		/* Get rest of General Settings */
		// Debug Mode
		if (mainConfig.getBoolean("General.enableDebugMode")) {
			main.getLogger().warning(refs.getMsg("wwcConfigEnabledDebugMode"));
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
			main.getLogger().warning(refs.getMsg("wwcConfigInvalidPrefixSettings"));
		}
		// Fatal Async Timeout Delay
		try {
			if (mainConfig.getInt("General.fatalAsyncTaskTimeout") > 7) {
				WorldwideChat.translatorFatalAbortSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout");
				WorldwideChat.translatorConnectionTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
				WorldwideChat.asyncTasksTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
			} else {
				main.getLogger().warning(refs.getMsg("wwcConfigInvalidFatalAsyncTimeout"));
			}
		} catch (Exception e) {
			main.getLogger().warning(refs.getMsg("wwcConfigInvalidFatalAsyncTimeout"));
		}
		// bStats
		if (mainConfig.getBoolean("General.enablebStats")) {
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigEnabledbStats"));
		} else {
			main.getLogger().warning(refs.getMsg("wwcConfigDisabledbStats"));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				mainConfig.set("General.updateCheckerDelay", 86400);
				main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay"));
			}
		} catch (Exception e) {
			mainConfig.set("General.updateCheckerDelay", 86400);
			main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay"));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigSyncDelayEnabled", mainConfig.getInt("General.syncUserDataDelay") + ""));
			} else {
				mainConfig.set("General.syncUserDataDelay", 7200);
				main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("General.syncUserDataDelay", 7200);
			main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid"));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigRateLimitEnabled", "" + mainConfig.getInt("Translator.rateLimit")));
			} else {
				mainConfig.set("Translator.rateLimit", 0);
				main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.rateLimit", 0);
			main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid"));
		}
		// Per-message char limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigMessageCharLimitEnabled", "" + mainConfig.getInt("Translator.messageCharLimit")));
			} else {
				mainConfig.set("Translator.messageCharLimit", 255);
				main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.messageCharLimit", 255);
			main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid"));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigCacheEnabled", mainConfig.getInt("Translator.translatorCacheSize") + ""));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				mainConfig.set("Translator.translatorCacheSize", 0);
				main.setCacheProperties(0);
				main.getLogger().warning(refs.getMsg("wwcConfigCacheDisabled"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.translatorCacheSize", 100);
			main.setCacheProperties(100);
			main.getLogger().warning(refs.getMsg("wwcConfigCacheInvalid"));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorLimitEnabled", mainConfig.getInt("Translator.errorLimit") + ""));
			} else {
				mainConfig.set("Translator.errorLimit", 5);
				main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.errorLimit", 5);
			main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid"));
		}
		// List of Errors to Ignore Settings
		try {
			ArrayList<String> errorsToIgnore = (ArrayList<String>) mainConfig.getList("Storage.errorsToIgnore");
			main.getLogger().info(
					ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorsToIgnoreSuccess"));
		} catch (Exception e) {
			mainConfig.set("Storage.errorsToIgnore", Arrays.asList("confidence", "same as target", "detect the source language"));
			main.getLogger().warning(refs.getMsg("wwcConfigErrorsToIgnoreInvalid"));
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
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "SQL"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "SQL"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getSqlSession() != null) {
					main.getSqlSession().disconnect();
					main.setSqlSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback"));
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB")) {
			try {
				MongoDBUtils mongo = new MongoDBUtils(mainConfig.getString("Storage.mongoHostname"), mainConfig.getString("Storage.mongoPort"),
						mainConfig.getString("Storage.mongoDatabaseName"), mainConfig.getString("Storage.mongoUsername"),
						mainConfig.getString("Storage.mongoPassword"), (List<String>) mainConfig.getList("Storage.mongoOptionalArgs"));
				mongo.connect();
				main.setMongoSession(mongo);
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "MongoDB"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "MongoDB"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getMongoSession() != null) {
					main.getMongoSession().disconnect();
					main.setMongoSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback"));
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL")) {
			try {
				PostgresUtils postgres = new PostgresUtils(mainConfig.getString("Storage.postgresHostname"), mainConfig.getString("Storage.postgresPort"),
						mainConfig.getString("Storage.postgresDatabaseName"), mainConfig.getString("Storage.postgresUsername"), mainConfig.getString("Storage.postgresPassword"),
						(List<String>) mainConfig.getList("Storage.postgresOptionalArgs"), mainConfig.getBoolean("Storage.postgresSSL"));
				postgres.connect();
				main.setPostgresSession(postgres);
				main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigConnectionSuccess", "Postgres"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getMsg("wwcConfigConnectionFail", "Postgres"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getPostgresSession() != null) {
					main.getPostgresSession().disconnect();
					main.setPostgresSession(null);
				}
				main.getLogger().severe(refs.getMsg("wwcConfigYAMLFallback"));
			}
		} else {
			main.getLogger().info(ChatColor.GREEN + refs.getMsg("wwcConfigYAMLDefault"));
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
				main.getLogger().warning(refs.getMsg("wwcTranslatorAttempt", new String[] {tryNumber + "", maxTries + ""}));
				for (Pair<String, String> eaPair : CommonRefs.translatorPairs) {
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
			main.getLogger().severe(refs.getMsg("wwcInvalidTranslator"));
		} else {
			main.getLogger().info(ChatColor.GREEN
					+ refs.getMsg("wwcConfigConnectionSuccess", outName));
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
		if (async) {
			refs.debugMsg("Saving messages config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMessagesConfig(false);
				}
			};
			refs.runAsync(out);
			return;
		}
		refs.debugMsg("Saving messages config sync!");
		saveCustomConfig(messagesConfig, messagesFile, false);
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
	public void syncData() {
		syncData(main.getTranslatorName().equalsIgnoreCase("Invalid"));
	}
	
	/* Sync user data to storage */
	public void syncData(boolean wasPreviouslyInvalid) {
		/* If our translator is Invalid, do not run this code */
		// TODO: For SQL/Postgres, use CommonRefs definitions instead...dataclass-esque thing?
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
				main.getActiveTranslators().entrySet().forEach((entry) -> {
					refs.debugMsg("(SQL) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				    if (!entry.getValue().getHasBeenSaved()) {
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

						// Log the final SQL statement for debugging purposes
				    	try (PreparedStatement newActiveTranslator = sqlConnection.prepareStatement(sqlStatement)) {
				    		ActiveTranslator val = entry.getValue();
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
					    	newActiveTranslator.executeUpdate();
				    	} catch (SQLException e) {
							e.printStackTrace();
							return;
						}
				    	refs.debugMsg("(SQL) Created/updated unsaved user data config of " + entry.getKey() + ".");
				    	entry.getValue().setHasBeenSaved(true);
				    }
				});

				/* Delete any old ActiveTranslators */
				try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
					while (rs.next()) {
						if (!main.isActiveTranslator(rs.getString("playerUUID"))) {
							try (PreparedStatement deleteOldItem = sqlConnection.prepareStatement("DELETE FROM activeTranslators WHERE playerUUID = ?")) {
								String uuid = rs.getString("playerUUID");
								deleteOldItem.setString(1, uuid);
								deleteOldItem.executeUpdate();
								refs.debugMsg("(SQL) Deleted user data config of " + uuid + ".");
							}
						}
					}
				}
				
				/* Sync PlayerRecord data to corresponding table */
                main.getPlayerRecords().entrySet().forEach((entry) -> {
                	refs.debugMsg("(SQL) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
                    if (!entry.getValue().getHasBeenSaved()) {
						// Dynamically construct the SQL statement based on the schema
						String tableName = "playerRecords";
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

                    	try (PreparedStatement newPlayerRecord = sqlConnection.prepareStatement(sqlStatement)) {
                    		PlayerRecord val = entry.getValue();
							int i = 1;
                    		newPlayerRecord.setString(i++, Instant.now().toString());
                    		newPlayerRecord.setString(i++, val.getUUID());
                    		newPlayerRecord.setInt(i++, val.getAttemptedTranslations());
                    		newPlayerRecord.setInt(i++, val.getSuccessfulTranslations());
                    		newPlayerRecord.setString(i++, val.getLastTranslationTime());
                    		newPlayerRecord.executeUpdate();
                    	} catch (SQLException e) {
                    		e.printStackTrace();
                    		return;
                    	}
                    	refs.debugMsg("(SQL) Created/updated unsaved user record of " + entry.getKey() + ".");
                    	entry.getValue().setHasBeenSaved(true);
				    }
				});
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (main.isPostgresConnValid(true)) {
			// Our Generic Table Layout:
			// | Creation Date | Object Properties |
			try (Connection postgresConnection = postgres.getConnection()) {
				/* Sync ActiveTranslator data to corresponding table */
				main.getActiveTranslators().entrySet().forEach((entry) -> {
					refs.debugMsg("(Postgres) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
					if (!entry.getValue().getHasBeenSaved()) {
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

						// Log the final SQL statement for debugging purposes
						try (PreparedStatement newActiveTranslator = postgresConnection.prepareStatement(sqlStatement)) {
							ActiveTranslator val = entry.getValue();
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
							newActiveTranslator.executeUpdate();
						} catch (SQLException e) {
							e.printStackTrace();
							return;
						}
						refs.debugMsg("(Postgres) Created/updated unsaved user data config of " + entry.getKey() + ".");
						entry.getValue().setHasBeenSaved(true);
					}
				});

				/* Delete any old ActiveTranslators */
				try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT playerUUID FROM activeTranslators")) {
					while (rs.next()) {
						if (!main.isActiveTranslator(rs.getString("playerUUID"))) {
							try (PreparedStatement deleteOldItem = postgresConnection.prepareStatement("DELETE FROM activeTranslators WHERE playerUUID = ?")) {
								String uuid = rs.getString("playerUUID");
								deleteOldItem.setString(1, uuid);
								deleteOldItem.executeUpdate();
								refs.debugMsg("(Postgres) Deleted user data config of " + uuid + ".");
							}
						}
					}
				}

				/* Sync PlayerRecord data to corresponding table */
				main.getPlayerRecords().entrySet().forEach((entry) -> {
					refs.debugMsg("(Postgres) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
					if (!entry.getValue().getHasBeenSaved()) {
						String tableName = "playerRecords";
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

						// Log the final SQL statement for debugging purposes
						try (PreparedStatement newPlayerRecord = postgresConnection.prepareStatement(sqlStatement)) {
							PlayerRecord val = entry.getValue();
							int i = 1;
							newPlayerRecord.setString(i++, Instant.now().toString());
							newPlayerRecord.setString(i++, val.getUUID());
							newPlayerRecord.setInt(i++, val.getAttemptedTranslations());
							newPlayerRecord.setInt(i++, val.getSuccessfulTranslations());
							newPlayerRecord.setString(i++, val.getLastTranslationTime());
							newPlayerRecord.executeUpdate();
						} catch (SQLException e) {
							e.printStackTrace();
							return;
						}
						refs.debugMsg("(Postgres) Created/updated unsaved user record of " + entry.getKey() + ".");
						entry.getValue().setHasBeenSaved(true);
					}
				});
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (main.isMongoConnValid(true)) {
			try {
				/* Initialize collections */
				MongoDatabase database = mongo.getActiveDatabase();
				MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
				MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");

				/* Write ActiveTranslators to DB */
				main.getActiveTranslators().entrySet().forEach((entry) -> {
					refs.debugMsg("(MongoDB) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
					if (!entry.getValue().getHasBeenSaved()) {
						ActiveTranslator val = entry.getValue();
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

						ReplaceOptions opts = new ReplaceOptions().upsert(true);
						Bson filter = Filters.eq("playerUUID", val.getUUID());
						activeTranslatorCol.replaceOne(filter, currTranslator, opts);

						entry.getValue().setHasBeenSaved(true);
					}
				});

				/* Delete old ActiveTranslators from MongoDB */
				FindIterable<Document> iterDoc = activeTranslatorCol.find();
				Iterator<Document> it = iterDoc.iterator();
				while (it.hasNext()) {
					Document currDoc = it.next();
					if (!main.isActiveTranslator(currDoc.getString("playerUUID"))) {
						String uuid = currDoc.getString("playerUUID");
						Bson query = Filters.eq("playerUUID", uuid);
						activeTranslatorCol.deleteOne(query);
						refs.debugMsg("(MongoDB) Deleted user data config of " + uuid + ".");
					}
				}

				/* Write PlayerRecords to DB */
				main.getPlayerRecords().entrySet().forEach((entry) -> {
					refs.debugMsg("(MongoDB) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
					if (!entry.getValue().getHasBeenSaved()) {
						PlayerRecord val = entry.getValue();
						Document currPlayerRecord = new Document()
								.append("creationDate", Instant.now().toString())
								.append("playerUUID", val.getUUID())
								.append("attemptedTranslations", val.getAttemptedTranslations())
								.append("successfulTranslations", val.getSuccessfulTranslations())
								.append("lastTranslationTime", val.getLastTranslationTime());

						ReplaceOptions opts = new ReplaceOptions().upsert(true);
						Bson filter = Filters.eq("playerUUID", val.getUUID());
						playerRecordCol.replaceOne(filter, currPlayerRecord, opts);

						entry.getValue().setHasBeenSaved(true);
						refs.debugMsg("(MongoDB) Created/updated unsaved user record of " + entry.getKey() + ".");
					}
				});

			} catch (MongoException e) {
				e.printStackTrace();
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

		/*
		if (userStatsDir.list() == null) {
			refs.debugMsg("Creating dir at " + userStatsDir.toString());
			userStatsDir.mkdir();
		}
		*/
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

		saveCustomConfig(userStatsConfig, userStatsFile, false);
	}

	/* Getters */
	public YamlConfiguration getMainConfig() {
		return mainConfig;
	}

	public YamlConfiguration getMsgsConfig() {
		return messagesConfig;
	}

	public File getConfigFile() {
		return configFile;
	}

	public File getMsgsFile() {
		return messagesFile;
	}
}