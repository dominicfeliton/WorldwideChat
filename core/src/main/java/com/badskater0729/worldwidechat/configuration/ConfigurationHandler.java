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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.translators.AmazonTranslation;
import com.badskater0729.worldwidechat.translators.BasicTranslation;
import com.badskater0729.worldwidechat.translators.DeepLTranslation;
import com.badskater0729.worldwidechat.translators.GoogleTranslation;
import com.badskater0729.worldwidechat.translators.LibreTranslation;
import com.badskater0729.worldwidechat.translators.TestTranslation;
import com.badskater0729.worldwidechat.translators.WatsonTranslation;
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

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsync;
import static com.badskater0729.worldwidechat.util.CommonRefs.serverIsStopping;
import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class ConfigurationHandler {

	private WorldwideChat main = WorldwideChat.instance;

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

	/* Load Main Settings Method */
	public void loadMainSettings() {
		/* Get rest of General Settings */
		// Debug Mode
		if (mainConfig.getBoolean("General.enableDebugMode")) {
			main.getLogger().warning(getMsg("wwcConfigEnabledDebugMode"));
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
			main.getLogger().warning(getMsg("wwcConfigInvalidPrefixSettings"));
		}
		// Fatal Async Timeout Delay
		try {
			if (mainConfig.getInt("General.fatalAsyncTaskTimeout") > 7) {
				WorldwideChat.translatorFatalAbortSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout");
				WorldwideChat.translatorConnectionTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
				WorldwideChat.asyncTasksTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
			} else {
				main.getLogger().warning(getMsg("wwcConfigInvalidFatalAsyncTimeout"));
			}
		} catch (Exception e) {
			main.getLogger().warning(getMsg("wwcConfigInvalidFatalAsyncTimeout"));
		}
		// bStats
		if (mainConfig.getBoolean("General.enablebStats")) {
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + getMsg("wwcConfigEnabledbStats"));
		} else {
			main.getLogger().warning(getMsg("wwcConfigDisabledbStats"));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				mainConfig.set("General.updateCheckerDelay", 86400);
				main.getLogger().warning(getMsg("wwcConfigBadUpdateDelay"));
			}
		} catch (Exception e) {
			mainConfig.set("General.updateCheckerDelay", 86400);
			main.getLogger().warning(getMsg("wwcConfigBadUpdateDelay"));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + getMsg("wwcConfigSyncDelayEnabled", mainConfig.getInt("General.syncUserDataDelay") + ""));
			} else {
				mainConfig.set("General.syncUserDataDelay", 7200);
				main.getLogger().warning(getMsg("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("General.syncUserDataDelay", 7200);
			main.getLogger().warning(getMsg("wwcConfigSyncDelayInvalid"));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + getMsg("wwcConfigRateLimitEnabled", "" + mainConfig.getInt("Translator.rateLimit")));
			} else {
				mainConfig.set("Translator.rateLimit", 0);
				main.getLogger().warning(getMsg("wwcConfigRateLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.rateLimit", 0);
			main.getLogger().warning(getMsg("wwcConfigRateLimitInvalid"));
		}
		// Per-message char limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + getMsg("wwcConfigMessageCharLimitEnabled", "" + mainConfig.getInt("Translator.messageCharLimit")));
			} else {
				mainConfig.set("Translator.messageCharLimit", 255);
				main.getLogger().warning(getMsg("wwcConfigMessageCharLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.messageCharLimit", 255);
			main.getLogger().warning(getMsg("wwcConfigMessageCharLimitInvalid"));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + getMsg("wwcConfigCacheEnabled", mainConfig.getInt("Translator.translatorCacheSize") + ""));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				mainConfig.set("Translator.translatorCacheSize", 0);
				main.setCacheProperties(0);
				main.getLogger().warning(getMsg("wwcConfigCacheDisabled"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.translatorCacheSize", 100);
			main.setCacheProperties(100);
			main.getLogger().warning(getMsg("wwcConfigCacheInvalid"));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + getMsg("wwcConfigErrorLimitEnabled", mainConfig.getInt("Translator.errorLimit") + ""));
			} else {
				mainConfig.set("Translator.errorLimit", 5);
				main.getLogger().warning(getMsg("wwcConfigErrorLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.errorLimit", 5);
			main.getLogger().warning(getMsg("wwcConfigErrorLimitInvalid"));
		}
		// List of Errors to Ignore Settings
		try {
			ArrayList<String> errorsToIgnore = (ArrayList<String>) mainConfig.getList("Storage.errorsToIgnore");
			main.getLogger().info(
					ChatColor.LIGHT_PURPLE + getMsg("wwcConfigErrorsToIgnoreSuccess"));
		} catch (Exception e) {
			mainConfig.set("Storage.errorsToIgnore", Arrays.asList("confidence", "same as target", "detect the source language"));
			main.getLogger().warning(getMsg("wwcConfigErrorsToIgnoreInvalid"));
		}
	}
	
	/* Storage Settings */
	public void loadStorageSettings() {
		if (mainConfig.getBoolean("Storage.useSQL")) {
			try {
				SQLUtils.connect(mainConfig.getString("Storage.sqlHostname"), mainConfig.getString("Storage.sqlPort"), 
						mainConfig.getString("Storage.sqlDatabaseName"), mainConfig.getString("Storage.sqlUsername"), mainConfig.getString("Storage.sqlPassword"), 
						(List<String>) mainConfig.getList("Storage.sqlOptionalArgs"), mainConfig.getBoolean("Storage.sqlUseSSL"));
				main.getLogger().info(ChatColor.GREEN + getMsg("wwcConfigConnectionSuccess", "SQL"));
			} catch (Exception e) {
				main.getLogger().severe(getMsg("wwcConfigConnectionFail", "SQL"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				SQLUtils.disconnect(); // Just in case
				main.getLogger().severe(getMsg("wwcConfigYAMLFallback"));
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB")) {
			try {
				MongoDBUtils.connect(mainConfig.getString("Storage.mongoHostname"), mainConfig.getString("Storage.mongoPort"), 
						mainConfig.getString("Storage.mongoDatabaseName"), mainConfig.getString("Storage.mongoUsername"), 
						mainConfig.getString("Storage.mongoPassword"), (List<String>) mainConfig.getList("Storage.mongoOptionalArgs"));
				main.getLogger().info(ChatColor.GREEN + getMsg("wwcConfigConnectionSuccess", "MongoDB"));
			} catch (Exception e) {
				main.getLogger().severe(getMsg("wwcConfigConnectionFail", "MongoDB"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				MongoDBUtils.disconnect();
				main.getLogger().severe(getMsg("wwcConfigYAMLFallback"));
			}
		} else {
			main.getLogger().info(ChatColor.GREEN + getMsg("wwcConfigYAMLDefault"));
		}
	}

	/* Translator Settings */
	public void loadTranslatorSettings() {
		String outName = "Invalid";
		final int maxTries = 3;
		for (int tryNumber = 1; tryNumber <= maxTries; tryNumber++) {
			if (serverIsStopping()) return;
			try {
				main.getLogger().warning(getMsg("wwcTranslatorAttempt", new String[] {tryNumber + "", maxTries + ""}));
				BasicTranslation test;
				if (mainConfig.getBoolean("Translator.useWatsonTranslate")) {
					outName = "Watson";
					test = new WatsonTranslation(mainConfig.getString("Translator.watsonAPIKey"),
							mainConfig.getString("Translator.watsonURL"), true);
					test.useTranslator();
					break;
				} else if (mainConfig.getBoolean("Translator.useGoogleTranslate")) {
					outName = "Google Translate";
					test = new GoogleTranslation(
							mainConfig.getString("Translator.googleTranslateAPIKey"), true);
					test.useTranslator();
					break;
				} else if (mainConfig.getBoolean("Translator.useAmazonTranslate")) {
					outName = "Amazon Translate";
					test = new AmazonTranslation(mainConfig.getString("Translator.amazonAccessKey"),
							mainConfig.getString("Translator.amazonSecretKey"),
							mainConfig.getString("Translator.amazonRegion"), true);
					test.useTranslator();
					break;
				} else if (mainConfig.getBoolean("Translator.useLibreTranslate")) {
					outName = "Libre Translate";
					test = new LibreTranslation(mainConfig.getString("Translator.libreAPIKey"),
							mainConfig.getString("Translator.libreURL"), true);
					test.useTranslator();
					break;
				} else if (mainConfig.getBoolean("Translator.useDeepLTranslate")) {
					outName = "DeepL Translate";
					test = new DeepLTranslation(mainConfig.getString("Translator.deepLAPIKey"), true);
					test.useTranslator();
					break;
				} else if (mainConfig.getBoolean("Translator.testModeTranslator")) {
					outName = "JUnit/MockBukkit Testing Translator";
					test = new TestTranslation(
							"TXkgYm95ZnJpZW5kICgyMk0pIHJlZnVzZXMgdG8gZHJpbmsgd2F0ZXIgdW5sZXNzIEkgKDI0RikgZHllIGl0IGJsdWUgYW5kIGNhbGwgaXQgZ2FtZXIganVpY2Uu", true);
					test.useTranslator();
					break;
				} else {
					outName = "Invalid";
					break;
				}
			} catch (Exception e) {
				main.getLogger().severe("(" + outName + ") " + e.getMessage());
				e.printStackTrace();
				outName = "Invalid";
			}
		}
		if (outName.equals("Invalid")) {
			main.getLogger().severe(getMsg("wwcInvalidTranslator"));
		} else {
			main.getLogger().info(ChatColor.GREEN
					+ getMsg("wwcConfigConnectionSuccess", outName));
		}
		main.setTranslatorName(outName);
	}
	
	/* Main config save method */
	public void saveMainConfig(boolean async) {
		if (async) {
			debugMsg("Saving main config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMainConfig(false);
				}
			};
			runAsync(out);
			return;
		}
		debugMsg("Saving main config sync!");
		saveCustomConfig(mainConfig, configFile, false);
	}
	
	/* Messages config save method */
	public void saveMessagesConfig(boolean async) {
		if (async) {
			debugMsg("Saving messages config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMessagesConfig(false);
				}
			};
			runAsync(out);
			return;
		}
		debugMsg("Saving messages config sync!");
		saveCustomConfig(messagesConfig, messagesFile, false);
	}
	
	/* Custom config save method */
	public synchronized void saveCustomConfig(YamlConfiguration inConfig, File dest, boolean async) {
		if (async && main.isEnabled()) {
			debugMsg("Saving custom config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveCustomConfig(inConfig, dest, false);
				}
			};
			runAsync(out);
			return;
		}
		if (inConfig != null && dest != null) {
			debugMsg("Saving custom config sync!");
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
		//TODO: Investigate why mockbukkit no longer works here
		if (wasPreviouslyInvalid || main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
			return;
		}
		if (SQLUtils.isConnected()) {
			// Our Generic Table Layout: 
			// | Creation Date | Object Properties |  
			try {
				Connection sqlConnection = SQLUtils.getConnection();
				
				/* Sync ActiveTranslator data to corresponding table */
				main.getActiveTranslators().entrySet().forEach((entry) -> {
					debugMsg("(SQL) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				    if (!entry.getValue().getHasBeenSaved()) {
				    	try {
				    		ActiveTranslator val = entry.getValue();
				    		PreparedStatement newActiveTranslator = sqlConnection.prepareStatement("REPLACE activeTranslators"
					    			+ " (creationDate,playerUUID,inLangCode,outLangCode,rateLimit,rateLimitPreviousTime,translatingChatOutgoing,translatingChatIncoming,translatingBook,translatingSign,translatingItem,translatingEntity)"
					    			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
					    	newActiveTranslator.setString(1, Instant.now().toString());
					    	newActiveTranslator.setString(2, val.getUUID());
					    	newActiveTranslator.setString(3, val.getInLangCode());
					    	newActiveTranslator.setString(4, val.getOutLangCode());
					    	newActiveTranslator.setInt(5, val.getRateLimit());
					    	newActiveTranslator.setString(6, val.getRateLimitPreviousTime());
					    	newActiveTranslator.setBoolean(7, val.getTranslatingChatOutgoing());
					    	newActiveTranslator.setBoolean(8, val.getTranslatingChatIncoming());
					    	newActiveTranslator.setBoolean(9, val.getTranslatingBook());
					    	newActiveTranslator.setBoolean(10, val.getTranslatingSign());
					    	newActiveTranslator.setBoolean(11, val.getTranslatingItem());
					    	newActiveTranslator.setBoolean(12, val.getTranslatingEntity());
					    	newActiveTranslator.executeUpdate();
					    	newActiveTranslator.close();
				    	} catch (SQLException e) {
							e.printStackTrace();
							return;
						}
				    	debugMsg("(SQL) Created/updated unsaved user data config of " + entry.getKey() + ".");
				    	entry.getValue().setHasBeenSaved(true);
				    }
				});

				/* Delete any old ActiveTranslators */
				ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators");
				while (rs.next()) {
					if (!main.isActiveTranslator(rs.getString("playerUUID"))) {
						String uuid = rs.getString("playerUUID");
						PreparedStatement deleteOldItem = sqlConnection.prepareStatement("DELETE FROM activeTranslators WHERE playerUUID = ?");
						deleteOldItem.setString(1, uuid);
						deleteOldItem.executeUpdate();
						deleteOldItem.close();
						debugMsg("(SQL) Deleted user data config of " + uuid + ".");
					}
				}
				
				/* Sync PlayerRecord data to corresponding table */
                main.getPlayerRecords().entrySet().forEach((entry) -> {
                	debugMsg("(SQL) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
                    if (!entry.getValue().getHasBeenSaved()) {
                    	try {
                    		PlayerRecord val = entry.getValue();
                    		PreparedStatement newPlayerRecord = sqlConnection.prepareStatement("REPLACE playerRecords"
                    				+ " (creationDate,playerUUID,attemptedTranslations,successfulTranslations,lastTranslationTime) VALUES (?,?,?,?,?)");
                    		newPlayerRecord.setString(1, Instant.now().toString());
                    		newPlayerRecord.setString(2, val.getUUID());
                    		newPlayerRecord.setInt(3, val.getAttemptedTranslations());
                    		newPlayerRecord.setInt(4, val.getSuccessfulTranslations());
                    		newPlayerRecord.setString(5, val.getLastTranslationTime());
                    		newPlayerRecord.executeUpdate();
                    		newPlayerRecord.close();
                    	} catch (SQLException e) {
                    		e.printStackTrace();
                    		return;
                    	}
                    	debugMsg("(SQL) Created/updated unsaved user record of " + entry.getKey() + ".");
                    	entry.getValue().setHasBeenSaved(true);
				    }
				});
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (MongoDBUtils.isConnected()) {
			try {
				/* Initialize collections */
				MongoDatabase database = MongoDBUtils.getActiveDatabase();
				MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
				MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");
				
				/* Write ActiveTranslators to DB */
				main.getActiveTranslators().entrySet().forEach((entry) -> {
					debugMsg("(MongoDB) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
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
						debugMsg("(MongoDB) Deleted user data config of " + uuid + ".");
					}
				}
				
				/* Write PlayerRecords to DB */
				main.getPlayerRecords().entrySet().forEach((entry) -> {
                	debugMsg("(MongoDB) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
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
                    	debugMsg("(MongoDB) Created/updated unsaved user record of " + entry.getKey() + ".");
				    }
				});
				
			} catch (MongoException e) {
				e.printStackTrace();
			}
		} else {
			/* Last resort, sync activeTranslators to disk via YAML */
			// Save all new activeTranslators
			main.getActiveTranslators().entrySet().forEach((entry) -> {
				debugMsg("(YAML) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				if (!entry.getValue().getHasBeenSaved()) {
					debugMsg("(YAML) Created/updated unsaved user data config of " + entry.getKey() + ".");
					entry.getValue().setHasBeenSaved(true);
					createUserDataConfig(entry.getValue());
				}
			});
			
			// Delete any old activeTranslators
			File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
			for (String eaName : userSettingsDir.list()) {
				File currFile = new File(userSettingsDir, eaName);
				if (!main.isActiveTranslator(currFile.getName().substring(0, currFile.getName().indexOf(".")))) {
					debugMsg("(YAML) Deleted user data config of "
							+ currFile.getName().substring(0, currFile.getName().indexOf(".")) + ".");
					currFile.delete();
				}
			}

			/* Sync playerRecords to disk */
			main.getPlayerRecords().entrySet().forEach((entry) -> {
				debugMsg("(YAML) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				if (!entry.getValue().getHasBeenSaved()) {
					debugMsg("(YAML) Created/updated unsaved user record of " + entry.getKey() + ".");
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
		File userStatsFile;
		YamlConfiguration userStatsConfig;
		userStatsFile = new File(main.getDataFolder() + File.separator + "stats" + File.separator,
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