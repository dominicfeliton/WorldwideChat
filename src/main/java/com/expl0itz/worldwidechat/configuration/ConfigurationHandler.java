package com.expl0itz.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
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

	private WorldwideChat main = WorldwideChat.getInstance();

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
		// General
		mainConfig.addDefault("General.prefixName", "WWC");
		mainConfig.addDefault("General.enablebStats", true);
		mainConfig.addDefault("General.pluginLang", "en");
		mainConfig.addDefault("General.updateCheckerDelay", 86400);
		mainConfig.addDefault("General.syncUserDataDelay", 7200);
		mainConfig.addDefault("General.enableDebugMode", false);

		// Chat
		mainConfig.addDefault("Chat.sendTranslationChat", true);
		mainConfig.addDefault("Chat.sendPluginUpdateChat", true);

		// Translator
		mainConfig.addDefault("Translator.useWatsonTranslate", true);
		mainConfig.addDefault("Translator.watsonAPIKey", "");
		mainConfig.addDefault("Translator.watsonURL", "");
		mainConfig.addDefault("Translator.useGoogleTranslate", false);
		mainConfig.addDefault("Translator.googleTranslateAPIKey", "");
		mainConfig.addDefault("Translator.useAmazonTranslate", false);
		mainConfig.addDefault("Translator.amazonAccessKey", "");
		mainConfig.addDefault("Translator.amazonSecretKey", "");
		mainConfig.addDefault("Translator.amazonRegion", "");
		mainConfig.addDefault("Translator.translatorCacheSize", 100);
		mainConfig.addDefault("Translator.rateLimit", 0);
		mainConfig.addDefault("Translator.errorLimit", 5);
		mainConfig.addDefault("Translator.maxResponseTime", 7);

		mainConfig.options().copyDefaults(true);
		saveMainConfig(false);

		/* Get plugin lang */
		for (int i = 0; i < CommonDefinitions.supportedPluginLangCodes.length; i++) {
			if (CommonDefinitions.supportedPluginLangCodes[i]
					.equalsIgnoreCase(getMainConfig().getString("General.pluginLang"))) {
				main.setPluginLang(getMainConfig().getString("General.pluginLang"));
				main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + main.getPluginLang() + ".");
				return;
			}
		}

		main.getLogger().warning("Unable to detect a valid language in your config.yml. Defaulting to en...");
	}

	/* Init Messages Method */
	public void initMessagesConfig() {
		/* Init config file */
		messagesFile = new File(main.getDataFolder(), "messages-" + main.getPluginLang() + ".yml");

		/* Save default messages file if it does not exist */
		if (!messagesFile.exists()) {
			main.saveResource("messages-" + main.getPluginLang() + ".yml", true);
			
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
			main.saveResource("messages-" + main.getPluginLang() + ".yml", true);
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
		if (getMainConfig().getBoolean("General.enableDebugMode")) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigEnabledDebugMode"));
			main.setDebugMode(true);
		}
		// Prefix
		try {
			if (!getMainConfig().getString("General.prefixName").equalsIgnoreCase("Default")
					&& !getMainConfig().getString("General.prefixName").equalsIgnoreCase("WWC")) {
				main.setPrefixName(getMainConfig().getString("General.prefixName"));
			} else {
				main.setPrefixName("WWC"); // If default the entry for prefix, interpret as WWC
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigInvalidPrefixSettings"));
		}
		// bStats
		main.setbStats(getMainConfig().getBoolean("General.enablebStats"));
		if (getMainConfig().getBoolean("General.enablebStats")) {
			@SuppressWarnings("unused")
			Metrics metrics = new Metrics(WorldwideChat.getInstance(), WorldwideChat.getInstance().getbStatsID());
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigEnabledbStats"));
		} else {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigDisabledbStats"));
		}
		// Update Checker Delay
		try {
			if ((getMainConfig().getInt("General.updateCheckerDelay") > 10)) {
				main.setUpdateCheckerDelay(getMainConfig().getInt("General.updateCheckerDelay"));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
				main.setUpdateCheckerDelay(86400);
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigBadUpdateDelay"));
			main.setUpdateCheckerDelay(86400);
		}
		// Sync User Data Delay
		try {
			if ((getMainConfig().getInt("General.syncUserDataDelay") > 10)) {
				main.setSyncUserDataDelay(getMainConfig().getInt("General.syncUserDataDelay"));
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigSyncDelayEnabled", new String[] {main.getSyncUserDataDelay() + ""}));
			} else {
				main.setSyncUserDataDelay(7200);
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			main.setSyncUserDataDelay(7200);
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigSyncDelayInvalid"));
		}
		// Rate limit Settings
		try {
			if (getMainConfig().getInt("Translator.rateLimit") > 0) {
				main.setRateLimit(getMainConfig().getInt("Translator.rateLimit"));
				main.getLogger().info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigRateLimitEnabled", new String[] {"" + main.getRateLimit()}));
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigRateLimitInvalid"));
			main.setRateLimit(0);
		}
		// Cache Settings
		try {
			if (getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigCacheEnabled", new String[] {getMainConfig().getInt("Translator.translatorCacheSize") + ""}));
				main.setTranslatorCacheLimit(getMainConfig().getInt("Translator.translatorCacheSize"));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheDisabled"));
				main.setTranslatorCacheLimit(0);
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigCacheInvalid"));
			main.setTranslatorCacheLimit(100);
		}
		// Error Limit Settings
		try {
			if (getMainConfig().getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigErrorLimitEnabled", new String[] {getMainConfig().getInt("Translator.errorLimit") + ""}));
				main.setErrorLimit(getMainConfig().getInt("Translator.errorLimit"));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
				main.setErrorLimit(5);
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigErrorLimitInvalid"));
			main.setErrorLimit(5);
		}
		// Max Response Time Settings
		try {
			if (getMainConfig().getInt("Translator.maxResponseTime") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + CommonDefinitions.getMessage("wwcConfigMaxResponseTimeEnabled", new String[] {getMainConfig().getInt("Translator.maxResponseTime") + ""}));
				main.setMaxResponseTime(getMainConfig().getInt("Translator.maxResponseTime"));
			} else {
				main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMaxResponseTimeInvalid"));
				main.setMaxResponseTime(7);
			}
		} catch (Exception e) {
			main.getLogger().warning(CommonDefinitions.getMessage("wwcConfigMaxResponseTimeInvalid"));
			main.setMaxResponseTime(7);
		}
	}

	/* Translator Settings */
	public void loadTranslatorSettings() {
		String outName = "Invalid";
		try {
			if (getMainConfig().getBoolean("Translator.useWatsonTranslate")
					&& (!(getMainConfig().getBoolean("Translator.useGoogleTranslate"))
							&& (!(getMainConfig().getBoolean("Translator.useAmazonTranslate"))))) {
				outName = "Watson";
				WatsonTranslation test = new WatsonTranslation(getMainConfig().getString("Translator.watsonAPIKey"),
						getMainConfig().getString("Translator.watsonURL"));
				test.useTranslator();
			} else if (getMainConfig().getBoolean("Translator.useGoogleTranslate")
					&& (!(getMainConfig().getBoolean("Translator.useWatsonTranslate"))
							&& (!(getMainConfig().getBoolean("Translator.useAmazonTranslate"))))) {
				outName = "Google Translate";
				GoogleTranslation test = new GoogleTranslation(
						getMainConfig().getString("Translator.googleTranslateAPIKey"));
				test.useTranslator();
			} else if (getMainConfig().getBoolean("Translator.useAmazonTranslate")
					&& (!(getMainConfig().getBoolean("Translator.useGoogleTranslate"))
							&& (!(getMainConfig().getBoolean("Translator.useWatsonTranslate"))))) {
				outName = "Amazon Translate";
				AmazonTranslation test = new AmazonTranslation(getMainConfig().getString("Translator.amazonAccessKey"),
						getMainConfig().getString("Translator.amazonSecretKey"),
						getMainConfig().getString("Translator.amazonRegion"));
				test.useTranslator();
			} else if (getMainConfig().getBoolean("Translator.testModeTranslator")) {
				outName = "JUnit/MockBukkit Testing Translator";
				TestTranslation test = new TestTranslation(
						"TXkgYm95ZnJpZW5kICgyMk0pIHJlZnVzZXMgdG8gZHJpbmsgd2F0ZXIgdW5sZXNzIEkgKDI0RikgZHllIGl0IGJsdWUgYW5kIGNhbGwgaXQgZ2FtZXIganVpY2Uu");
				test.useTranslator();
			} else {
				getMainConfig().set("Translator.useWatsonTranslate", false);
				getMainConfig().set("Translator.useGoogleTranslate", false);
				getMainConfig().set("Translator.useAmazonTranslate", false);
				
				saveMainConfig(false);
				outName = "Invalid";
			}
		} catch (Exception e) {
			if (e instanceof InaccessibleObjectException) {
				// Watson does not work properly on 1.17 without --illegal-access=permit. Remove
				// this once the IBM devs fix it
				main.getLogger()
						.warning(CommonDefinitions.getMessage("wwcWatson117Warning"));
			} else {
				main.getLogger().severe("(" + outName + ") " + e.getMessage());
				e.printStackTrace();
			}
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
					CommonDefinitions
							.sendDebugMessage("Created/updated user data config of " + eaTranslator.getUUID() + ".");
					createUserDataConfig(eaTranslator);
				}
				// Delete any old activeTranslators
				File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
				for (String eaName : userSettingsDir.list()) {
					File currFile = new File(userSettingsDir, eaName);
					if (main.getActiveTranslator(
							currFile.getName().substring(0, currFile.getName().indexOf("."))).getUUID().equals("")) {
						CommonDefinitions.sendDebugMessage("Deleted user data config of "
								+ currFile.getName().substring(0, currFile.getName().indexOf(".")) + " .");
						currFile.delete();
					}
				}
			}

			/* Sync playerRecords to disk */
			for (PlayerRecord eaRecord : main.getPlayerRecords()) {
				CommonDefinitions.sendDebugMessage("Created/updated user record of " + eaRecord.getUUID() + ".");
				createStatsConfig(eaRecord);
			}
			// Delete old playerRecords (just in case)
			File playerRecordsDir = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
			for (String eaName : playerRecordsDir.list()) {
				File currFile = new File(playerRecordsDir, eaName);
				if (main.getPlayerRecord(currFile.getName().substring(0, currFile.getName().indexOf(".")),
						false).getUUID().equals("")) {
					CommonDefinitions.sendDebugMessage("Deleted user record of "
							+ currFile.getName().substring(0, currFile.getName().indexOf(".")) + ".");
					currFile.delete();
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