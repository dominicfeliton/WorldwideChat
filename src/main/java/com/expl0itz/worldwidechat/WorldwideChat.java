package com.expl0itz.worldwidechat;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;

import com.expl0itz.worldwidechat.commands.WWCConfiguration;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslateRateLimit;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.commands.WWCStats;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateInGameObjects;
import com.expl0itz.worldwidechat.commands.WWCTranslateItem;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.configuration.ConfigurationHandler;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
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

import fr.minuskube.inv.InventoryManager;
import io.reactivex.annotations.NonNull;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class WorldwideChat extends JavaPlugin {
	/* Vars */
	private static WorldwideChat instance;

	private InventoryManager inventoryManager;
	private BukkitAudiences adventure;
	private ConfigurationHandler configurationManager;

	private List<SupportedLanguageObject> supportedLanguages = new CopyOnWriteArrayList<SupportedLanguageObject>(); // Way																												// write
	private List<PlayerRecord> playerRecords = new CopyOnWriteArrayList<PlayerRecord>(); // Way more reads, occasional																			// write
	private List<ActiveTranslator> activeTranslators = Collections.synchronizedList(new ArrayList<ActiveTranslator>()); // Many																					// writes
	private List<CachedTranslation> cache = Collections.synchronizedList(new ArrayList<CachedTranslation>()); // Many																				// writes
	private List<Player> playersUsingConfigurationGUI = Collections.synchronizedList(new ArrayList<Player>()); // Many																				// writes

	private String pluginVersion = this.getDescription().getVersion();

	private int rateLimit = 0;
	private int bStatsID = 10562;
	private String currentMessagesConfigVersion = "8302021-1"; //This is just MM-DD-YYYY-whatever
	private int updateCheckerDelay = 86400;
	private int syncUserDataDelay = 7200;
	private int asyncTasksTimeoutSeconds = 7;
	private int errorCount = 0;
	private int errorLimit = 5;
	private int translatorCacheLimit = 100;
	private int maxResponseTime = 7;

	private boolean enablebStats = true;
	private boolean outOfDate = false;
	private boolean debugMode = false;
	
	private String pluginLang = "en";
	private String translatorName = "Starting";

	/* Default constructor */
	public WorldwideChat() {
		super();
	}

	/* MockBukkit required constructor */
	protected WorldwideChat(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	private TextComponent pluginPrefix = Component.text().content("[").color(NamedTextColor.DARK_RED)
			.append(Component.text().content("WWC").color(NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
			.append(Component.text().content("]").color(NamedTextColor.DARK_RED))
			.build();

	/* Methods */
	public static WorldwideChat getInstance() {
		return instance;
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}

	@Override
	public void onEnable() {
		// Initialize critical instances
		this.adventure = BukkitAudiences.create(this); // Adventure
		inventoryManager = new WWCInventoryManager(this); // InventoryManager for SmartInvs API
		inventoryManager.init(); // Init InventoryManager
		instance = this; // Static instance of this class
		registerGlowEffect(); // Register inventory glow effect

		// Load plugin configs, check if they successfully initialized
		loadPluginConfigs(false);

		// Check current server version
		checkMCVersion();

		// EventHandlers + check for plugins
		if (getServer().getPluginManager().getPlugin("DeluxeChat") != null) { // DeluxeChat is incompatible as of v1.3
			// getServer().getPluginManager().registerEvents(new DeluxeChatListener(),
			// this);
			getLogger().warning(CommonDefinitions.getMessage("wwcDeluxeChatIncompatible"));
		}
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new OnPlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new TranslateInGameListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getLogger().info(ChatColor.LIGHT_PURPLE
				+ CommonDefinitions.getMessage("wwcListenersInitialized"));

		// We made it!
		getLogger().info(ChatColor.GREEN + CommonDefinitions.getMessage("wwcEnabled")
				.replace("%i", pluginVersion));
	}

	@Override
	public void onDisable() {
		// Wait for background async tasks to finish
		// Thanks to:
		// https://gist.github.com/blablubbabc/e884c114484f34cae316c48290b21d8e#file-someplugin-java-L37
		if (!translatorName.equals("JUnit/MockBukkit Testing Translator")) {
			final long asyncTasksTimeoutMillis = (long) asyncTasksTimeoutSeconds * 1000;
			final long asyncTasksStart = System.currentTimeMillis();
			boolean asyncTasksTimeout = false;
			while (this.getActiveAsyncTasks() > 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// Disable once we reach timeout
				if (System.currentTimeMillis() - asyncTasksStart > asyncTasksTimeoutMillis) {
					asyncTasksTimeout = true;
					CommonDefinitions.sendDebugMessage(
							"Waited " + asyncTasksTimeoutSeconds + " seconds for " + this.getActiveAsyncTasks()
									+ " remaining async tasks to complete. Disabling regardless...");
					break;
				}
			}
			final long asyncTasksTimeWaited = System.currentTimeMillis() - asyncTasksStart;
			if (!asyncTasksTimeout && asyncTasksTimeWaited > 1) {
				CommonDefinitions.sendDebugMessage("Waited " + asyncTasksTimeWaited + " ms for async tasks to finish.");
			}
		}

		// Cleanly cancel/reset all background tasks (runnables, timers, vars, etc.)
		cancelBackgroundTasks();

		// Unregister listeners
		HandlerList.unregisterAll(this);

		// Set static vars to null
		if (this.adventure != null) {
			this.adventure.close();
			this.adventure = null;
		}
		instance = null;
		CommonDefinitions.supportedMCVersions = null;
		CommonDefinitions.supportedPluginLangCodes = null;

		// All done.
		getLogger().info("Disabled WorldwideChat version " + pluginVersion + ".");
	}

	/* Get active async tasks */
	private int getActiveAsyncTasks() {
		int workers = 0;
		if (!translatorName.equals("JUnit/MockBukkit Testing Translator")) {
			for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
				if (worker.getOwner().equals(this)) {
					workers++;
				}
			}
		}
		return workers;
	}

	/* Init all commands */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("wwc")) {
			// WWC version
			final TextComponent versionNotice = Component.text()
					.append((Component.text().content(CommonDefinitions.getMessage("wwcVersion")).color(NamedTextColor.RED))
					.append((Component.text().content(" " + pluginVersion)).color(NamedTextColor.LIGHT_PURPLE))).build();
			CommonDefinitions.sendMessage(sender, versionNotice);
		} else if (command.getName().equalsIgnoreCase("wwcr") && !translatorName.equals("Starting")
				&& getActiveAsyncTasks() == 0) {
			// Reload command
			WWCReload wwcr = new WWCReload(sender, command, label, args);
			return wwcr.processCommand();
		} else if (command.getName().equalsIgnoreCase("wwcg") && hasValidTranslatorSettings(sender)) {
			// Global translation
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslate wwcg = new WWCGlobal(sender, command, label, args);
				return wwcg.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwct") && hasValidTranslatorSettings(sender)) {
			// Per player translation
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslate wwct = new WWCTranslate(sender, command, label, args);
				return wwct.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwctb") && hasValidTranslatorSettings(sender)) {
			// Book translation
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslateInGameObjects wwctb = new WWCTranslateBook(sender, command, label, args);
				return wwctb.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwcts") && hasValidTranslatorSettings(sender)) {
			// Sign translation
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslateInGameObjects wwcts = new WWCTranslateSign(sender, command, label, args);
				return wwcts.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwcti") && hasValidTranslatorSettings(sender)) {
			// Item translation
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslateInGameObjects wwcti = new WWCTranslateItem(sender, command, label, args);
				return wwcti.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwcs")) {
			// Stats for translator
			WWCStats wwcs = new WWCStats(sender, command, label, args);
			return wwcs.processCommand();
		} else if (command.getName().equalsIgnoreCase("wwcc")) {
			// Configuration GUI
			if (checkSenderIdentity(sender)) {
				WWCConfiguration wwcc = new WWCConfiguration(sender, command, label, args);
				return wwcc.processCommand();
			}
		} else if (command.getName().equalsIgnoreCase("wwctrl")) {
			// Rate Limit Command
			if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
				WWCTranslateRateLimit wwctrl = new WWCTranslateRateLimit(sender, command, label, args);
				return wwctrl.processCommand();
			}
		}
		return true;
	}

	/* Easy Reload Method */
	public void reload() {
		errorCount = 0;
		if (!translatorName.equals("Invalid")) {
			translatorName = "Starting";
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				cancelBackgroundTasks();
				loadPluginConfigs(true);
			}
		}.runTaskAsynchronously(this);
	}

	/* Cancel Background Tasks */
	public void cancelBackgroundTasks() {
		// Close all inventories
		CommonDefinitions.closeAllInventories();

		// Cancel + remove all tasks
		WorldwideChat.getInstance().getServer().getScheduler().cancelTasks(WorldwideChat.getInstance());

		// Sync activeTranslators, playerRecords to disk
		getConfigManager().syncData();

		// Clear all active translating users, cache, playersUsingConfigGUI
		supportedLanguages.clear();
		playerRecords.clear();
		activeTranslators.clear();
		cache.clear();
	}

	/* Load Plugin Configs */
	public void loadPluginConfigs(boolean isReloading) {
		setConfigManager(new ConfigurationHandler());

		// Init main config, then init messages config, then load main settings
		getConfigManager().initMainConfig();
		getConfigManager().initMessagesConfig();
		getConfigManager().loadMainSettings();
		getConfigManager().loadTranslatorSettings();

		/* Run tasks after translator loaded */
		try {
			// Check for updates
			Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new UpdateChecker(), 0,
					getUpdateCheckerDelay() * 20); // Run update checker now

			// Schedule automatic user data sync
			Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new SyncUserData(), getSyncUserDataDelay() * 20,
					getSyncUserDataDelay() * 20);

			// Load saved user data
			new LoadUserData().run();

			if (isReloading) {
				new BukkitRunnable() {
					@Override
					public void run() {
						registerTabCompleters();
					}
				}.runTask(this);
			} else {
				registerTabCompleters();
			}
		} catch (IllegalPluginAccessException e) {
			// We will only run into this if the user runs /stop or /reload confirm
			// right after a /wwcr.
			// If this happens, and we run into an exception despite our checks,
			// Just catch the exception.
			// This will not affect the user in any way, since the only way we run into this
			// is if we are getting disabled.
			CommonDefinitions.sendDebugMessage(
					"Ran into an IllegalPluginAccessException on " + this.getClass().getName() + ", oh well...");
		}
	}

	public void checkMCVersion() {
		String supportedVersions = "";
		for (int i = 0; i < CommonDefinitions.supportedMCVersions.length; i++) {
			supportedVersions += "(" + CommonDefinitions.supportedMCVersions[i] + ") ";
			if (Bukkit.getVersion().contains(CommonDefinitions.supportedMCVersions[i])) {
				return;
			}
		}
		// Not running a supported version of Bukkit, Spigot, or Paper
		getLogger().warning(CommonDefinitions.getMessage("wwcUnsupportedVersion"));
		getLogger().warning(supportedVersions);
	}

	public @NonNull BukkitAudiences adventure() {
		if (this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
		}
		return this.adventure;
	}

	public boolean checkSenderIdentity(CommandSender sender) {
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

	public boolean hasValidTranslatorSettings(CommandSender sender) {
		if (getTranslatorName().equals("Starting")) {
			final TextComponent notDone = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcNotDoneLoading"))
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

	public void registerGlowEffect() {
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(this, "wwc_glow"));
			Enchantment.registerEnchantment(glow);
		} catch (IllegalArgumentException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerTabCompleters() {
		// Register tab completers
		getCommand("wwcg").setTabCompleter(new WWCTabCompleter());
		getCommand("wwct").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcti").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctb").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcts").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcs").setTabCompleter(new WWCTabCompleter());
		getCommand("wwctrl").setTabCompleter(new WWCTabCompleter());
		getCommand("wwc").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcr").setTabCompleter(new WWCTabCompleter());
		getCommand("wwcc").setTabCompleter(new WWCTabCompleter());
	}

	/* Setters */
	public void setConfigManager(ConfigurationHandler i) {
		configurationManager = i;
	}

	public void addActiveTranslator(ActiveTranslator i) {
		if (!activeTranslators.contains(i)) {
			activeTranslators.add(i);
			CommonDefinitions.sendDebugMessage(i.getUUID() + " has been added to the internal active translator list.");
		}
	}

	public void removeActiveTranslator(ActiveTranslator i) {
		activeTranslators.remove(i);
		CommonDefinitions.sendDebugMessage(i.getUUID() + " has been removed from the internal active translator list.");
	}

	public void addPlayerUsingConfigurationGUI(Player p) {
		if (!playersUsingConfigurationGUI.contains(p)) {
			playersUsingConfigurationGUI.add(p);
			CommonDefinitions.sendDebugMessage("Player " + p.getName()
					+ " has been added to the internal list of people that are using the configuration GUI.");
		}
	}

	public void removePlayerUsingConfigurationGUI(Player p) {
		playersUsingConfigurationGUI.remove(p);
		CommonDefinitions.sendDebugMessage("Player " + p.getName()
				+ " has been removed from the internal list of people that are using the configuration GUI.");
	}

	public void addCacheTerm(CachedTranslation input) {
		if (translatorCacheLimit > 0) {
			if (cache.size() < translatorCacheLimit) {
				CommonDefinitions.sendDebugMessage("Added new phrase into cache!");
				cache.add(input);
			} else { // cache size is greater than X; let's remove the least used thing
				CachedTranslation leastAmountOfTimes = new CachedTranslation("", "", "", "");
				leastAmountOfTimes.setNumberOfTimes(Integer.MAX_VALUE);
				synchronized (cache) {
					for (CachedTranslation eaTrans : cache) {
						if (eaTrans.getNumberOfTimes() < leastAmountOfTimes.getNumberOfTimes()) {
							leastAmountOfTimes = eaTrans;
						}
					}
				}

				removeCacheTerm(leastAmountOfTimes);
				CommonDefinitions
						.sendDebugMessage("Removed least used phrase in cache, since we are now at the hard limit.");
				addCacheTerm(input);
			}
		}
	}

	public void removeCacheTerm(CachedTranslation i) {
		cache.remove(i);
	}

	public void addPlayerRecord(PlayerRecord i) {
		if (!playerRecords.contains(i)) {
			playerRecords.add(i);
		}
	}

	public void removePlayerRecord(PlayerRecord i) {
		playerRecords.remove(i);
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

	public void setUpdateCheckerDelay(int i) {
		updateCheckerDelay = i;
	}

	public void setRateLimit(int i) {
		rateLimit = i;
	}

	public void setSyncUserDataDelay(int i) {
		syncUserDataDelay = i;
	}

	public void setErrorCount(int i) {
		errorCount = i;
	}

	public void setErrorLimit(int i) {
		errorLimit = i;
	}

	public void setTranslatorCacheLimit(int i) {
		translatorCacheLimit = i;
	}

	public void setMaxResponseTime(int i) {
		maxResponseTime = i;
	}
	
	public void setPluginLang(String i) {
		pluginLang = i;
	}

	public void setTranslatorName(String i) {
		translatorName = i;
	}

	public void setbStats(boolean i) {
		enablebStats = i;
	}

	public void setOutOfDate(boolean i) {
		outOfDate = i;
	}

	public void setDebugMode(boolean i) {
		debugMode = i;
	}

	/* Getters */
	public ActiveTranslator getActiveTranslator(String uuid) {
		if (activeTranslators.size() > 0) // just return false if there are no active translators, less code to run
		{
			synchronized (activeTranslators) {
				for (ActiveTranslator eaTranslator : activeTranslators) {
					if (eaTranslator.getUUID().equals(uuid)) // if uuid matches up with one in ArrayList
					{
						return eaTranslator;
					}
				}
			}
		}
		return new ActiveTranslator("", "", "");
	}

	public PlayerRecord getPlayerRecord(String UUID, boolean createNewIfNotExisting) {
		if (playerRecords.size() > 0) {
			synchronized (playerRecords) {
				for (PlayerRecord eaRecord : playerRecords) {
					// If the player is in the ArrayList
					if (eaRecord.getUUID().toString().equals(UUID)) {
						return eaRecord;
					}
				}
			}
		}
		if (createNewIfNotExisting) {
			// Create + add new record
			PlayerRecord newRecord = new PlayerRecord("--------", UUID, 0, 0);
			addPlayerRecord(newRecord);
			return newRecord;
		}
		return new PlayerRecord("", "", -1, -1);
	}

	public List<ActiveTranslator> getActiveTranslators() {
		return activeTranslators;
	}

	public List<Player> getPlayersUsingGUI() {
		return playersUsingConfigurationGUI;
	}

	public List<CachedTranslation> getCache() {
		return cache;
	}

	public List<PlayerRecord> getPlayerRecords() {
		return playerRecords;
	}

	public List<SupportedLanguageObject> getSupportedTranslatorLanguages() {
		return supportedLanguages;
	}

	public TextComponent getPluginPrefix() {
		return pluginPrefix;
	}

	public String getPluginLang() {
		return pluginLang;
	}

	public String getTranslatorName() {
		return translatorName;
	}
	
	public String getCurrentMessagesConfigVersion() {
		return currentMessagesConfigVersion;
	}

	public boolean getbStats() {
		return enablebStats;
	}

	public boolean getOutOfDate() {
		return outOfDate;
	}

	public boolean getDebugMode() {
		return debugMode;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}

	public int getbStatsID() {
		return bStatsID;
	}

	public int getUpdateCheckerDelay() {
		return updateCheckerDelay;
	}

	public int getRateLimit() {
		return rateLimit;
	}

	public int getSyncUserDataDelay() {
		return syncUserDataDelay;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getErrorLimit() {
		return errorLimit;
	}

	public int getTranslatorCacheLimit() {
		return translatorCacheLimit;
	}
	
	public int getMaxResponseTime() {
		return maxResponseTime;
	}

	public ConfigurationHandler getConfigManager() {
		return configurationManager;
	}
}