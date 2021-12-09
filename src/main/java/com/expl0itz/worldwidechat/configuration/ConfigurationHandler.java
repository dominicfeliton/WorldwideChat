package com.expl0itz.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.translators.AmazonTranslation;
import com.expl0itz.worldwidechat.translators.GoogleTranslation;
import com.expl0itz.worldwidechat.translators.TestTranslation;
import com.expl0itz.worldwidechat.translators.WatsonTranslation;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.Metrics;
import com.expl0itz.worldwidechat.util.PlayerRecord;

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

		//TODO: Check if setting works to overwrite mem vals (but not overwrite config) instead of setPluginLang
		mainConfig.set("General.pluginLang", "en");
		main.getLogger().warning("Unable to detect a valid language in your config.yml. Defaulting to en...");
		//main.setPluginLang("en");
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
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigInvalidPrefixSettings"));
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
			if ((mainConfig.getInt("General.updateCheckerDelay") > 10)) {
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigSyncDelayEnabled", new String[] {mainConfig.getInt("General.syncUserDataDelay") + ""}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigRateLimitEnabled", new String[] {"" + mainConfig.getInt("Translator.rateLimit")}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigRateLimitInvalid"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigRateLimitInvalid"));
		}
		// Per-message char limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigMessageCharLimitEnabled", new String[] {"" + mainConfig.getInt("Translator.messageCharLimit")}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMessageCharLimitInvalid"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMessageCharLimitInvalid"));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigCacheEnabled", new String[] {mainConfig.getInt("Translator.translatorCacheSize") + ""}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheDisabled"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheInvalid"));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigErrorLimitEnabled", new String[] {mainConfig.getInt("Translator.errorLimit") + ""}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
		}
		// Max Response Time Settings
		try {
			if (mainConfig.getInt("Translator.maxResponseTime") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigMaxResponseTimeEnabled", new String[] {mainConfig.getInt("Translator.maxResponseTime") + ""}));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMaxResponseTimeInvalid"));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMaxResponseTimeInvalid"));
		}
	}

	/* Translator Settings */
	public void loadTranslatorSettings() {
		String outName = "Invalid";
		try {
			if (mainConfig.getBoolean("Translator.useWatsonTranslate")
					&& (!(mainConfig.getBoolean("Translator.useGoogleTranslate"))
							&& (!(mainConfig.getBoolean("Translator.useAmazonTranslate"))))) {
				outName = "Watson";
				WatsonTranslation test = new WatsonTranslation(mainConfig.getString("Translator.watsonAPIKey"),
						mainConfig.getString("Translator.watsonURL"));
				test.useTranslator();
			} else if (mainConfig.getBoolean("Translator.useGoogleTranslate")
					&& (!(mainConfig.getBoolean("Translator.useWatsonTranslate"))
							&& (!(mainConfig.getBoolean("Translator.useAmazonTranslate"))))) {
				outName = "Google Translate";
				GoogleTranslation test = new GoogleTranslation(
						mainConfig.getString("Translator.googleTranslateAPIKey"));
				test.useTranslator();
			} else if (mainConfig.getBoolean("Translator.useAmazonTranslate")
					&& (!(mainConfig.getBoolean("Translator.useGoogleTranslate"))
							&& (!(mainConfig.getBoolean("Translator.useWatsonTranslate"))))) {
				outName = "Amazon Translate";
				AmazonTranslation test = new AmazonTranslation(mainConfig.getString("Translator.amazonAccessKey"),
						mainConfig.getString("Translator.amazonSecretKey"),
						mainConfig.getString("Translator.amazonRegion"));
				test.useTranslator();
			} else if (mainConfig.getBoolean("Translator.testModeTranslator")) {
				outName = "JUnit/MockBukkit Testing Translator";
				TestTranslation test = new TestTranslation(
						"TXkgYm95ZnJpZW5kICgyMk0pIHJlZnVzZXMgdG8gZHJpbmsgd2F0ZXIgdW5sZXNzIEkgKDI0RikgZHllIGl0IGJsdWUgYW5kIGNhbGwgaXQgZ2FtZXIganVpY2Uu");
				test.useTranslator();
			} else {
				mainConfig.set("Translator.useWatsonTranslate", false);
				mainConfig.set("Translator.useGoogleTranslate", false);
				mainConfig.set("Translator.useAmazonTranslate", false);
				
				saveMainConfig(false);
				outName = "Invalid";
			}
		} catch (Exception e) {
			main.getLogger().severe("(" + outName + ") " + e.getMessage());
			e.printStackTrace();
			outName = "Invalid";
		}
		if (outName.equals("Invalid")) {
			main.getLogger().severe(CommonDefinitions.getMessage("wwcInvalidTranslator"));
		} else {
			main.getLogger().info(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigConnectionSuccess", new String[] {outName}));
		}
		main.setTranslatorName(outName);
	}

	/* Per User Settings Saver */
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

	/* Stats File Creator */
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
		if (async && main.isEnabled()) {
			CommonDefinitions.sendDebugMessage("Saving main config async!");
			new BukkitRunnable() {
				@Override
				public void run() {
					saveMainConfig(false);
				}
			}.runTaskAsynchronously(main);
			return;
		}
		CommonDefinitions.sendDebugMessage("Saving main config sync!");
		saveCustomConfig(mainConfig, configFile, false);
	}
	
	/* Messages config save method */
	public void saveMessagesConfig(boolean async) {
		if (async && main.isEnabled()) {
			CommonDefinitions.sendDebugMessage("Saving messages config async!");
			new BukkitRunnable() {
				@Override
				public void run() {
					saveMessagesConfig(false);
				}
			}.runTaskAsynchronously(main);
			return;
		}
		CommonDefinitions.sendDebugMessage("Saving messages config sync!");
		saveCustomConfig(messagesConfig, messagesFile, false);
	}
	
	/* Custom config save method */
	public synchronized void saveCustomConfig(YamlConfiguration inConfig, File dest, boolean async) {
		if (async && main.isEnabled()) {
			CommonDefinitions.sendDebugMessage("Saving custom config async!");
			new BukkitRunnable() {
				@Override
				public void run() {
					saveCustomConfig(inConfig, dest, false);
				}
			}.runTaskAsynchronously(main);
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
	
	/* Sync user data to disk */
	public void syncData() {
		if (!main.getTranslatorName().equals("Invalid")) {
			/* Sync activeTranslators to disk */
			synchronized (main.getActiveTranslators()) {
				// Save all new activeTranslators
				for (ActiveTranslator eaTranslator : main.getActiveTranslators()) {
					CommonDefinitions.sendDebugMessage("Translation data of " + eaTranslator.getUUID() + " save status: " + eaTranslator.getHasBeenSaved());
					if (!eaTranslator.getHasBeenSaved()) {
						CommonDefinitions.sendDebugMessage("Created/updated unsaved user data config of " + eaTranslator.getUUID() + ".");
						eaTranslator.setHasBeenSaved(true);
						createUserDataConfig(eaTranslator);
					}
				}
				// Delete any old activeTranslators
				File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
				for (String eaName : userSettingsDir.list()) {
					File currFile = new File(userSettingsDir, eaName);
					if (main.getActiveTranslator(
							currFile.getName().substring(0, currFile.getName().indexOf("."))).getUUID().equals("")) {
						CommonDefinitions.sendDebugMessage("Deleted user data config of "
								+ currFile.getName().substring(0, currFile.getName().indexOf(".")) + ".");
						currFile.delete();
					}
				}
			}

			/* Sync playerRecords to disk */
			for (PlayerRecord eaRecord : main.getPlayerRecords()) {
				CommonDefinitions.sendDebugMessage("Record of " + eaRecord.getUUID() + " save status: " + eaRecord.getHasBeenSaved());
				if (!eaRecord.getHasBeenSaved()) {
					CommonDefinitions.sendDebugMessage("Created/updated unsaved user record of " + eaRecord.getUUID() + ".");
					eaRecord.setHasBeenSaved(true);
					createStatsConfig(eaRecord);
				}
			}
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