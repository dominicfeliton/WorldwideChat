package com.badskater0729.worldwidechat;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import com.badskater0729.worldwidechat.commands.WWCConfiguration;
import com.badskater0729.worldwidechat.commands.WWCGlobal;
import com.badskater0729.worldwidechat.commands.WWCStats;
import com.badskater0729.worldwidechat.commands.WWCTranslate;
import com.badskater0729.worldwidechat.commands.WWCTranslateBook;
import com.badskater0729.worldwidechat.commands.WWCTranslateChatIncoming;
import com.badskater0729.worldwidechat.commands.WWCTranslateChatOutgoing;
import com.badskater0729.worldwidechat.commands.WWCTranslateEntity;
import com.badskater0729.worldwidechat.commands.WWCTranslateItem;
import com.badskater0729.worldwidechat.commands.WWCTranslateRateLimit;
import com.badskater0729.worldwidechat.commands.WWCTranslateSign;
import com.badskater0729.worldwidechat.configuration.ConfigurationHandler;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui;
import com.badskater0729.worldwidechat.listeners.ChatListener;
import com.badskater0729.worldwidechat.listeners.InventoryListener;
import com.badskater0729.worldwidechat.listeners.OnPlayerJoinListener;
import com.badskater0729.worldwidechat.listeners.TranslateInGameListener;
import com.badskater0729.worldwidechat.listeners.WWCTabCompleter;
import com.badskater0729.worldwidechat.runnables.LoadUserData;
import com.badskater0729.worldwidechat.runnables.SyncUserData;
import com.badskater0729.worldwidechat.runnables.UpdateChecker;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CachedTranslation;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.badskater0729.worldwidechat.util.storage.MongoDBUtils;
import com.badskater0729.worldwidechat.util.storage.SQLUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.closeAllInvs;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsync;
import static com.badskater0729.worldwidechat.util.CommonRefs.runAsyncRepeating;
import static com.badskater0729.worldwidechat.util.CommonRefs.runSync;
import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;
import static com.badskater0729.worldwidechat.util.CommonRefs.supportedMCVersions;
import static com.badskater0729.worldwidechat.util.CommonRefs.getServerInfo;

public class WorldwideChat extends JavaPlugin {
	public static int translatorFatalAbortSeconds = 10;
	public static int translatorConnectionTimeoutSeconds = translatorFatalAbortSeconds - 2;
	public static int asyncTasksTimeoutSeconds = translatorConnectionTimeoutSeconds - 2;
	public static final int bStatsID = 10562;
	public static final String messagesConfigVersion = "09082023-2"; // MMDDYYYY-revisionNumber

	public static WorldwideChat instance;
	
	private BukkitAudiences adventure;
	private InventoryManager inventoryManager;
	private ConfigurationHandler configurationManager;
	
	private List<SupportedLang> supportedInputLangs = new CopyOnWriteArrayList<>();
	private List<SupportedLang> supportedOutputLangs = new CopyOnWriteArrayList<>();
	private final List<String> playersUsingConfigGUI = new CopyOnWriteArrayList<>();
	private final Map<String, PlayerRecord> playerRecords = new ConcurrentHashMap<>();
	private final Map<String, ActiveTranslator> activeTranslators = new ConcurrentHashMap<>();

	private Cache<CachedTranslation, String> cache = Caffeine.newBuilder()
			.maximumSize(100)
			.build();
	
	private int translatorErrorCount = 0;
	
	private boolean outOfDate = false;

	private volatile String translatorName = "Starting";

	private TextComponent pluginPrefix = Component.text().content("[").color(NamedTextColor.DARK_RED)
			.append(Component.text().content("WWC").color(NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
			.append(Component.text().content("]").color(NamedTextColor.DARK_RED))
			.build();
	
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
	
	public InventoryManager getInventoryManager() {
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
		adventure = BukkitAudiences.create(this); // Adventure
		inventoryManager = new WWCInventoryManager(this); // InventoryManager for SmartInvs API
		inventoryManager.init(); // Init InventoryManager

		// Load plugin configs, check if they successfully initialized
		loadPluginConfigs(false);

		// Check current server version
		checkMCVersion();

		// EventHandlers + check for plugins
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new OnPlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new TranslateInGameListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getLogger().info(ChatColor.LIGHT_PURPLE
				+ getMsg("wwcListenersInitialized"));

		// We made it!
		debugMsg("Async tasks running: " + this.getActiveAsyncTasks());
		getLogger().info(ChatColor.GREEN + getMsg("wwcEnabled", getPluginVersion()));
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
		supportedPluginLangCodes = null;

		// All done.
		getLogger().info("Disabled WorldwideChat version " + getPluginVersion() + ". Goodbye!");
	}
	
	/* Init all commands */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		/* Commands that run regardless of translator settings, but not during restarts */
		if (!translatorName.equals("Starting")) {
            switch (command.getName()) {
                case "wwc" -> {
                    // WWC version
                    final TextComponent versionNotice = Component.text()
                            .content(getMsg("wwcVersion")).color(NamedTextColor.RED)
                            .append((Component.text().content(" " + getPluginVersion())).color(NamedTextColor.LIGHT_PURPLE))
                            .append((Component.text().content(" (Made with love by ")).color(NamedTextColor.GOLD))
                            .append((Component.text().content("BadSkater0729")).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                            .append((Component.text().content(")").resetStyle()).color(NamedTextColor.GOLD)).build();
                    sendMsg(sender, versionNotice);
                    return true;
                }
                case "wwcr" -> {
                    // Reload command
                    reload(sender);
					// TODO: Send diff message if translator fails to player
                    return true;
                }
                case "wwcs" -> {
                    // Stats for translator
                    WWCStats wwcs = new WWCStats(sender, command, label, args);
                    return wwcs.processCommand();
                }
            }
		}
		/* Commands that run if translator settings are valid */
		if (hasValidTranslatorSettings(sender)) {
            switch (command.getName()) {
                case "wwcg" -> {
                    // Global translation
                    WWCTranslate wwcg = new WWCGlobal(sender, command, label, args);
                    return wwcg.processCommand();
                }
                case "wwct" -> {
                    // Per player translation
                    WWCTranslate wwct = new WWCTranslate(sender, command, label, args);
                    return wwct.processCommand();
                }
                case "wwctb" -> {
                    // Book translation
                    WWCTranslateBook wwctb = new WWCTranslateBook(sender, command, label, args);
                    return wwctb.processCommand();
                }
                case "wwcts" -> {
                    // Sign translation
                    WWCTranslateSign wwcts = new WWCTranslateSign(sender, command, label, args);
                    return wwcts.processCommand();
                }
                case "wwcti" -> {
                    // Item translation
                    WWCTranslateItem wwcti = new WWCTranslateItem(sender, command, label, args);
                    return wwcti.processCommand();
                }
                case "wwcte" -> {
                    // Entity translation
                    WWCTranslateEntity wwcte = new WWCTranslateEntity(sender, command, label, args);
                    return wwcte.processCommand();
                }
                case "wwctco" -> {
                    // Outgoing chat translation
                    WWCTranslateChatOutgoing wwctco = new WWCTranslateChatOutgoing(sender, command, label, args);
                    return wwctco.processCommand();
                }
                case "wwctci" -> {
                    // Incoming chat translation
                    WWCTranslateChatIncoming wwctci = new WWCTranslateChatIncoming(sender, command, label, args);
                    return wwctci.processCommand();
                }
                case "wwctrl" -> {
                    // Rate Limit Command
                    WWCTranslateRateLimit wwctrl = new WWCTranslateRateLimit(sender, command, label, args);
                    return wwctrl.processCommand();
                }
            }
		}
		/* Commands that run regardless of translator settings, but not during restarts or as console */
		/* Keep these commands down here, otherwise checkSenderIdentity() will send a message when we don't want it to */
		if (checkSenderIdentity(sender) && !translatorName.equals("Starting")) {
			switch (command.getName()) {
				case "wwcc" -> {
					// Configuration GUI
					WWCConfiguration wwcc = new WWCConfiguration(sender, command, label, args);
					return wwcc.processCommand();
				}
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
		debugMsg("Is invalid state???:::" + invalidState);

		if (translatorName.equals("Starting")) {
			debugMsg("Cannot reload while reloading!");
			return;
		}
		translatorName = "Starting";
		closeAllInvs();
		translatorErrorCount = 0;

		/* Save main config on current thread */
		if (saveMainConfig) {
			getConfigManager().saveMainConfig(false);
		}

		/* Send start reload message */
		if (inSender != null) {
			final TextComponent wwcrBegin = Component.text()
							.content(getMsg("wwcrBegin"))
							.color(NamedTextColor.YELLOW)
					.build();
			sendMsg(inSender, wwcrBegin);
		}
		
		/* Once it is safe to, cancelBackgroundTasks and loadPluginConfigs async so we don't stall the main thread */
		BukkitRunnable reload = new BukkitRunnable() {
			@Override
			public void run() {
				final long currentDuration = System.nanoTime();
				cancelBackgroundTasks(true, invalidState, this.getTaskId());
				loadPluginConfigs(true);
				
				/* Send successfully reloaded message */
				if (inSender != null) {
					final TextComponent wwcrSuccess = Component.text()
									.content(getMsg("wwcrSuccess"))
									.color(NamedTextColor.GREEN)
							.append(Component.text()
									.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
									.color(NamedTextColor.YELLOW))
							.build();
					sendMsg(inSender, wwcrSuccess);
				}
			}
		};
		runAsync(reload);
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
	  * @param taskID - Task to ignore when cancelling tasks, in case this is being ran async
	  */
	public void cancelBackgroundTasks(boolean isReloading, boolean wasPreviouslyInvalid, int taskID) {
		// Wait for completion + kill all background tasks
		// Thanks to:
		// https://gist.github.com/blablubbabc/e884c114484f34cae316c48290b21d8e#file-someplugin-java-L37
		if (!translatorName.equals("JUnit/MockBukkit Testing Translator")) {
			final long asyncTasksTimeoutMillis = (long) asyncTasksTimeoutSeconds * 1000;
			final long asyncTasksStart = System.currentTimeMillis();
			boolean asyncTasksTimeout = false;
			while (this.getActiveAsyncTasks(taskID) > 0) {
				// Send interrupt signal
				try {
					for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
						if (worker.getOwner().equals(this) && worker.getTaskId() != taskID) {
							debugMsg("Sending interrupt to task with ID " + worker.getTaskId() + "...");
							worker.getThread().interrupt();
						}
					}
					// TODO: Replace with poll instead of sleep (somehow?)
					Thread.sleep(100);
				} catch (InterruptedException e) {
					debugMsg("Thread successfully aborted and threw an InterruptedException.");
				}

				// Disable once we reach timeout
				if (System.currentTimeMillis() - asyncTasksStart > asyncTasksTimeoutMillis) {
					asyncTasksTimeout = true;
					debugMsg(
							"Waited " + asyncTasksTimeoutSeconds + " seconds for " + this.getActiveAsyncTasks()
									+ " remaining async tasks to complete. Disabling/reloading regardless...");
					break;
				}
			}
			final long asyncTasksTimeWaited = System.currentTimeMillis() - asyncTasksStart;
			if (!asyncTasksTimeout && asyncTasksTimeWaited > 1) {
				debugMsg("Waited " + asyncTasksTimeWaited + " ms for async tasks to finish.");
			}
		}
		
		// Close all inventories
		if (!isReloading) closeAllInvs();

		// Cancel + remove all tasks
		this.getServer().getScheduler().cancelTasks(this);

		// Sync activeTranslators, playerRecords to disk
		configurationManager.syncData(wasPreviouslyInvalid);

		// Disconnect SQL
		SQLUtils.disconnect();
		
		// Disconnect MongoDB
		MongoDBUtils.disconnect();
		
		// Clear all active translating users, cache, playersUsingConfigGUI
		supportedInputLangs.clear();
		supportedOutputLangs.clear();
		playerRecords.clear();
		activeTranslators.clear();
		cache.invalidateAll();
		cache.cleanUp();
	}

	/* Load Plugin Configs */
	/**
	  * (Re)load all plugin configs 
	  * @param isReloading - If this function should accommodate for a plugin reload or not
	  */
	public void loadPluginConfigs(boolean isReloading) {
		setConfigManager(new ConfigurationHandler());

		// Init and load configs
		configurationManager.initMainConfig();
		configurationManager.initMessagesConfig();
		configurationManager.loadMainSettings();
		configurationManager.loadStorageSettings();
		configurationManager.loadTranslatorSettings();

		/* Run tasks after translator loaded */
		// Pre-generate hard coded Config UIs
		MenuGui.genAllConfigUIs();
		
		// Check for updates
		BukkitRunnable update = new BukkitRunnable() {
			@Override
			public void run() {
				new UpdateChecker().run();
			}
		};
		runAsyncRepeating(true, 0, configurationManager.getMainConfig().getInt("General.updateCheckerDelay") * 20, update);

		// Schedule automatic user data sync
		BukkitRunnable sync = new BukkitRunnable() {
			@Override
			public void run() {
				new SyncUserData().run();
			}
		};
        runAsyncRepeating(true, configurationManager.getMainConfig().getInt("General.syncUserDataDelay") * 20,  configurationManager.getMainConfig().getInt("General.syncUserDataDelay") * 20, sync);
			
		// Load saved user data
		BukkitRunnable loadUserData = new BukkitRunnable() {
			@Override
			public void run() {
				new LoadUserData().run();
			}
		};
		runSync(loadUserData);

		// Enable tab completers
		if (isReloading) {
			BukkitRunnable tab = new BukkitRunnable() {
				@Override
				public void run() {
					registerTabCompleters();
				}
			};
			runSync(tab);
		} else {
			registerTabCompleters();
		}
	}
	
	/**
	  * Get active asynchronous tasks 
	  * @return int - Number of active async tasks
	  */
	private int getActiveAsyncTasks() {
		return getActiveAsyncTasks(-1);
	}
	
	/**
	  * Get active asynchronous tasks (excluding a provided one)
	  * @param excludedId - Task ID to be excluded from this count
	  * @return int - Number of active async tasks, excluding excludedId
	  */
	private int getActiveAsyncTasks(int excludedId) {
		int workers = 0;
		if (!translatorName.equals("JUnit/MockBukkit Testing Translator")) {
			for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
				if (worker.getOwner().equals(this) && worker.getTaskId() != excludedId) {
					workers++;
				}
			}
		}
		return workers;
	}

	/**
	 * Check server type/version, print to console if unsupported
	 */
	private void checkMCVersion() {
		/* MC Version check */
		// TODO
		Pair<String, String> serverInfo = getServerInfo();
		String type = serverInfo.getKey();
		String version = serverInfo.getValue();
		debugMsg("Server type: " + type);
		debugMsg("Server version: " + version);
		if (type.equals("Bukkit") || type.equals("Spigot") || type.equals("Paper")) {
			// TODO
			getLogger().info("##### " + getMsg("wwcSupportedServer", type) + " #####");
			for (String eaVer: supportedMCVersions) {
				if (version.contains(eaVer)) {
					getLogger().info("##### " + getMsg("wwcSupportedVersion", eaVer) + " #####");
					return;
				}
			}
			getLogger().warning("##### " + getMsg("wwcUnsupportedVersion") + " #####");
			getLogger().warning("##### " + Arrays.toString(supportedMCVersions) + " #####");
			return;
		}

		// Not running a supported server type, default to Bukkit
		getLogger().warning("##### " + getMsg("wwcUnsupportedServer") + " #####");
	}




		/**
          * Checks a sender if they are a player
          * @param sender - CommandSender to be checked
          * @return Boolean - Whether sender is allowed or not
          */
	private boolean checkSenderIdentity(CommandSender sender) {
		if (!(sender instanceof Player)) {
			final TextComponent consoleNotice = Component.text()
							.content(getMsg("wwcNoConsole"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(sender, consoleNotice);
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
			sendMsg(sender, notDone);
			return false;
		} else if (getTranslatorName().equals("Invalid")) {
			final TextComponent invalid = Component.text()
							.content(getMsg("wwcInvalidTranslator"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(sender, invalid);
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
	public void addActiveTranslator(ActiveTranslator i) {
		activeTranslators.put(i.getUUID(), i);
		debugMsg(i.getUUID() + " has been added (or overwrriten) to the internal active translator hashmap.");
	}

	public void removeActiveTranslator(ActiveTranslator i) {
		activeTranslators.remove(i.getUUID());
		debugMsg(i.getUUID() + " has been removed from the internal active translator hashmap.");
	}
	
	public void setInputLangs(List<SupportedLang> in) {
		supportedInputLangs = in;
	}
	
	public void setOutputLangs(List<SupportedLang> in) {
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
			debugMsg("UUID " + in
					+ " has been added (or overwrriten) to the internal hashmap of people that are using the configuration GUI.");
			return;
		}
	}
	
	public void addPlayerUsingConfigurationGUI(Player in) {
		addPlayerUsingConfigurationGUI(in.getUniqueId());
	}

	public void removePlayerUsingConfigurationGUI(UUID in) {
		playersUsingConfigGUI.remove(in.toString());
		debugMsg("Player " + in
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
		// TODO: Ensure that cache size changes take effect

        // No cache
		if (configurationManager.getMainConfig().getInt("Translator.translatorCacheSize") <= 0) {
			return;
		}
		
		// Cache found, do not add
		if (cache.getIfPresent(input) != null) {
			debugMsg("Term already exists! Not adding.");
			return;
		}
		
		// Exceeds max size
		long estimatedCacheSize = getEstimatedCacheSize();
		debugMsg("Removed least used phrase in cache if at hard limit. Size after removal test: " + estimatedCacheSize);
		
		// Put phrase after (potential) removal
		cache.put(input, outputPhrase);
		debugMsg("Added new phrase into cache! Size after addition: ");
	}

	public void removeCacheTerm(CachedTranslation i) {
		// TODO: Make sure cache term is removed
		cache.cleanUp();
		cache.invalidate(i);
	}

	public void addPlayerRecord(PlayerRecord i) {
		playerRecords.put(i.getUUID(), i);
	}

	public void removePlayerRecord(PlayerRecord i) {
		playerRecords.remove(i.getUUID());
		debugMsg("Removed player record of " + i.getUUID() + ".");
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
	
	/* Getters */
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
		debugMsg("Cache lookup outcome: " + out);
		
		return out;
	}
	
	public long getEstimatedCacheSize() {
		cache.cleanUp();
		return cache.estimatedSize();
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
	
	public boolean isPlayerUsingGUI(String uuid) { return playersUsingConfigGUI.contains(uuid); }

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
	
	public List<SupportedLang> getSupportedInputLangs() {
		return supportedInputLangs;
	}
	
	public List<SupportedLang> getSupportedOutputLangs() {
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
}