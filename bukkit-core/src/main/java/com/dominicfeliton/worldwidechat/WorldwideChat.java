package com.dominicfeliton.worldwidechat;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

import com.dominicfeliton.worldwidechat.commands.*;
import com.dominicfeliton.worldwidechat.util.*;
import com.dominicfeliton.worldwidechat.util.storage.PostgresUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import com.dominicfeliton.worldwidechat.configuration.ConfigurationHandler;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.listeners.WWCTabCompleter;
import com.dominicfeliton.worldwidechat.runnables.LoadUserData;
import com.dominicfeliton.worldwidechat.runnables.SyncUserData;
import com.dominicfeliton.worldwidechat.runnables.UpdateChecker;
import com.dominicfeliton.worldwidechat.util.storage.MongoDBUtils;
import com.dominicfeliton.worldwidechat.util.storage.SQLUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;
import static com.dominicfeliton.worldwidechat.util.CommonRefs.supportedPluginLangCodes;
import static com.dominicfeliton.worldwidechat.util.CommonRefs.supportedMCVersions;
import static com.dominicfeliton.worldwidechat.util.CommonRefs.pluginLangConfigs;

public class WorldwideChat extends JavaPlugin {
	public static final int bStatsID = 10562;
	public static final String messagesConfigVersion = "06272024-2"; // MMDDYYYY-revisionNumber

	public static int translatorFatalAbortSeconds = 10;
	public static int translatorConnectionTimeoutSeconds = translatorFatalAbortSeconds - 2;
	public static int asyncTasksTimeoutSeconds = translatorConnectionTimeoutSeconds - 2;

	public static WorldwideChat instance;
	private BukkitAudiences adventure;

	private String currPlatform;

	private WorldwideChatHelper wwcHelper;
	private WWCInventoryManager inventoryManager;

	private ConfigurationHandler configurationManager;

	private MongoDBUtils mongoSession;

	private SQLUtils sqlSession;

	private PostgresUtils postgresSession;

	private ExecutorService callbackExecutor;

	private ServerAdapterFactory serverFactory;

	private Object scheduler;

	private CommonRefs refs;
	
	private Map<String, SupportedLang> supportedInputLangs = new ConcurrentHashMap<>();
	private Map<String, SupportedLang> supportedOutputLangs = new ConcurrentHashMap<>();
	private List<String> playersUsingConfigGUI = new CopyOnWriteArrayList<>();
	private Map<String, PlayerRecord> playerRecords = new ConcurrentHashMap<>();
	private Map<String, ActiveTranslator> activeTranslators = new ConcurrentHashMap<>();

	private Cache<CachedTranslation, String> cache = Caffeine.newBuilder()
			.maximumSize(100)
			.build();
	
	private int translatorErrorCount = 0;
	
	private boolean outOfDate = false;

	private volatile String translatorName = "Starting";

	/* Config values */
	private TextComponent pluginPrefix = Component.text().content("[").color(NamedTextColor.DARK_RED)
			.append(Component.text().content("WWC").color(NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
			.append(Component.text().content("]").color(NamedTextColor.DARK_RED))
			.build();

	private int updateCheckerDelay = 86400;

	private int syncUserDataDelay = 7200;

	private int globalRateLimit = 0;

	private int messageCharLimit = 255;

	private boolean persistentCache = false;

	private int errorLimit = 5;

	private ArrayList<String> errorsToIgnore = new ArrayList<>(Arrays.asList("confidence", "same as target", "detect the source language", "Unable to find model for specified languages"));

	/* Default constructor */
	public WorldwideChat() {
		super();
	}

	/* MockBukkit required constructor */
	protected WorldwideChat(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	/* Methods */
	public @NotNull BukkitAudiences adventure() {
		if (adventure == null) {
			throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
		}
		return adventure;
	}

	public ExecutorService getCallbackExecutor() { return callbackExecutor; }
	public WWCInventoryManager getInventoryManager() {
		return inventoryManager;
	}
	
	public void setConfigManager(ConfigurationHandler i) {
		configurationManager = i;
	}
	
	public ConfigurationHandler getConfigManager() {
		return configurationManager;
	}

	@Override
	public void onEnable() {
		// Initialize critical instances
		instance = this; // Static instance of this class
		serverFactory = new ServerAdapterFactory();

		// Check current server version + set adapters
		if (!checkAndInitAdapters()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Setup adventure if needed
		// TODO: Move BukkitAudiences to Adapters (therefore all of this)
		if (currPlatform.equals("Bukkit") || currPlatform.equals("Spigot")) {
			adventure = BukkitAudiences.create(this); // Adventure
		}

		// Setup inventory manager
		inventoryManager = new WWCInventoryManager(); // InventoryManager for SmartInvs API
		inventoryManager.init(); // Init InventoryManager

		// Load "secondary" services + plugin configs, check if they successfully initialized
		doStartupTasks(false);

		// Register event handlers
		wwcHelper.registerEventHandlers();

		// We made it!
		//refs.debugMsg("Async tasks running: " + this.getActiveAsyncTasks());
		getLogger().info(ChatColor.GREEN + refs.getMsg("wwcEnabled", getPluginVersion(), null));
	}

	@Override
	public void onDisable() {
		// Cleanly cancel/reset all background tasks (runnables, timers, vars, etc.)
		cancelBackgroundTasks(false);

		// Unregister listeners
		HandlerList.unregisterAll(this);

		// Set static vars to null
		if (adventure != null) {
			adventure.close();
			adventure = null;
		}
		instance = null;
		supportedMCVersions = null;
		pluginLangConfigs = null;
		serverFactory = null;

		// All done.
		getLogger().info("Disabled WorldwideChat version " + getPluginVersion() + ". Goodbye!");
	}
	
	/* Init all commands */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		/* Commands that run regardless of translator settings, but not during restarts */
		if (!translatorName.equals("Starting")) {
            switch (command.getName()) {
				case "wwc":
                    // WWC version
                    final TextComponent versionNotice = Component.text()
                            .content(refs.getMsg("wwcVersion", sender)).color(NamedTextColor.RED)
                            .append((Component.text().content(" " + getPluginVersion())).color(NamedTextColor.LIGHT_PURPLE))
                            .append((Component.text().content(" (Made with love by ")).color(NamedTextColor.GOLD))
                            .append((Component.text().content("Dominic Feliton")).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                            .append((Component.text().content(")").resetStyle()).color(NamedTextColor.GOLD)).build();
                    refs.sendMsg(sender, versionNotice);
                    return true;
				case "wwcr":
                    // Reload command
                    reload(sender);
                    return true;
				case "wwcs":
                    // Stats for translator
                    WWCStats wwcs = new WWCStats(sender, command, label, args);
                    return wwcs.processCommand();
            }
		}
		/* Commands that run if translator settings are valid */
		if (hasValidTranslatorSettings(sender)) {
            switch (command.getName()) {
				case "wwcg":
                    // Global translation
                    WWCTranslate wwcg = new WWCGlobal(sender, command, label, args);
                    return wwcg.processCommand();
				case "wwct":
                    // Per player translation
                    WWCTranslate wwct = new WWCTranslate(sender, command, label, args);
                    return wwct.processCommand();
				case "wwcl":
					// Per player localization
					WWCLocalize wwcl = new WWCLocalize(sender, command, label, args);
					return wwcl.processCommand();
				case "wwcd":
					WWCDebug wwcd = new WWCDebug(sender, command, label, args);
					return wwcd.processCommand();
				case "wwctb":
                    // Book translation
                    WWCTranslateBook wwctb = new WWCTranslateBook(sender, command, label, args);
                    return wwctb.processCommand();
				case "wwcts":
                    // Sign translation
                    WWCTranslateSign wwcts = new WWCTranslateSign(sender, command, label, args);
                    return wwcts.processCommand();
				case "wwcti":
                    // Item translation
                    WWCTranslateItem wwcti = new WWCTranslateItem(sender, command, label, args);
                    return wwcti.processCommand();
				case "wwcte":
                    // Entity translation
                    WWCTranslateEntity wwcte = new WWCTranslateEntity(sender, command, label, args);
                    return wwcte.processCommand();
				case "wwctco":
                    // Outgoing chat translation
                    WWCTranslateChatOutgoing wwctco = new WWCTranslateChatOutgoing(sender, command, label, args);
                    return wwctco.processCommand();
				case "wwctci":
                    // Incoming chat translation
                    WWCTranslateChatIncoming wwctci = new WWCTranslateChatIncoming(sender, command, label, args);
                    return wwctci.processCommand();
		        case "wwctrl":
                    // Rate Limit Command
                    WWCTranslateRateLimit wwctrl = new WWCTranslateRateLimit(sender, command, label, args);
                    return wwctrl.processCommand();
            }
		}
		/* Commands that run regardless of translator settings, but not during restarts or as console */
		/* Keep these commands down here, otherwise checkSenderIdentity() will send a message when we don't want it to */
		if (checkSenderIdentity(sender) && !translatorName.equals("Starting")) {
			switch (command.getName()) {
				case "wwcc":
					// Configuration GUI
					if (inventoryManager == null) {
						refs.debugMsg("invManager is null! We may be on folia, abort...");
						return true;
					}

					WWCConfiguration wwcc = new WWCConfiguration(sender, command, label, args);
					return wwcc.processCommand();
			}
		}
		return true;
	}

	/**
	  * Easy way to reload this plugin; call this method anywhere to reload quickly
	  */
	public void reload() {
		reload(null, false);
	}
	
	/**
	 * Reload this plugin with a null sender but can toggle state
	 * @param saveMainConfig - whether to save the main config or not
	 */
	public void reload(boolean saveMainConfig) {
		reload(null, saveMainConfig);
	}
	
	/**
	 * Reload this plugin with a sender and choose whether to save main config
	 * @param sender - Valid command sender
	 */
	public void reload(CommandSender sender) {
		reload(sender, false);
	}
	
	/**
	  * Reloads the plugin and sends a message to the caller
	  * @param inSender - CommandSender who requested reload, null if none
	  * @param saveMainConfig - whether to save the main config or not
	  * THIS METHOD MUST BE RUN SYNCED TO MAIN THREAD
	  */
	public void reload(CommandSender inSender, boolean saveMainConfig) {
		/* Put plugin into a reloading state */
		// Check if plugin was previously "disabled" or "invalid"
		boolean invalidState = translatorName.equalsIgnoreCase("Invalid");
		refs.debugMsg("Is invalid state???:::" + invalidState);

		if (translatorName.equals("Starting")) {
			refs.debugMsg("Cannot reload while reloading!");
			return;
		}
		translatorName = "Starting";
		refs.closeAllInvs();
		translatorErrorCount = 0;

		/* Send start reload message */
		if (inSender != null) {
			final TextComponent wwcrBegin = Component.text()
							.content(refs.getMsg("wwcrBegin", inSender))
							.color(NamedTextColor.YELLOW)
					.build();
			refs.sendMsg(inSender, wwcrBegin);
		}
		
		/* Once it is safe to, cancelBackgroundTasks and loadPluginConfigs async so we don't stall the main thread */
		BukkitRunnable reload = new BukkitRunnable() {
			@Override
			public void run() {
				final long currentDuration = System.nanoTime();
				/* Cancel background tasks before main config saving */
				// TODO: Add unit test to make sure that storage config changes do not apply until next run
				// TODO: Add unit test to make sure that storage defaults to YAML on conn failure
				// TODO: Add case for if connection gets interrupted?
				if (!currPlatform.equals("Folia")) {
					cancelBackgroundTasks(true, invalidState, this.getTaskId());
				} else {
					cancelBackgroundTasks(true, invalidState);
				}

				/* Save main config on current thread BEFORE actual reload */
				if (saveMainConfig) {
					refs.debugMsg("Saving main config on async thread BEFORE actual reload...");
					getConfigManager().saveMainConfig(false);
				}

				doStartupTasks(true);
				
				/* Send successfully reloaded message */
				if (inSender != null) {
					if ((getConfigManager().getMainConfig().getBoolean("Storage.useSQL") && !isSQLConnValid(true)) ||
									(getConfigManager().getMainConfig().getBoolean("Storage.usePostgreSQL") && !isPostgresConnValid((true))) ||
									(getConfigManager().getMainConfig().getBoolean("Storage.useMongoDB") && !isMongoConnValid(true))) {
						if (!translatorName.equals("Invalid")) {
							final TextComponent wwcrStorageFail = Component.text()
									.content(refs.getMsg("wwcrStorageFail", inSender))
									.color(NamedTextColor.RED)
									.append(Component.text()
											.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
											.color(NamedTextColor.YELLOW))
									.build();
							refs.sendMsg(inSender, wwcrStorageFail);
						} else {
							final TextComponent wwcrStorageTranslatorFail = Component.text()
									.content(refs.getMsg("wwcrStorageTranslatorFail", inSender))
									.color(NamedTextColor.RED)
									.append(Component.text()
											.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
											.color(NamedTextColor.YELLOW))
									.build();
							refs.sendMsg(inSender, wwcrStorageTranslatorFail);
						}
					} else if (translatorName.equals("Invalid")) {
						final TextComponent wwcrTransFail = Component.text()
								.content(refs.getMsg("wwcrTransFail", inSender))
								.color(NamedTextColor.RED)
								.append(Component.text()
										.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
										.color(NamedTextColor.YELLOW))
								.build();
						refs.sendMsg(inSender, wwcrTransFail);
					} else {
						final TextComponent wwcrSuccess = Component.text()
								.content(refs.getMsg("wwcrSuccess", inSender))
								.color(NamedTextColor.GREEN)
								.append(Component.text()
										.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
										.color(NamedTextColor.YELLOW))
								.build();
						refs.sendMsg(inSender, wwcrSuccess);
					}
				}
			}
		};
		wwcHelper.runAsync(reload, ASYNC, null);
	}

	/**
	 * Wait for and cancel background tasks, no taskID to exclude/previously valid translator
	 * @param isReloading - If this function should accommodate for a plugin reload or not
	 */
	public void cancelBackgroundTasks(boolean isReloading) {
		cancelBackgroundTasks(isReloading, false);
	}
	
	/**
	  * Wait for and cancel background tasks, no taskID to exclude
	  * @param isReloading - If this function should accommodate for a plugin reload or not
	  * @param wasPreviouslyInvalid - If we are reloading from an invalid state to begin with
	  */
    public void cancelBackgroundTasks(boolean isReloading, boolean wasPreviouslyInvalid) {
    	cancelBackgroundTasks(isReloading, wasPreviouslyInvalid, -1);
    }
	
    /**
	  * Wait for and cancel background tasks 
	  * @param isReloading - If this function should accommodate for a plugin reload or not
	  * @param wasPreviouslyInvalid - If this plugin was previously in a soft "disabled" state (no functioning translator)
	  * @param taskID - Task to ignore when cancelling tasks, in case this is being ran async
	  */
	public void cancelBackgroundTasks(boolean isReloading, boolean wasPreviouslyInvalid, int taskID) {
		// Do not do any of this if CommonRefs/WWCHelper/ServerFactory are null!
		if (refs == null || wwcHelper == null || serverFactory == null) {
			return;
		}
		refs.debugMsg("Cancel background tasks!");

		// Shut down executors
		callbackExecutor.shutdownNow();

		// Close all inventories
		refs.closeAllInvs();

		// Cleanup background tasks
		wwcHelper.cleanupTasks(taskID);

		// Sync activeTranslators, playerRecords to disk
		try {
			configurationManager.syncData(wasPreviouslyInvalid);
		} catch (Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Disconnect SQL
		if (sqlSession != null) sqlSession.disconnect();
		sqlSession = null;
		
		// Disconnect MongoDB
		if (mongoSession != null) mongoSession.disconnect();
		mongoSession = null;

		// Disconnect Postgres
		if (postgresSession != null) postgresSession.disconnect();
		postgresSession = null;
		
		// Clear all active translating users, cache, playersUsingConfigGUI
		supportedInputLangs.clear();
		supportedOutputLangs.clear();
		playerRecords.clear();
		activeTranslators.clear();
		cache.invalidateAll();
		cache.cleanUp();
	}

	/* Do Startup Tasks */
	/**
	  * Platform-exclusive tasks, such as (re)loading all plugin configs
	  * @param isReloading - If this function should accommodate for a plugin reload or not
	  */
	public void doStartupTasks(boolean isReloading) {
		// Start thread executor
		callbackExecutor = Executors.newCachedThreadPool();

		// Set config manager
		setConfigManager(new ConfigurationHandler());

		// Init and load configs
		configurationManager.initMainConfig();
		configurationManager.initMessagesConfigs();

		configurationManager.loadMainSettings();
		configurationManager.loadStorageSettings();
		// we are storing the real translator name in tempTransName.
		// this is to prevent the plugin from being fully accessible to all users just yet.
		// (we are not done init'ing)
		String tempTransName = configurationManager.loadTranslatorSettings();

		/* Run tasks after translator loaded */
		// Load saved user data
		new LoadUserData(tempTransName).run();

        // Schedule automatic user data sync
		BukkitRunnable sync = new BukkitRunnable() {
			@Override
			public void run() {
				new SyncUserData().run();
			}
		};

		wwcHelper.runAsyncRepeating(true, syncUserDataDelay * 20,  syncUserDataDelay * 20, sync, ASYNC, null);

		// Enable tab completers (we run as a sync task to avoid using Bukkit API async)
		if (isReloading) {
			BukkitRunnable tab = new BukkitRunnable() {
				@Override
				public void run() {
					registerTabCompleters();
				}
			};
			wwcHelper.runSync(tab, GLOBAL, null);
		} else {
			registerTabCompleters();
		}

		// Check for updates
		BukkitRunnable update = new BukkitRunnable() {
			@Override
			public void run() {
				new UpdateChecker().run();
			}
		};

		wwcHelper.runAsyncRepeating(true, 0, updateCheckerDelay * 20, update, ASYNC, null);

		// Finish by setting translator name, which permits plugin usage ("Starting" does not)
		translatorName = tempTransName;
	}

	/**
	 * Initialize adapters and check MC version/platform
	 */
	private boolean checkAndInitAdapters() {
		// Init vars
		Pair<String, String> serverInfo = serverFactory.getServerInfo();
		String type = serverInfo.getKey();
		String version = serverInfo.getValue();

		// Get adapter class name
		String outputVersion = "";
		switch (type) {
			case "Bukkit":
			case "Spigot":
			case "Paper":
			case "Folia":
			// Raw Bukkit (not spigot) is not *explicitly* supported but should work
				getLogger().info("##### Detected supported platform: " + type + " #####");
				break;
			default:
				getLogger().warning("##### You are running an unsupported server platform. Defaulting to Bukkit... #####");
				break;
		}

		for (String eaVer: supportedMCVersions) {
			if (version.contains(eaVer)) {
				outputVersion = eaVer;
				getLogger().info("##### Detected supported MC version: " + outputVersion + " #####");
			}
		}

		// Not running a supported server version, default to latest
		if (outputVersion.isEmpty()) {
			outputVersion = supportedMCVersions[supportedMCVersions.length-1];
			getLogger().warning("##### Unsupported MC version: " + version + ". Defaulting to " + outputVersion + "... #####");
		}

		// If running Folia 1.19/1.18 (?)
		if (type.equals("Folia") && (outputVersion.equals("1.19") || (outputVersion.equals("1.18")))) {
			getLogger().warning("##### Unsupported MC version: " + version + ". Folia detected, disabling... #####");
			return false;
		}

		// Load methods
		currPlatform = type;
		refs = serverFactory.getCommonRefs();
		wwcHelper = serverFactory.getWWCHelper();

		if (refs == null || wwcHelper == null) {
			return false;
		}
		return true;
	}

	/**
	  * Checks a sender if they are a player
	  * @param sender - CommandSender to be checked
	  * @return Boolean - Whether sender is allowed or not
	  */
	private boolean checkSenderIdentity(CommandSender sender) {
		if (!(sender instanceof Player)) {
			final TextComponent consoleNotice = Component.text()
							.content(refs.getMsg("wwcNoConsole", null))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, consoleNotice);
			return false;
		}
		return true;
	}

	/**
	  * Checks if the current translator is active/working
	  * @param sender - CommandSender who requested this check
	  * @return Boolean - True if translator is valid; false otherwise
	  */
	private boolean hasValidTranslatorSettings(CommandSender sender) {
		if (getTranslatorName().equals("Starting")) {
			final TextComponent notDone = Component.text()
							.content("WorldwideChat is still initializing, please try again shortly.")
							.color(NamedTextColor.YELLOW)
					.build();
			refs.sendMsg(sender, notDone);
			return false;
		} else if (getTranslatorName().equals("Invalid")) {
			final TextComponent invalid = Component.text()
							.content(refs.getMsg("wwcInvalidTranslator", sender))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, invalid);
			return false;
		}
		return true;
	}

	/**
	  * Registers tab autocomplete for all valid commands 
	  */
	private void registerTabCompleters() {
		Set<String> myPluginCommands = getDescription().getCommands().keySet();

		for (String commandName : myPluginCommands) {
			getCommand(commandName).setTabCompleter(new WWCTabCompleter());
		}
	}

	/* Setters */
	public void setMongoSession(MongoDBUtils i) {
		mongoSession = i;
	}

	public void setSqlSession(SQLUtils i) {
		sqlSession = i;
	}

	public void setPostgresSession(PostgresUtils i) { postgresSession = i; }

	public void addActiveTranslator(ActiveTranslator i) {
		activeTranslators.put(i.getUUID(), i);
		refs.debugMsg(i.getUUID() + " has been added (or overwrriten) to the internal active translator hashmap.");
	}

	public void removeActiveTranslator(ActiveTranslator i) {
		activeTranslators.remove(i.getUUID());
		refs.debugMsg(i.getUUID() + " has been removed from the internal active translator hashmap.");
	}
	
	public void setInputLangs(Map<String, SupportedLang> in) {
		supportedInputLangs = in;
	}
	
	public void setOutputLangs(Map<String, SupportedLang> in) {
		supportedOutputLangs = in;
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(Player in) {
		if (in == null) {
			return false;
		}
		return isActiveTranslator(in.getUniqueId());
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player UUID
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(UUID in) {
		if (in == null) {
			return false;
		}
		return isActiveTranslator(in.toString());
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player UUID as a String
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(String in) {
		return !getActiveTranslator(in).getUUID().isEmpty();
	}

	public void addPlayerUsingConfigurationGUI(UUID in) {
		if (!playersUsingConfigGUI.contains(in.toString())) {
			playersUsingConfigGUI.add(in.toString());
			refs.debugMsg("UUID " + in
					+ " has been added (or overwrriten) to the internal hashmap of people that are using the configuration GUI.");
			return;
		}
	}
	
	public void addPlayerUsingConfigurationGUI(Player in) {
		addPlayerUsingConfigurationGUI(in.getUniqueId());
	}

	public void removePlayerUsingConfigurationGUI(UUID in) {
		playersUsingConfigGUI.remove(in.toString());
		refs.debugMsg("Player " + in
				+ " has been removed from the internal list of people that are using the configuration GUI.");
	}
	
	public void removePlayerUsingConfigurationGUI(Player p) {
		removePlayerUsingConfigurationGUI(p.getUniqueId());
	}
	
	public void setCacheProperties(int in) {
		cache = Caffeine.newBuilder()
				.maximumSize(in)
				.build();
	}

	public void addCacheTerm(CachedTranslation input, String outputPhrase) {
        // No cache
		if (configurationManager.getMainConfig().getInt("Translator.translatorCacheSize") <= 0) {
			return;
		}
		
		// Cache found, do not add
		if (cache.getIfPresent(input) != null) {
			refs.debugMsg("Term already exists! Not adding.");
			return;
		}
		
		// Exceeds max size...looks like caffeine removes for us!
		long estimatedCacheSize = getEstimatedCacheSize();
		refs.debugMsg("Removed least used phrase in cache if at hard limit. Size after removal test: " + estimatedCacheSize);
		
		// Put phrase after (potential) removal
		cache.put(input, outputPhrase);
		refs.debugMsg("Added new phrase into cache! Size after addition: ");
	}

	public void removeCacheTerm(CachedTranslation i) {
		cache.cleanUp();
		cache.invalidate(i);
	}

	public void addPlayerRecord(PlayerRecord i) {
		playerRecords.put(i.getUUID(), i);
	}

	public void removePlayerRecord(PlayerRecord i) {
		playerRecords.remove(i.getUUID());
		refs.debugMsg("Removed player record of " + i.getUUID() + ".");
	}
	
	/**
	 * Checks if a given player has a player record.
	 * @param in - A player
	 * @return true if PlayerRecord, false otherwise
	 */
	public boolean isPlayerRecord(Player in) {
		return isPlayerRecord(in.getUniqueId());
	}
	
	/**
	 * Checks if a given name has a player record.
	 * @param in - A player UUID
	 * @return true if PlayerRecord, false otherwise
	 */
	public boolean isPlayerRecord(UUID in) {
		return isPlayerRecord(in.toString());
	}
	
	/**
	 * Checks if a given name has a player record.
	 * @param in - A player UUID as a String
	 * @return true if PlayerRecord, false otherwise
	 */
	public boolean isPlayerRecord(String in) {
		return !getPlayerRecord(in, false).getUUID().isEmpty();
	}

	public void setOutOfDate(boolean i) {
		outOfDate = i;
	}
	
	public void setPrefixName(String i) {
		if (!i.equalsIgnoreCase("WWC")) {
			pluginPrefix = LegacyComponentSerializer.legacyAmpersand().deserialize(i);
		} else {
			pluginPrefix = Component.text().content("[").color(NamedTextColor.DARK_RED)
					.append(Component.text().content("WWC").color(NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
					.append(Component.text().content("]").color(NamedTextColor.DARK_RED))
					.build();
		}
	}

	public void setTranslatorName(String i) {
		translatorName = i;
	}
	
	public void setTranslatorErrorCount(int i) {
		translatorErrorCount = i;
	}

	public void setUpdateCheckerDelay(int i) {
		updateCheckerDelay = i;
	}

	public void setSyncUserDataDelay(int i) {
		syncUserDataDelay = i;
	}

	public void setGlobalRateLimit(int i) {
		globalRateLimit = i;
	}

	public void setMessageCharLimit(int i) {
		messageCharLimit = i;
	}

	public void setPersistentCache(boolean i) {
		persistentCache = i;
	}

	public void setErrorLimit(int i) {
		errorLimit = i;
	}

	public void setErrorsToIgnore(ArrayList<String> in) {
		errorsToIgnore = in;
	}
	
	/* Getters */
	public ServerAdapterFactory getServerFactory() { return serverFactory; }

	public boolean isMongoConnValid(boolean quiet) {
		if (mongoSession == null || !mongoSession.isConnected()) {
			if (!quiet) {
				getLogger().warning(refs.getMsg("wwcInvalidStorageSession", "MongoDB", null));
			}
			return false;
		}
		return true;
	}

	public MongoDBUtils getMongoSession() {
		return mongoSession;
	}

	public boolean isSQLConnValid(boolean quiet) {
		if (sqlSession == null || !sqlSession.isConnected()) {
			if (!quiet) {
				getLogger().warning(refs.getMsg("wwcInvalidStorageSession", "SQL", null));
			}
			return false;
		}
		return true;
	}

	public SQLUtils getSqlSession() {
		return sqlSession;
	}

	public boolean isPostgresConnValid(boolean quiet) {
		if (postgresSession == null || !postgresSession.isConnected()) {
			if (!quiet) {
				getLogger().warning(refs.getMsg("wwcInvalidStorageSession", "Postgres", null));
			}
			return false;
		}
		return true;
	}

	public PostgresUtils getPostgresSession() {
		return postgresSession;
	}

	public ActiveTranslator getActiveTranslator(Player in) {
		return getActiveTranslator(in.getUniqueId());
	}
	
	public ActiveTranslator getActiveTranslator(UUID uuid) {
		return getActiveTranslator(uuid.toString());
	}
	
	public ActiveTranslator getActiveTranslator(String uuid) {
		ActiveTranslator outTranslator = activeTranslators.get(uuid);
		if (outTranslator != null) {
			return outTranslator;
		}
		return new ActiveTranslator("", "", "");
	}
	
	public String getCacheTerm(CachedTranslation in) {
		if (configurationManager.getMainConfig().getInt("Translator.translatorCacheSize") <= 0) 
			return null;
		
		String out = cache.getIfPresent(in);
		refs.debugMsg("Cache lookup outcome: " + out);
		
		return out;
	}

	public boolean hasCacheTerm(CachedTranslation in) {
		if (configurationManager.getMainConfig().getInt("Translator.translatorCacheSize") <= 0)
			return false;

		return cache.getIfPresent(in) != null;
	}
	
	public long getEstimatedCacheSize() {
		cache.cleanUp();
		return cache.estimatedSize();
	}

	public PlayerRecord getPlayerRecord(Player inPlayer, boolean createNewIfNotExisting) {
		return getPlayerRecord(inPlayer.getUniqueId().toString(), createNewIfNotExisting);
	}

	public PlayerRecord getPlayerRecord(String uuid, boolean createNewIfNotExisting) {
		PlayerRecord outRecord = playerRecords.get(uuid);
		if (outRecord != null) {
			return outRecord;
		}
		if (createNewIfNotExisting) {
			// Create + add new record
			PlayerRecord newRecord = new PlayerRecord("--------", uuid, 0, 0);
			addPlayerRecord(newRecord);
			return newRecord;
		}
		return new PlayerRecord("", "", -1, -1);
	}
	
	public boolean isPlayerUsingGUI(Player player) { return playersUsingConfigGUI.contains(player.getUniqueId().toString()); }

	public Map<String, ActiveTranslator> getActiveTranslators() {
		return activeTranslators;
	}

	public Cache<CachedTranslation, String> getCache() {
		return cache;
	}

	public Map<String, PlayerRecord> getPlayerRecords() {
		return playerRecords;
	}

	public List<String> getPlayersUsingGUI() {
		return playersUsingConfigGUI;
	}
	
	public Map<String, SupportedLang> getSupportedInputLangs() {
		return supportedInputLangs;
	}
	
	public Map<String, SupportedLang> getSupportedOutputLangs() {
		return supportedOutputLangs;
	}

	public TextComponent getPluginPrefix() {
		return pluginPrefix;
	}

	public boolean getOutOfDate() {
		return outOfDate;
	}
	
	public int getTranslatorErrorCount() {
		return translatorErrorCount;
	}
	
	public String getTranslatorName() {
		return translatorName;
	}

	public String getPluginVersion() { return this.getDescription().getVersion(); }

	public String getCurrPlatform() { return currPlatform; }

	public int getUpdateCheckerDelay() {
		return updateCheckerDelay;
	}

	public int getSyncUserDataDelay() {
		return syncUserDataDelay;
	}

	public int getGlobalRateLimit() {
		return globalRateLimit;
	}

	public int getMessageCharLimit() {
		return messageCharLimit;
	}

	public boolean isPersistentCache() {
		return persistentCache;
	}

	public int getErrorLimit() {
		return errorLimit;
	}

	public ArrayList<String> getErrorsToIgnore() {
		return errorsToIgnore;
	}
}