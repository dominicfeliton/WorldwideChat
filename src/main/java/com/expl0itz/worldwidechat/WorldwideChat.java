package com.expl0itz.worldwidechat;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

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

import com.expl0itz.worldwidechat.commands.WWCConfiguration;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslateRateLimit;
import com.expl0itz.worldwidechat.commands.WWCStats;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateChatIncoming;
import com.expl0itz.worldwidechat.commands.WWCTranslateChatOutgoing;
import com.expl0itz.worldwidechat.commands.WWCTranslateEntity;
import com.expl0itz.worldwidechat.commands.WWCTranslateItem;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.configuration.ConfigurationHandler;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui;
import com.expl0itz.worldwidechat.listeners.ChatListener;
import com.expl0itz.worldwidechat.listeners.InventoryListener;
import com.expl0itz.worldwidechat.listeners.OnPlayerJoinListener;
import com.expl0itz.worldwidechat.listeners.TranslateInGameListener;
import com.expl0itz.worldwidechat.listeners.WWCTabCompleter;
import com.expl0itz.worldwidechat.runnables.LoadUserData;
import com.expl0itz.worldwidechat.runnables.SyncUserData;
import com.expl0itz.worldwidechat.runnables.UpdateChecker;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CachedTranslation;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.PlayerRecord;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;
import com.expl0itz.worldwidechat.util.storage.MongoDBUtils;
import com.expl0itz.worldwidechat.util.storage.SQLUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fr.minuskube.inv.InventoryManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class WorldwideChat extends JavaPlugin {
	public static int translatorFatalAbortSeconds = 10;
	public static final int bStatsID = 10562;
	public static final int translatorConnectionTimeoutSeconds = translatorFatalAbortSeconds - 2;
	public static final int asyncTasksTimeoutSeconds = translatorConnectionTimeoutSeconds - 2;
	
	public static WorldwideChat instance;
	
	private BukkitAudiences adventure;
	private InventoryManager inventoryManager;
	private ConfigurationHandler configurationManager;
	
	private List<SupportedLanguageObject> supportedLanguages = new CopyOnWriteArrayList<SupportedLanguageObject>();
	private List<String> playersUsingConfigurationGUI = new CopyOnWriteArrayList<String>();
	
	private Cache<CachedTranslation, String> cache = Caffeine.newBuilder()
			.maximumSize(100)
			.build();
	private Map<String, PlayerRecord> playerRecords = new ConcurrentHashMap<String, PlayerRecord>();
	private Map<String, ActiveTranslator> activeTranslators = new ConcurrentHashMap<String, ActiveTranslator>();
	
	private int translatorErrorCount = 0;
	
	private boolean outOfDate = false;
	
	private String pluginVersion = this.getDescription().getVersion();
	private String currentMessagesConfigVersion = "07102022-3"; // MMDDYYYY-revisionNumber
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
				+ CommonDefinitions.getMessage("wwcListenersInitialized"));

		// We made it!
		CommonDefinitions.sendDebugMessage("Async tasks running: " + this.getActiveAsyncTasks());
		getLogger().info(ChatColor.GREEN + CommonDefinitions.getMessage("wwcEnabled", new String[] {pluginVersion}));
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
		CommonDefinitions.supportedMCVersions = null;
		CommonDefinitions.supportedPluginLangCodes = null;
		translatorFatalAbortSeconds = 0;

		// All done.
		getLogger().info("Disabled WorldwideChat version " + pluginVersion + ". Goodbye!");
	}
	
	/* Init all commands */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/* Commands that run regardless of translator settings, but not during restarts */
		if (!translatorName.equals("Starting")) {
			switch (command.getName()) {
			case "wwc": 
				// WWC version
				final TextComponent versionNotice = Component.text()
						.append((Component.text().content(CommonDefinitions.getMessage("wwcVersion")).color(NamedTextColor.RED))
						.append((Component.text().content(" " + pluginVersion)).color(NamedTextColor.LIGHT_PURPLE))
						.append((Component.text().content(" (Made with love by ")).color(NamedTextColor.GOLD))
						.append((Component.text().content("3xpl0itz")).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
						.append((Component.text().content(")").resetStyle()).color(NamedTextColor.GOLD))).build();
				CommonDefinitions.sendMessage(sender, versionNotice);
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
		/* Commands that run regardless of translator settings, but not during restarts or as console */
		if (checkSenderIdentity(sender) && !translatorName.equals("Starting")) {
			switch (command.getName()) {
			case "wwcc":
				// Configuration GUI
				WWCConfiguration wwcc = new WWCConfiguration(sender, command, label, args);
				return wwcc.processCommand();
			}
		/* Commands that run if translator settings are valid */
		}
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
	 * @param invalidState - whether there was a previously functional translator or not
	 */
	public void reload(boolean invalidState) {
		reload(null, invalidState);
	}
	
	/**
	 * Reload this plugin with a sender and pre-defined state
	 * @param sender - Valid command sender
	 */
	public void reload(CommandSender sender) {
		reload(sender, translatorName.equalsIgnoreCase("Invalid"));
	}
	
	/**
	  * Reloads the plugin and sends a message to the caller
	  * inSender - CommandSender who requested reload
	  */
	public void reload(CommandSender inSender, boolean invalidState) {
		/* Put plugin into a reloading state */
		//TODO: Reloading in an invalid state deletes all saved user data? Is this still the case?
		CommonDefinitions.sendDebugMessage("Is invalid state???:::" + invalidState);
		translatorName = "Starting";
		CommonDefinitions.closeAllInventories();
		translatorErrorCount = 0;
		
		/* Send start reload message */
		if (inSender != null) {
			final TextComponent wwcrBegin = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcrBegin"))
							.color(NamedTextColor.YELLOW))
					.build();
			CommonDefinitions.sendMessage(inSender, wwcrBegin);
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
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwcrSuccess"))
									.color(NamedTextColor.GREEN))
							.append(Component.text()
									.content(" (" + TimeUnit.MILLISECONDS.convert((System.nanoTime() - currentDuration), TimeUnit.NANOSECONDS) + "ms)")
									.color(NamedTextColor.YELLOW))
							.build();
					CommonDefinitions.sendMessage(inSender, wwcrSuccess);
				}
			}
		};
		CommonDefinitions.scheduleTaskAsynchronously(reload);
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
							CommonDefinitions.sendDebugMessage("Sending interrupt to task with ID " + worker.getTaskId() + "...");
							worker.getThread().interrupt();
						}
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					CommonDefinitions.sendDebugMessage("Thread successfully aborted and threw an InterruptedException.");
				}

				// Disable once we reach timeout
				if (System.currentTimeMillis() - asyncTasksStart > asyncTasksTimeoutMillis) {
					asyncTasksTimeout = true;
					CommonDefinitions.sendDebugMessage(
							"Waited " + asyncTasksTimeoutSeconds + " seconds for " + this.getActiveAsyncTasks()
									+ " remaining async tasks to complete. Disabling/reloading regardless...");
					break;
				}
			}
			final long asyncTasksTimeWaited = System.currentTimeMillis() - asyncTasksStart;
			if (!asyncTasksTimeout && asyncTasksTimeWaited > 1) {
				CommonDefinitions.sendDebugMessage("Waited " + asyncTasksTimeWaited + " ms for async tasks to finish.");
			}
		}
		
		// Close all inventories
		if (!isReloading) CommonDefinitions.closeAllInventories();

		// Cancel + remove all tasks
		this.getServer().getScheduler().cancelTasks(this);

		// Sync activeTranslators, playerRecords to disk
		configurationManager.syncData(wasPreviouslyInvalid);

		// Disconnect SQL
		SQLUtils.disconnect();
		
		// Disconnect MongoDB
		MongoDBUtils.disconnect();
		
		// Clear all active translating users, cache, playersUsingConfigGUI
		supportedLanguages.clear();
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
		CommonDefinitions.scheduleTaskAsynchronouslyRepeating(true, 0, configurationManager.getMainConfig().getInt("General.updateCheckerDelay") * 20, update);

		// Schedule automatic user data sync
		BukkitRunnable sync = new BukkitRunnable() {
			@Override
			public void run() {
				new SyncUserData().run();
			}
		};
        CommonDefinitions.scheduleTaskAsynchronouslyRepeating(true, configurationManager.getMainConfig().getInt("General.syncUserDataDelay") * 20,  configurationManager.getMainConfig().getInt("General.syncUserDataDelay") * 20, sync);
			
		// Load saved user data
		BukkitRunnable loadUserData = new BukkitRunnable() {
			@Override
			public void run() {
				new LoadUserData().run();
			}
		};
		CommonDefinitions.scheduleTask(loadUserData);

		// Enable tab completers
		if (isReloading) {
			BukkitRunnable tab = new BukkitRunnable() {
				@Override
				public void run() {
					registerTabCompleters();
				}
			};
			CommonDefinitions.scheduleTask(tab);
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
	  * Check MC version, print to console if unsupported 
	  */
	private void checkMCVersion() {
		/* MC Version check */
		String supportedVersions = "";
		for (int i = 0; i < CommonDefinitions.supportedMCVersions.length; i++) {
			if (i+1 != CommonDefinitions.supportedMCVersions.length) {
				supportedVersions += CommonDefinitions.supportedMCVersions[i] + ", ";
			} else {
				supportedVersions += CommonDefinitions.supportedMCVersions[i];
			}
			if (Bukkit.getVersion().contains(CommonDefinitions.supportedMCVersions[i])) {
				return;
			}
		}
		// Not running a supported version of Bukkit, Spigot, or Paper
		getLogger().warning(CommonDefinitions.getMessage("wwcUnsupportedVersion"));
		getLogger().warning(supportedVersions);
	}

	/**
	  * Checks a sender if they are console
	  * @param sender - CommandSender to be checked
	  * @return Boolean - Whether sender is allowed or not
	  */
	private boolean checkSenderIdentity(CommandSender sender) {
		if (!(sender instanceof Player)) {
			final TextComponent consoleNotice = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcNoConsole"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, consoleNotice);
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
					.append(Component.text()
							.content("WorldwideChat is still initializing, please try again shortly.")
							.color(NamedTextColor.YELLOW))
					.build();
			CommonDefinitions.sendMessage(sender, notDone);
			return false;
		} else if (getTranslatorName().equals("Invalid")) {
			final TextComponent invalid = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcInvalidTranslator"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalid);
			return false;
		}
		return true;
	}

	/**
	  * Registers tab autocomplete for all valid commands 
	  */
	private void registerTabCompleters() {
		getCommand("wwcg").setTabCompleter(new WWCTabCompleter());
		getCommand("wwct").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcti").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctb").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcte").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctco").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctci").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcts").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcs").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctrl").setTabCompleter(new WWCTabCompleter());
		getCommand("wwc").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcr").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcc").setTabCompleter(new WWCTabCompleter());
	}

	/* Setters */
	public void addActiveTranslator(ActiveTranslator i) {
		activeTranslators.put(i.getUUID(), i);
		CommonDefinitions.sendDebugMessage(i.getUUID() + " has been added (or overwrriten) to the internal active translator hashmap.");
	}

	public void removeActiveTranslator(ActiveTranslator i) {
		activeTranslators.remove(i.getUUID());
		CommonDefinitions.sendDebugMessage(i.getUUID() + " has been removed from the internal active translator hashmap.");
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(Player in) {
		return isActiveTranslator(in.getUniqueId());
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player UUID
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(UUID in) {
		return isActiveTranslator(in.toString());
	}
	
	/**
	 * Checks if a given name is a currently active translator.
	 * @param in - A player UUID as a String
	 * @return true if ActiveTranslator, false otherwise
	 */
	public boolean isActiveTranslator(String in) {
		return !getActiveTranslator(in).getUUID().equals("");
	}

	public void addPlayerUsingConfigurationGUI(UUID in) {
		if (!playersUsingConfigurationGUI.contains(in.toString())) {
			playersUsingConfigurationGUI.add(in.toString());
			CommonDefinitions.sendDebugMessage("Player " + getServer().getPlayer(in).getName()
					+ " has been added (or overwrriten) to the internal hashmap of people that are using the configuration GUI.");
		}
	}
	
	public void addPlayerUsingConfigurationGUI(Player in) {
		addPlayerUsingConfigurationGUI(in.getUniqueId());
	}

	public void removePlayerUsingConfigurationGUI(UUID in) {
		playersUsingConfigurationGUI.remove(in.toString());
		CommonDefinitions.sendDebugMessage("Player " + getServer().getPlayer(in).getName()
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
			CommonDefinitions.sendDebugMessage("Term already exists! Not adding.");
			return;
		}
		
		// Exceeds max size
		long estimatedCacheSize = getEstimatedCacheSize();
		CommonDefinitions.sendDebugMessage("Removed least used phrase in cache if at hard limit. Size after removal test: " + estimatedCacheSize);
		
		// Put phrase after (potential) removal
		cache.put(input, outputPhrase);
		CommonDefinitions.sendDebugMessage("Added new phrase into cache! Size after addition: ");
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
		CommonDefinitions.sendDebugMessage("Removed player record of " + i.getUUID() + ".");
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
		return !getPlayerRecord(in, false).getUUID().equals("");
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

	public void setSupportedTranslatorLanguages(List<SupportedLanguageObject> in) {
		supportedLanguages.addAll(in);
	}

	public void setTranslatorName(String i) {
		translatorName = i;
	}
	
	public void setTranslatorErrorCount(int i) {
		translatorErrorCount = i;
	}
	
	/* Getters */
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
		CommonDefinitions.sendDebugMessage("Cache lookup outcome: " + out);
		
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
	
	public boolean isPlayerUsingGUI(String uuid) {
		if (playersUsingConfigurationGUI.contains(uuid)) {
			return true;
		}
		return false;
	}

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
		return playersUsingConfigurationGUI;
	}
	
	public List<SupportedLanguageObject> getSupportedTranslatorLanguages() {
		return supportedLanguages;
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
	
	public String getCurrentMessagesConfigVersion() {
		return currentMessagesConfigVersion;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}
}