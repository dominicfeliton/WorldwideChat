package com.dominicfeliton.worldwidechat.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.*;
import com.dominicfeliton.worldwidechat.util.storage.MongoDBUtils;
import com.dominicfeliton.worldwidechat.util.storage.PostgresUtils;
import com.dominicfeliton.worldwidechat.util.storage.SQLUtils;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.threeten.bp.Instant;
import org.yaml.snakeyaml.Yaml;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class ConfigurationHandler {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();
	private ConfigurationGenerator configGen = new ConfigurationGenerator(this);

	private File configFile;
	private YamlConfiguration mainConfig;
	private YamlConfiguration aiConfig;

	private File aiFile;
	private File blacklistFile;
	private YamlConfiguration blacklistConfig;

	private ConcurrentHashMap<String, YamlConfiguration> pluginLangConfigs = new ConcurrentHashMap<>();

	// TODO: Split up this class. It is far too gargantuan...
	/* Init Main Config Method */
	public void initMainConfig() {
		/* Init config file */
		String name = "config.yml";
		configFile = new File(main.getDataFolder(), name);
		mainConfig = configGen.setupConfig(configFile);
		saveMainConfig(false);
		YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(main.getResource(name), StandardCharsets.UTF_8));

		/* Get plugin lang */
		if (refs.isSupportedLang(mainConfig.getString("General.pluginLang"), "local")) {
			main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + mainConfig.getString("General.pluginLang") + ".");
			return;
		}

		mainConfig.set("General.pluginLang", "en");
		main.getLogger().warning("Unable to detect a valid language in your config.yml. Defaulting to en...");
	}

	public void initBlacklistConfig() {
		refs.debugMsg("Loading blacklist...");

		/* Init config file */
		String name = "blacklist.yml";
		blacklistFile = new File(main.getDataFolder(), name);
		blacklistConfig = configGen.setupConfig(blacklistFile);
		YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(main.getResource(name), StandardCharsets.UTF_8));

		/* Validate the configuration */
		if (!blacklistConfig.isList("bannedWords")) {
			main.getLogger().warning(refs.getMsg("wwcBlacklistBadFormat", null));
			blacklistConfig = null;
			return;
		}
		List<String> bannedWords = blacklistConfig.getStringList("bannedWords");
		if (bannedWords == null || bannedWords.contains(null)) {
			main.getLogger().warning(refs.getMsg("wwcBlacklistBadFormat", null));
			blacklistConfig = null;
			return;
		}

		main.setBlacklistTerms(bannedWords);
		if (main.isBlacklistEnabled()) {
			main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcBlacklistLoaded", new String[] {bannedWords.size()+""}, null));
		}
	}

	public void initAISettings() {
		refs.debugMsg("Loading AI settings...");

		String name = "ai-settings.yml";
		aiFile = new File(main.getDataFolder(), name);
		aiConfig = configGen.setupConfig(aiFile);
		YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(main.getResource(name), StandardCharsets.UTF_8));

		/* Validate the configuration */
		// systemPrompt
		String systemPrompt = aiConfig.getString("translateSystemPrompt");
		if (systemPrompt == null) {
			main.getLogger().warning(refs.getMsg("wwcAiSystemPromptBad", null));
			aiConfig.set("translateSystemPrompt", templateConfig.getString("translateSystemPrompt"));
			return;
		}
		main.setAISystemPrompt(systemPrompt);

		// supportedLangs
		if (!aiConfig.isConfigurationSection("supportedLangs")) {
			main.getLogger().warning(refs.getMsg("wwcAiSupportedLangsBad", null));
			aiConfig.set("supportedLangs", templateConfig.getString("supportedLangs"));
			return;
		}

		for (Map.Entry<String, String> eaPair : CommonRefs.translatorPairs.entrySet()) {
			if (eaPair.getValue().equalsIgnoreCase("ChatGPT") && mainConfig.getBoolean(eaPair.getKey())) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcAiSystemPromptLoaded",null));
				break;
			}
		}
	}

	/* Init Messages Method */
	public void initMessagesConfigs() {
		// Init ALL message configs
		main.getLogger().warning("Importing/upgrading localization files...");
		Set<SupportedLang> uniqueLangs = new HashSet<>(supportedPluginLangCodes.values());
		for (SupportedLang eaLang : uniqueLangs) {
			String eaStr = eaLang.getLangCode();
			refs.debugMsg("Checking " + eaStr + "...");
			YamlConfiguration currConfig = generateMessagesConfig(eaStr);
			if (currConfig == null) {
				main.getLogger().warning(refs.serial(refs.getFancyMsg("wwclLangNotLoadedConsole", new String[] {"&c"+eaStr}, "&e", null)));
				continue;
			}

			pluginLangConfigs.put(eaStr, generateMessagesConfig(eaStr));
		}
		main.getLogger().warning("Done.");
	}

	public YamlConfiguration generateMessagesConfig(String inLocalLang) {
		/* Init config file */
		File msgFile = new File(main.getDataFolder(), "messages-" + inLocalLang + ".yml");
		if (main.getResource("messages-" + inLocalLang + ".yml") == null) {
			refs.debugMsg("!!! Skipping " + inLocalLang + ", not found in default resources??");
			return null;
		}

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
			if (msgFile.exists() && !msgFile.delete()) {
				refs.debugMsg("Could not delete old messages file for " + inLocalLang + "! Perhaps bad permissions?");
				return null;
			}

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
		// Not stored in main, since we want debug MSGs ASAP
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
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigEnabledbStats", null));
		} else {
			main.getLogger().warning(refs.getMsg("wwcConfigDisabledbStats", null));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				main.setUpdateCheckerDelay(86400);
				main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay", null));
			} else {
				main.setUpdateCheckerDelay(mainConfig.getInt("General.updateCheckerDelay"));
			}
		} catch (Exception e) {
			main.setUpdateCheckerDelay(86400);
			main.getLogger().warning(refs.getMsg("wwcConfigBadUpdateDelay", null));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigSyncDelayEnabled", mainConfig.getInt("General.syncUserDataDelay") + "", null));
				main.setSyncUserDataDelay(mainConfig.getInt("General.syncUserDataDelay"));
			} else {
				main.setSyncUserDataDelay(7200);
				main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid", null));
			}
		} catch (Exception e) {
			main.setSyncUserDataDelay(7200);
			main.getLogger().warning(refs.getMsg("wwcConfigSyncDelayInvalid", null));
		}
		// Sync User Localizations
		try {
			main.setSyncUserLocal(mainConfig.getBoolean("General.syncUserLocalization"));
		} catch (Exception e) {
			main.setSyncUserLocal(true);
			main.getLogger().warning(refs.getMsg("wwcConfigSyncUserLocalInvalid", null));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigRateLimitEnabled", "" + mainConfig.getInt("Translator.rateLimit"), null));
				main.setGlobalRateLimit(mainConfig.getInt("Translator.rateLimit"));
			} else {
				main.setGlobalRateLimit(0);
				main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid", null));
			}
		} catch (Exception e) {
			main.setGlobalRateLimit(0);
			main.getLogger().warning(refs.getMsg("wwcConfigRateLimitInvalid", null));
		}
		// Per-message Char Limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0 && mainConfig.getInt("Translator.messageCharLimit") <= 255) {
				main.setMessageCharLimit(mainConfig.getInt("Translator.messageCharLimit"));
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigMessageCharLimitEnabled", "" + mainConfig.getInt("Translator.messageCharLimit"), null));
			} else {
				main.setMessageCharLimit(255);
				main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid", null));
			}
		} catch (Exception e) {
			main.setMessageCharLimit(255);
			main.getLogger().warning(refs.getMsg("wwcConfigMessageCharLimitInvalid", null));
		}
		// Chat Listener Priority
		try {
			EventPriority eventPriority = EventPriority.valueOf(mainConfig.getString("Chat.chatListenerPriority").toUpperCase());
			main.setChatPriority(eventPriority);
			main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigEventPrioritySet", mainConfig.getString("Chat.chatListenerPriority"), null));
		} catch (Exception e) {
			main.setChatPriority(EventPriority.HIGHEST);
			main.getLogger().warning(refs.getMsg("wwcConfigEventPriorityInvalid", "HIGHEST",null));
		}
		// Vault Support
		try {
            main.setVaultSupport(mainConfig.getBoolean("Chat.useVault"));
		} catch (Exception e) {
			main.setVaultSupport(true);
			main.getLogger().warning(refs.getMsg("wwcConfigVaultSupportInvalid", null));
		}
		// Blacklist
		try {
			main.setBlacklistStatus(mainConfig.getBoolean("Chat.enableBlacklist"));
		} catch (Exception e) {
			main.setBlacklistStatus(true);
			main.getLogger().warning(refs.getMsg("wwcBlacklistBadFormat", null));
		}
		// Separate Chat Channel Prefix
		try {
			main.setTranslateIcon(mainConfig.getString("Chat.separateChatChannel.icon"));
		} catch (Exception e) {
			main.setTranslateIcon("globe");
			main.getLogger().warning(refs.getMsg("wwcConfigChatChannelIconInvalid",
					new String[] {refs.serial(main.getTranslateIcon())},
					null));
		}
		// Separate Chat Channel Format
		try {
			String format = mainConfig.getString("Chat.separateChatChannel.format");
			main.setTranslateFormat(format);

			int count = format.length() - format.replace("%", "").length();
			if (count > 1 && main.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigPAPIInstalled", null));
			} else if (count > 1 && main.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
				main.getLogger().warning(refs.getMsg("wwcConfigPAPINotInstalled", null));
			}
		} catch (Exception e) {
			main.setTranslateFormat("{prefix}{username}{suffix}:");
			main.getLogger().warning(refs.getMsg("wwcConfigChatChannelFormatInvalid",
					new String[] {"{prefix}{username}{suffix}:"},
					null));
		}
		// Hover Text Format
		try {
			if (mainConfig.getBoolean("Chat.sendIncomingHoverTextChat") ||
					mainConfig.getBoolean("Chat.sendOutgoingHoverTextChat")) {
				String format = mainConfig.getString("Chat.separateChatChannel.hoverFormat");
				main.setTranslateHoverFormat(format);
			} else {
				refs.debugMsg("Not setting hover text, setting to nothing");
				main.setTranslateHoverFormat("");
			}

			// PAPI detection not needed, already sufficiently warned by Format
		} catch (Exception e) {
			main.setTranslateHoverFormat("&o{local:wwcOrigHover}:");
			main.getLogger().warning(refs.getMsg("wwcConfigChatChannelHoverFormatInvalid",
					new String[] {"&o{local:wwcOrigHover}:"},
					null));
		}
		// Always Force Separate Chat Channel
		try {
			if (mainConfig.getBoolean("Chat.separateChatChannel.force")) {
				main.setForceSeparateChatChannel(true);
				main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigForceChatChannelEnabled", null));
			} else {
				main.setForceSeparateChatChannel(false);
			}
		} catch (Exception e) {
			main.setForceSeparateChatChannel(false);
			main.getLogger().warning(refs.getMsg("wwcConfigChatChannelInvalid", null));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigCacheEnabled", mainConfig.getInt("Translator.translatorCacheSize") + "", null));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				main.setCacheProperties(0);
				main.getLogger().warning(refs.getMsg("wwcConfigCacheDisabled", null));
			}
		} catch (Exception e) {
			main.setCacheProperties(100);
			main.getLogger().warning(refs.getMsg("wwcConfigCacheInvalid", null));
		}
		// Persistent Cache Settings
		try {
			if (mainConfig.getBoolean("Translator.enablePersistentCache")) {
				main.getLogger()
						.info(ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigPersistentCacheEnabled", null));
				main.setPersistentCache(true);
			} else {
				main.setPersistentCache(false);
			}
		} catch (Exception e) {
			main.setPersistentCache(false);
			main.getLogger().warning(refs.getMsg("wwcConfigPersistentCacheInvalid", null));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorLimitEnabled", mainConfig.getInt("Translator.errorLimit") + "", null));
				main.setErrorLimit(mainConfig.getInt("Translator.errorLimit"));
			} else {
				main.setErrorLimit(5);
				main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid", null));
			}
		} catch (Exception e) {
			main.setErrorLimit(5);
			main.getLogger().warning(refs.getMsg("wwcConfigErrorLimitInvalid", null));
		}
		// List of Errors to Ignore Settings
		try {
			main.setErrorsToIgnore((ArrayList<String>) mainConfig.getStringList("Translator.errorsToIgnore"));
			main.getLogger().info(
					ChatColor.LIGHT_PURPLE + refs.getMsg("wwcConfigErrorsToIgnoreSuccess", null));
		} catch (Exception e) {
			ArrayList<String> defaultArr = new ArrayList<>(Arrays.asList(new String[] {"confidence", "same as target", "detect the source language", "Unable to find model for specified languages"}));
			main.setErrorsToIgnore(defaultArr);
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
		refs.debugMsg("Landing on " + outName);
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
			wwcHelper.runAsync(out, ASYNC, null);
			return;
		}
		refs.debugMsg("Saving main config sync!");
		saveCustomConfig(mainConfig, configFile, false);
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
			wwcHelper.runAsync(out, ASYNC, null);
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
			wwcHelper.runAsync(out, ASYNC, null);
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

	/* Getters */
	public YamlConfiguration getMainConfig() {
		return mainConfig;
	}

	public YamlConfiguration getAIConfig() {
		return aiConfig;
	}

	public YamlConfiguration getBlacklistConfig() {
		return blacklistConfig;
	}

	public YamlConfiguration getMsgsConfig() {
		return pluginLangConfigs.get(mainConfig.getString("General.pluginLang"));
	}

	public File getConfigFile() {
		return configFile;
	}

	public File getBlacklistFile() {
		return blacklistFile;
	}

	public ConcurrentHashMap<String, YamlConfiguration> getPluginLangConfigs() {
		return pluginLangConfigs;
	}

}