package com.dominicfeliton.worldwidechat.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.*;
import com.dominicfeliton.worldwidechat.util.storage.MongoDBUtils;
import com.dominicfeliton.worldwidechat.util.storage.PostgresUtils;
import com.dominicfeliton.worldwidechat.util.storage.SQLUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
			main.getLogger().warning(refs.getPlainMsg("wwcBlacklistBadFormat"));
			blacklistConfig = null;
			return;
		}
		List<String> bannedWords = blacklistConfig.getStringList("bannedWords");
		if (bannedWords == null || bannedWords.contains(null)) {
			main.getLogger().warning(refs.getPlainMsg("wwcBlacklistBadFormat"));
			blacklistConfig = null;
			return;
		}

		main.setBlacklistTerms(bannedWords);
		if (main.isBlacklistEnabled()) {
			main.getLogger().info(refs.getPlainMsg("wwcBlacklistLoaded",
					new String[] {"&6"+bannedWords.size()},
					"&d"));
		}
	}

	public void initAISettings() {
		refs.debugMsg("Loading AI settings...");

		String name = "ai-settings.yml";
		aiFile = new File(main.getDataFolder(), name);
		aiConfig = configGen.setupConfig(aiFile);
		YamlConfiguration templateConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(main.getResource(name), StandardCharsets.UTF_8));

		/* Validate the configuration */
		// systemPrompt (jsonDefaultSystemPrompt should always be overwritten)
		aiConfig.set("jsonDefaultSystemPrompt", templateConfig.getString("jsonDefaultSystemPrompt"));
		aiConfig.set("plainTextDefaultSystemPrompt", templateConfig.getString("plainTextDefaultSystemPrompt"));

		String systemPrompt = "";
		if (mainConfig.getBoolean("Translator.useChatGPT")) {
			refs.debugMsg("JSON sys prompt");
			systemPrompt = aiConfig.getString("jsonOverrideSystemPrompt").equalsIgnoreCase("default") ?
					aiConfig.getString("jsonDefaultSystemPrompt") :
					aiConfig.getString("jsonOverrideSystemPrompt");
			if (systemPrompt == null) {
				main.getLogger().warning(refs.getPlainMsg("wwcAiSystemPromptBad"));
				systemPrompt = templateConfig.getString("jsonDefaultSystemPrompt");
			}
		} else {
			refs.debugMsg("Plaintext sys prompt");
			systemPrompt = aiConfig.getString("plainTextOverrideSystemPrompt").equalsIgnoreCase("default")  ?
					aiConfig.getString("plainTextDefaultSystemPrompt") :
					aiConfig.getString("plainTextOverrideSystemPrompt");
			if (systemPrompt == null) {
				main.getLogger().warning(refs.getPlainMsg("wwcAiSystemPromptBad"));
				systemPrompt = templateConfig.getString("plainTextDefaultSystemPrompt");
			}
		}
		main.setAISystemPrompt(systemPrompt);

		// supportedLangs
		// TODO: copy supportedLangs to mem instead of setting.
		if (!aiConfig.isList("supportedLangs")) {
			main.getLogger().warning(refs.getPlainMsg("wwcAiSupportedLangsBad"));
			aiConfig.set("supportedLangs", templateConfig.getString("supportedLangs"));
			return;
		}

		if (mainConfig.getBoolean("Translator.useChatGPT")) {
			main.getLogger().info(ChatColor.LIGHT_PURPLE + refs.getPlainMsg("wwcAiSystemPromptLoaded"));
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
				main.getLogger().warning(refs.getPlainMsg("wwclLangNotLoadedConsole",
						new String[] {"&c"+eaStr},
						"&e"));
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
			main.getLogger().warning(refs.getPlainMsg("wwcConfigEnabledDebugMode"));
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
			main.getLogger().warning(refs.getPlainMsg("wwcConfigInvalidPrefixSettings"));
		}
		// Fatal Async Timeout Delay
		try {
			if (mainConfig.getInt("General.fatalAsyncTaskTimeout") > 7) {
				WorldwideChat.translatorFatalAbortSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout");
				WorldwideChat.translatorConnectionTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
				WorldwideChat.asyncTasksTimeoutSeconds = mainConfig.getInt("General.fatalAsyncTaskTimeout") - 2;
			} else {
				main.getLogger().warning(refs.getPlainMsg("wwcConfigInvalidFatalAsyncTimeout"));
			}
		} catch (Exception e) {
			main.getLogger().warning(refs.getPlainMsg("wwcConfigInvalidFatalAsyncTimeout"));
		}
		// bStats
		if (mainConfig.getBoolean("General.enablebStats")) {
			Metrics metrics = new Metrics(WorldwideChat.instance, WorldwideChat.bStatsID);
			main.getLogger()
					.info(ChatColor.LIGHT_PURPLE + refs.getPlainMsg("wwcConfigEnabledbStats"));
		} else {
			main.getLogger().warning(refs.getPlainMsg("wwcConfigDisabledbStats"));
		}
		// Update Checker Delay
		try {
			if (!(mainConfig.getInt("General.updateCheckerDelay") > 10)) {
				main.setUpdateCheckerDelay(86400);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigBadUpdateDelay"));
			} else {
				main.setUpdateCheckerDelay(mainConfig.getInt("General.updateCheckerDelay"));
			}
		} catch (Exception e) {
			main.setUpdateCheckerDelay(86400);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigBadUpdateDelay"));
		}
		// Sync User Data Delay
		try {
			if ((mainConfig.getInt("General.syncUserDataDelay") > 10)) {
				main.getLogger().info(
						refs.getPlainMsg("wwcConfigSyncDelayEnabled",
								"&6"+mainConfig.getInt("General.syncUserDataDelay"),
								"&d"));
				main.setSyncUserDataDelay(mainConfig.getInt("General.syncUserDataDelay"));
			} else {
				main.setSyncUserDataDelay(7200);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigSyncDelayInvalid"));
			}
		} catch (Exception e) {
			main.setSyncUserDataDelay(7200);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigSyncDelayInvalid"));
		}
		// Sync User Localizations
		try {
			main.setSyncUserLocal(mainConfig.getBoolean("General.syncUserLocalization"));
		} catch (Exception e) {
			main.setSyncUserLocal(true);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigSyncUserLocalInvalid"));
		}
		// Rate limit Settings
		try {
			if (mainConfig.getInt("Translator.rateLimit") >= 0) {
				main.getLogger().info(refs.getPlainMsg("wwcConfigRateLimitEnabled",
						"&6"+mainConfig.getInt("Translator.rateLimit"),
						"&d"));
				main.setGlobalRateLimit(mainConfig.getInt("Translator.rateLimit"));
			} else {
				main.setGlobalRateLimit(0);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigRateLimitInvalid"));
			}
		} catch (Exception e) {
			main.setGlobalRateLimit(0);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigRateLimitInvalid"));
		}
		// Per-message Char Limit Settings
		try {
			if (mainConfig.getInt("Translator.messageCharLimit") >= 0 && mainConfig.getInt("Translator.messageCharLimit") <= 255) {
				main.setMessageCharLimit(mainConfig.getInt("Translator.messageCharLimit"));
				main.getLogger().info(refs.getPlainMsg("wwcConfigMessageCharLimitEnabled",
						"&6"+mainConfig.getInt("Translator.messageCharLimit"),
						"&d"));
			} else {
				main.setMessageCharLimit(255);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigMessageCharLimitInvalid"));
			}
		} catch (Exception e) {
			main.setMessageCharLimit(255);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigMessageCharLimitInvalid"));
		}
		// Chat Listener Priority
		try {
			EventPriority eventPriority = EventPriority.valueOf(mainConfig.getString("Chat.chatListenerPriority").toUpperCase());
			main.setChatPriority(eventPriority);
			main.getLogger().info(refs.getPlainMsg("wwcConfigEventPrioritySet",
					"&6"+mainConfig.getString("Chat.chatListenerPriority"),
					"&d"));
		} catch (Exception e) {
			main.setChatPriority(EventPriority.HIGHEST);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigEventPriorityInvalid",
					"&6HIGHEST",
					""));
		}
		// Vault Support
		try {
            main.setVaultSupport(mainConfig.getBoolean("Chat.useVault"));
		} catch (Exception e) {
			main.setVaultSupport(true);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigVaultSupportInvalid"));
		}
		// Blacklist
		try {
			main.setBlacklistStatus(mainConfig.getBoolean("Chat.enableBlacklist"));
		} catch (Exception e) {
			main.setBlacklistStatus(true);
			main.getLogger().warning(refs.getPlainMsg("wwcBlacklistBadFormat"));
		}
		// Separate Chat Channel Prefix
		try {
			main.setTranslateIcon(mainConfig.getString("Chat.separateChatChannel.icon"));
		} catch (Exception e) {
			main.setTranslateIcon("globe");
			main.getLogger().warning(refs.getPlainMsg("wwcConfigChatChannelIconInvalid",
					"&6"+refs.serial(main.getTranslateIcon()),
					""));
		}
		// Separate Chat Channel Format
		try {
			String format = mainConfig.getString("Chat.separateChatChannel.format");
			main.setTranslateFormat(format);

			int count = format.length() - format.replace("%", "").length();
			if (count > 1 && main.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
				main.getLogger().info(refs.getPlainMsg("wwcConfigPAPIInstalled", "", "&d"));
			} else if (count > 1 && main.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
				main.getLogger().warning(refs.getPlainMsg("wwcConfigPAPINotInstalled"));
			}
		} catch (Exception e) {
			main.setTranslateFormat("{prefix}{username}{suffix}:");
			main.getLogger().warning(refs.getPlainMsg("wwcConfigChatChannelFormatInvalid",
					"&6{prefix}{username}{suffix}:",
					""));
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
			// TODO: Escape &o?
			main.getLogger().warning(refs.getPlainMsg("wwcConfigChatChannelHoverFormatInvalid",
					"&o{local:wwcOrigHover}:",
					"&d"));
		}
		// Always Force Separate Chat Channel
		try {
			if (mainConfig.getBoolean("Chat.separateChatChannel.force")) {
				main.setForceSeparateChatChannel(true);
				main.getLogger().info(refs.getPlainMsg("wwcConfigForceChatChannelEnabled",
						"",
						"&d"));
			} else {
				main.setForceSeparateChatChannel(false);
			}
		} catch (Exception e) {
			main.setForceSeparateChatChannel(false);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigChatChannelInvalid"));
		}
		// Cache Settings
		try {
			if (mainConfig.getInt("Translator.translatorCacheSize") > 0) {
				main.getLogger()
						.info(refs.getPlainMsg("wwcConfigCacheEnabled",
								"&6"+mainConfig.getInt("Translator.translatorCacheSize"),
								"&d"));
			    // Set cache to size beforehand, so we can avoid expandCapacity :)
				main.setCacheProperties(mainConfig.getInt("Translator.translatorCacheSize"));
			} else {
				main.setCacheProperties(0);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigCacheDisabled"));
			}
		} catch (Exception e) {
			main.setCacheProperties(100);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigCacheInvalid"));
		}
		// Persistent Cache Settings
		try {
			if (mainConfig.getBoolean("Translator.enablePersistentCache")) {
				main.getLogger()
						.info(refs.getPlainMsg("wwcConfigPersistentCacheEnabled",
								"",
								"&d"));
				main.setPersistentCache(true);
			} else {
				main.setPersistentCache(false);
			}
		} catch (Exception e) {
			main.setPersistentCache(false);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigPersistentCacheInvalid"));
		}
		// Error Limit Settings
		try {
			if (mainConfig.getInt("Translator.errorLimit") > 0) {
				main.getLogger().info(
						refs.getPlainMsg("wwcConfigErrorLimitEnabled",
								"&6"+mainConfig.getInt("Translator.errorLimit"),
								"&d"));
				main.setErrorLimit(mainConfig.getInt("Translator.errorLimit"));
			} else {
				main.setErrorLimit(5);
				main.getLogger().warning(refs.getPlainMsg("wwcConfigErrorLimitInvalid"));
			}
		} catch (Exception e) {
			main.setErrorLimit(5);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigErrorLimitInvalid"));
		}
		// List of Errors to Ignore Settings
		try {
			main.setErrorsToIgnore((ArrayList<String>) mainConfig.getStringList("Translator.errorsToIgnore"));
			main.getLogger().info(
					refs.getPlainMsg("wwcConfigErrorsToIgnoreSuccess",
							"",
							"&d"));
		} catch (Exception e) {
			ArrayList<String> defaultArr = new ArrayList<>(Arrays.asList("confidence", "same as target", "detect the source language", "Unable to find model for specified languages"));
			main.setErrorsToIgnore(defaultArr);
			main.getLogger().warning(refs.getPlainMsg("wwcConfigErrorsToIgnoreInvalid"));
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
				main.getLogger().info(refs.getPlainMsg("wwcConfigConnectionSuccess",
						"&6SQL",
						"&a"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getPlainMsg("wwcConfigConnectionFail",
						"&6SQL",
						"&a"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getSqlSession() != null) {
					main.getSqlSession().disconnect();
					main.setSqlSession(null);
				}
				main.getLogger().severe(refs.getPlainMsg("wwcConfigYAMLFallback"));
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB")) {
			try {
				MongoDBUtils mongo = new MongoDBUtils(mainConfig.getString("Storage.mongoHostname"), mainConfig.getString("Storage.mongoPort"),
						mainConfig.getString("Storage.mongoDatabaseName"), mainConfig.getString("Storage.mongoUsername"),
						mainConfig.getString("Storage.mongoPassword"), (List<String>) mainConfig.getList("Storage.mongoOptionalArgs"));
				mongo.connect();
				main.setMongoSession(mongo);
				main.getLogger().info(refs.getPlainMsg("wwcConfigConnectionSuccess",
						"&6MongoDB",
						"&a"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getPlainMsg("wwcConfigConnectionFail",
						"&6MongoDB",
						"&a"));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getMongoSession() != null) {
					main.getMongoSession().disconnect();
					main.setMongoSession(null);
				}
				main.getLogger().severe(refs.getPlainMsg("wwcConfigYAMLFallback"));
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL")) {
			try {
				PostgresUtils postgres = new PostgresUtils(mainConfig.getString("Storage.postgresHostname"), mainConfig.getString("Storage.postgresPort"),
						mainConfig.getString("Storage.postgresDatabaseName"), mainConfig.getString("Storage.postgresUsername"), mainConfig.getString("Storage.postgresPassword"),
						(List<String>) mainConfig.getList("Storage.postgresOptionalArgs"), mainConfig.getBoolean("Storage.postgresSSL"));
				postgres.connect();
				main.setPostgresSession(postgres);
				main.getLogger().info(refs.getPlainMsg("wwcConfigConnectionSuccess",
						"&6Postgres",
						"&a"));
			} catch (Exception e) {
				main.getLogger().severe(refs.getPlainMsg("wwcConfigConnectionFail",
						"Postgres",
						""));
				main.getLogger().warning(ExceptionUtils.getMessage(e));
				if (main.getPostgresSession() != null) {
					main.getPostgresSession().disconnect();
					main.setPostgresSession(null);
				}
				main.getLogger().severe(refs.getPlainMsg("wwcConfigYAMLFallback"));
			}
		} else {
			main.getLogger().info(refs.getPlainMsg("wwcConfigYAMLDefault",
					"",
					"&a"));
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
				main.getLogger().warning(refs.getPlainMsg("wwcTranslatorAttempt",
						new String[] {tryNumber + "", maxTries + ""},
						""));
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
			main.getLogger().severe(refs.getPlainMsg("wwcInvalidTranslator"));
		} else {
			main.getLogger().info(refs.getPlainMsg("wwcConfigConnectionSuccess",
					"&6"+outName,
					"&a"));
		}
		refs.debugMsg("Landing on " + outName);
		return outName;
	}
	
	/* Main config save method */
	public void saveMainConfig(boolean async) {
		if (async) {
			refs.debugMsg("Saving main config async!");
			GenericRunnable out = new GenericRunnable() {
				@Override
				protected void execute() {
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
			GenericRunnable out = new GenericRunnable() {
				@Override
				protected void execute() {
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
			GenericRunnable out = new GenericRunnable() {
				@Override
				protected void execute() {
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

	public File getAIFile() {
		return aiFile;
	}
}