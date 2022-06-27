package com.expl0itz.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.translators.AmazonTranslation;
import com.expl0itz.worldwidechat.translators.BasicTranslation;
import com.expl0itz.worldwidechat.translators.GoogleTranslation;
import com.expl0itz.worldwidechat.translators.LibreTranslation;
import com.expl0itz.worldwidechat.translators.TestTranslation;
import com.expl0itz.worldwidechat.translators.WatsonTranslation;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.Metrics;
import com.expl0itz.worldwidechat.util.PlayerRecord;
import com.expl0itz.worldwidechat.util.SQLUtils;

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
		try {
			Reader mainConfigStream = new InputStreamReader(main.getResource("config.yml"), "UTF-8");
			mainConfig.setDefaults(YamlConfiguration.loadConfiguration(mainConfigStream));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		mainConfig.options().copyDefaults(true);
		saveMainConfig(false);

		/* Get plugin lang */
		for (int i = 0; i < CommonDefinitions.supportedPluginLangCodes.length; i++) {
			if (CommonDefinitions.supportedPluginLangCodes[i]
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
			
			tempConfig.set("DoNotTouchThis.Version", main.getCurrentMessagesConfigVersion());
			
			saveCustomConfig(tempConfig, messagesFile, false);
		}

		/* Load config */
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
		
		/* Check if version value is out of date...*/
		String currentMessagesConfigVersion = main.getCurrentMessagesConfigVersion();
		
		if (messagesConfig.getString("DoNotTouchThis.Version") == null || !messagesConfig.getString("DoNotTouchThis.Version").equals(currentMessagesConfigVersion)) {
			main.getLogger().warning("Upgrading out-of-date messages config!");
			HashMap<String, String> oldOverrides = new HashMap<String, String>();
			
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
			messagesConfig.set("DoNotTouchThis.Version", main.getCurrentMessagesConfigVersion());
			
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
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigEnabledDebugMode"));
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
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigInvalidPrefixSettings"));
		}
		// Fatal Async Timeout Delay
		try {
			if (mainConfig.getInt("General.fatalAsyncTaskTimeout") > 7) {
				WorldwideChat.translatorFatalAbortSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout");
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigInvalidFatalAsyncTimeout"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigInvalidFatalAsyncTimeout"));
		}
		// bStats
		if (mainConfig.getBoolean("General.enablebStats")) {
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigEnabledbStats"));
		} else {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigDisabledbStats"));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				mainConfig.set("General.updateCheckerDelay", 86400);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
			}
		} catch (Exception e) {
			mainConfig.set("General.updateCheckerDelay", 86400);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigSyncDelayEnabled", new String[] {mainConfig.getInt("General.syncUserDataDelay") + ""}));
			} else {
				mainConfig.set("General.syncUserDataDelay", 7200);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("General.syncUserDataDelay", 7200);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigRateLimitEnabled", new String[] {"" + mainConfig.getInt("Translator.rateLimit")}));
			} else {
				mainConfig.set("Translator.rateLimit", 0);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigRateLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.rateLimit", 0);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigRateLimitInvalid"));
		}
		// Per-message char limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigMessageCharLimitEnabled", new String[] {"" + mainConfig.getInt("Translator.messageCharLimit")}));
			} else {
				mainConfig.set("Translator.messageCharLimit", 255);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMessageCharLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.messageCharLimit", 255);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMessageCharLimitInvalid"));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigCacheEnabled", new String[] {mainConfig.getInt("Translator.translatorCacheSize") + ""}));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				mainConfig.set("Translator.translatorCacheSize", 0);
				main.setCacheProperties(0);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheDisabled"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.translatorCacheSize", 100);
			main.setCacheProperties(100);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheInvalid"));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigErrorLimitEnabled", new String[] {mainConfig.getInt("Translator.errorLimit") + ""}));
			} else {
				mainConfig.set("Translator.errorLimit", 5);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
			}
		} catch (Exception e) {
			mainConfig.set("Translator.errorLimit", 5);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
		}
	}
	
	/* Storage Settings */
	public void loadStorageSettings() {
		if (mainConfig.getBoolean("Storage.useSQL")) {
			try {
				SQLUtils.connect(mainConfig.getString("Storage.sqlHostname"), mainConfig.getString("Storage.sqlPort"), 
						mainConfig.getString("Storage.sqlDatabaseName"), mainConfig.getString("Storage.sqlUsername"), mainConfig.getString("Storage.sqlPassword"), 
						(List<String>) mainConfig.getList("Storage.sqlOptionalArgs"), mainConfig.getBoolean("Storage.sqlUseSSL"));
				main.getLogger().info(ChatColor.GREEN + CommonDefinitions.getMessage("wwcConfigSQLSuccess"));
			} catch (Exception e) {
				main.getLogger().severe(CommonDefinitions.getMessage("wwcConfigSQLFail"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				SQLUtils.disconnect(); // Just in case
				main.getLogger().severe(CommonDefinitions.getMessage("wwcConfigYAMLFallback"));
			}
		} else {
			main.getLogger().info(ChatColor.GREEN + CommonDefinitions.getMessage("wwcConfigYAMLDefault"));
		}
	}

	/* Translator Settings */
	public void loadTranslatorSettings() {
		String outName = "Invalid";
		final int maxTries = 3;
		for (int tryNumber = 1; tryNumber <= maxTries; tryNumber++) {
			if (CommonDefinitions.serverIsStopping()) return;
			try {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcTranslatorAttempt", new String[] {tryNumber + "", maxTries + ""}));
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
			main.getLogger().severe(CommonDefinitions.getMessage("wwcInvalidTranslator"));
		} else {
			main.getLogger().info(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigConnectionSuccess", new String[] {outName}));
		}
		main.setTranslatorName(outName);
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
	
	/* Main config save method */
	public void saveMainConfig(boolean async) {
		if (async) {
			CommonDefinitions.sendDebugMessage("Saving main config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMainConfig(false);
				}
			};
			CommonDefinitions.scheduleTaskAsynchronously(out);
			return;
		}
		CommonDefinitions.sendDebugMessage("Saving main config sync!");
		saveCustomConfig(mainConfig, configFile, false);
	}
	
	/* Messages config save method */
	public void saveMessagesConfig(boolean async) {
		if (async) {
			CommonDefinitions.sendDebugMessage("Saving messages config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveMessagesConfig(false);
				}
			};
			CommonDefinitions.scheduleTaskAsynchronously(out);
			return;
		}
		CommonDefinitions.sendDebugMessage("Saving messages config sync!");
		saveCustomConfig(messagesConfig, messagesFile, false);
	}
	
	/* Custom config save method */
	public synchronized void saveCustomConfig(YamlConfiguration inConfig, File dest, boolean async) {
		if (async && main.isEnabled()) {
			CommonDefinitions.sendDebugMessage("Saving custom config async!");
			BukkitRunnable out = new BukkitRunnable() {
				@Override
				public void run() {
					saveCustomConfig(inConfig, dest, false);
				}
			};
			CommonDefinitions.scheduleTaskAsynchronously(out);
			return;
		}
		if (inConfig != null && dest != null) {
			CommonDefinitions.sendDebugMessage("Saving custom config sync!");
			try {
				inConfig.save(dest);
			} catch (IOException e) {
				e.printStackTrace();
				Bukkit.getPluginManager().disablePlugin(main);
				return;
			}
		}
	}
	
	/* Sync user data to storage */
	public void syncData() {
		/* If our translator is Invalid, do not run this code */
		//TODO: Investigate why mockbukkit no longer works here
		if (!main.getTranslatorName().equals("Invalid") && !main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
			/* Sync to SQL database, if it exists */
			// Our Generic Table Layout: 
			// | Creation Date | Object Properties |  
			if (SQLUtils.isConnected()) {
				try {
					Connection sqlConnection = SQLUtils.getConnection();
					
					/* Create tables if they do not exist already */
					PreparedStatement initActiveTranslators = sqlConnection.prepareStatement("CREATE TABLE IF NOT EXISTS activeTranslators "
							+ "(creationDate VARCHAR(256),playerUUID VARCHAR(100),inLangCode VARCHAR(12),outLangCode VARCHAR(12),rateLimit VARCHAR(256),"
							+ "rateLimitPreviousTime VARCHAR(256),translatingChatOutgoing VARCHAR(12), translatingChatIncoming VARCHAR(12),"
							+ "translatingBook VARCHAR(12),translatingSign VARCHAR(12),translatingItem VARCHAR(12),translatingEntity VARCHAR(12),PRIMARY KEY (playerUUID))");
					initActiveTranslators.executeUpdate();
					initActiveTranslators.close();
					PreparedStatement initPlayerRecords = sqlConnection.prepareStatement("CREATE TABLE IF NOT EXISTS playerRecords "
							+ "(creationDate VARCHAR(256),playerUUID VARCHAR(100),attemptedTranslations VARCHAR(256),successfulTranslations VARCHAR(256),"
							+ "lastTranslationTime VARCHAR(256),PRIMARY KEY (playerUUID))");
					initPlayerRecords.executeUpdate();
					initPlayerRecords.close();
					/* Sync ActiveTranslator data to corresponding table */
					main.getActiveTranslators().entrySet().forEach((entry) -> {
						CommonDefinitions.sendDebugMessage("(SQL) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
					    if (!entry.getValue().getHasBeenSaved()) {
					    	try {
					    		PreparedStatement newActiveTranslator = sqlConnection.prepareStatement("REPLACE activeTranslators"
						    			+ " (creationDate,playerUUID,inLangCode,outLangCode,rateLimit,rateLimitPreviousTime,translatingChatOutgoing,translatingChatIncoming,translatingBook,translatingSign,translatingItem,translatingEntity)" 
						    			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
						    	newActiveTranslator.setString(1, Instant.now().toString());
						    	newActiveTranslator.setString(2, entry.getValue().getUUID());
						    	newActiveTranslator.setString(3, entry.getValue().getInLangCode());
						    	newActiveTranslator.setString(4, entry.getValue().getOutLangCode());
						    	newActiveTranslator.setInt(5, entry.getValue().getRateLimit());
						    	newActiveTranslator.setString(6, entry.getValue().getRateLimitPreviousTime());
						    	newActiveTranslator.setBoolean(7, entry.getValue().getTranslatingChatOutgoing());
						    	newActiveTranslator.setBoolean(8, entry.getValue().getTranslatingChatIncoming());
						    	newActiveTranslator.setBoolean(9, entry.getValue().getTranslatingBook());
						    	newActiveTranslator.setBoolean(10, entry.getValue().getTranslatingSign());
						    	newActiveTranslator.setBoolean(11, entry.getValue().getTranslatingItem());
						    	newActiveTranslator.setBoolean(12, entry.getValue().getTranslatingEntity());
						    	newActiveTranslator.executeUpdate();
						    	newActiveTranslator.close();
					    	} catch (SQLException e) {
								e.printStackTrace();
								return;
							}
					    	CommonDefinitions.sendDebugMessage("(SQL) Created/updated unsaved user data config of " + entry.getKey() + ".");
					    	entry.getValue().setHasBeenSaved(true);
					    }
					});
					/* Delete any old ActiveTranslators */
					ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators");
					while (rs.next()) {
						if (main.getActiveTranslator(rs.getString("playerUUID")).getUUID().equals("")) {
							String uuid = rs.getString("playerUUID");
							PreparedStatement deleteOldItem = sqlConnection.prepareStatement("DELETE FROM activeTranslators WHERE playerUUID = ?");
							deleteOldItem.setString(1, uuid);
							deleteOldItem.executeUpdate();
							deleteOldItem.close();
							CommonDefinitions.sendDebugMessage("(SQL) Deleted user data config of " + uuid + ".");
						}
					}
					
					/* Sync PlayerRecord data to corresponding table */
                    main.getPlayerRecords().entrySet().forEach((entry) -> {
                    	CommonDefinitions.sendDebugMessage("(SQL) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
                        if (!entry.getValue().getHasBeenSaved()) {
                        	try {
                        		PreparedStatement newPlayerRecord = sqlConnection.prepareStatement("REPLACE playerRecords"
                        				+ " (creationDate,playerUUID,attemptedTranslations,successfulTranslations,lastTranslationTime) VALUES (?,?,?,?,?)");
                        		newPlayerRecord.setString(1, Instant.now().toString());
                        		newPlayerRecord.setString(2, entry.getValue().getUUID());
                        		newPlayerRecord.setInt(3, entry.getValue().getAttemptedTranslations());
                        		newPlayerRecord.setInt(4, entry.getValue().getSuccessfulTranslations());
                        		newPlayerRecord.setString(5, entry.getValue().getLastTranslationTime());
                        		newPlayerRecord.executeUpdate();
                        		newPlayerRecord.close();
                        	} catch (SQLException e) {
                        		e.printStackTrace();
                        		return;
                        	}
                        	CommonDefinitions.sendDebugMessage("(SQL) Created/updated unsaved user record of " + entry.getKey() + ".");
                        	entry.getValue().setHasBeenSaved(true);
					    }
					});
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			/* Last resort, sync activeTranslators to disk */
			// Save all new activeTranslators
			main.getActiveTranslators().entrySet().forEach((entry) -> {
				CommonDefinitions.sendDebugMessage("(YAML) Translation data of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				if (!entry.getValue().getHasBeenSaved()) {
					CommonDefinitions.sendDebugMessage("(YAML) Created/updated unsaved user data config of " + entry.getKey() + ".");
					entry.getValue().setHasBeenSaved(true);
					createUserDataConfig(entry.getValue());
				}
			});
			// Delete any old activeTranslators
			File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
			for (String eaName : userSettingsDir.list()) {
				File currFile = new File(userSettingsDir, eaName);
				if (main.getActiveTranslator(
						currFile.getName().substring(0, currFile.getName().indexOf("."))).getUUID().equals("")) {
					CommonDefinitions.sendDebugMessage("(YAML) Deleted user data config of "
							+ currFile.getName().substring(0, currFile.getName().indexOf(".")) + ".");
					currFile.delete();
				}
			}

			/* Sync playerRecords to disk */
			main.getPlayerRecords().entrySet().forEach((entry) -> {
				CommonDefinitions.sendDebugMessage("(YAML) Record of " + entry.getKey() + " save status: " + entry.getValue().getHasBeenSaved());
				if (!entry.getValue().getHasBeenSaved()) {
					CommonDefinitions.sendDebugMessage("(YAML) Created/updated unsaved user record of " + entry.getKey() + ".");
					entry.getValue().setHasBeenSaved(true);
					createStatsConfig(entry.getValue());
				}
			});
		}
	}

	/* Getters */
	public YamlConfiguration getMainConfig() {
		return mainConfig;
	}

	public YamlConfiguration getMessagesConfig() {
		return messagesConfig;
	}

	public File getConfigFile() {
		return configFile;
	}

	public File getMessagesFile() {
		return messagesFile;
	}
}