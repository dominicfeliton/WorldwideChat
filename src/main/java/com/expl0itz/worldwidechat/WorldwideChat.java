package com.expl0itz.worldwidechat;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.commands.WWCConfiguration;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslateRateLimit;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.commands.WWCStats;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
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
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CachedTranslation;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.expl0itz.worldwidechat.misc.SupportedLanguageObject;
import com.expl0itz.worldwidechat.runnables.LoadUserData;
import com.expl0itz.worldwidechat.runnables.SyncUserData;
import com.expl0itz.worldwidechat.runnables.UpdateChecker;

import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import io.reactivex.annotations.NonNull;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WorldwideChat extends JavaPlugin {
    /* Vars */
    private static WorldwideChat instance;
    
    private InventoryManager inventoryManager;
    private BukkitAudiences adventure;
    private ConfigurationHandler configurationManager;
    
    private List < SupportedLanguageObject > supportedLanguages = new CopyOnWriteArrayList < SupportedLanguageObject > (); //Way more reads, every reload write
    private List < PlayerRecord > playerRecords = new CopyOnWriteArrayList < PlayerRecord > (); //Way more reads, occasional write
    private List < ActiveTranslator > activeTranslators = Collections.synchronizedList(new ArrayList < ActiveTranslator > ()); //Many writes
    private List < CachedTranslation > cache = Collections.synchronizedList(new ArrayList < CachedTranslation > ()); //Many writes
    private List < Player > playersUsingConfigurationGUI = Collections.synchronizedList(new ArrayList < Player > ()); //Many writes
    
    private double pluginVersion = Double.parseDouble(this.getDescription().getVersion());
    
    private int rateLimit = 0;
    private int bStatsID = 10562;
    private int updateCheckerDelay = 86400;
    private int syncUserDataDelay = 7200;

    private boolean enablebStats = true;
    private boolean outOfDate = false;

    private String pluginPrefixString = "WWC";
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
    
    private TextComponent pluginPrefix = Component.text()
        .content("[").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true)
        .append(Component.text().content(pluginPrefixString).color(TextColor.color(0x5757c4)))
        .append(Component.text().content("]").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
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
        //Initialize critical instances
        this.adventure = BukkitAudiences.create(this); //Adventure
        inventoryManager = new WWCInventoryManager(this); //InventoryManager for SmartInvs API
        inventoryManager.init(); //Init InventoryManager
        instance = this; //Static instance of this class
        registerGlowEffect(); //Register inventory glow effect
        
        //Load plugin configs, check if they successfully initialized
        loadPluginConfigs();
        
        //Check current server version
        checkMCVersion();
        
        //EventHandlers + check for plugins
    	if (getServer().getPluginManager().getPlugin("DeluxeChat") != null) { //DeluxeChat is incompatible as of v1.3
            //getServer().getPluginManager().registerEvents(new DeluxeChatListener(), this);
            getLogger().warning(getConfigManager().getMessagesConfig().getString("Messages.wwcDeluxeChatIncompatible"));
        }
		getServer().getPluginManager().registerEvents(new ChatListener(), this); 
		getServer().getPluginManager().registerEvents(new OnPlayerJoinListener(), this);
		getServer().getPluginManager().registerEvents(new TranslateInGameListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getLogger().info(ChatColor.LIGHT_PURPLE + getConfigManager().getMessagesConfig().getString("Messages.wwcListenersInitialized"));
        
        //We made it!
        getLogger().info(ChatColor.GREEN + getConfigManager().getMessagesConfig().getString("Messages.wwcEnabled").replace("%i", pluginVersion + ""));
    }

    @Override
    public void onDisable() {
        //Cleanly cancel/reset all background tasks (runnables, timers, vars, etc.)
        cancelBackgroundTasks();
        
        //Unregister listeners
        HandlerList.unregisterAll(this);
        
        //Set static vars to null
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        instance = null;
        CommonDefinitions.supportedMCVersions = null;
        CommonDefinitions.supportedPluginLangCodes = null;
        
        //All done.
        getLogger().info("Disabled WorldwideChat version " + pluginVersion + ".");
    }
    
    /* Init all commands */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if (command.getName().equalsIgnoreCase("wwc")) {
            //WWC version
            final TextComponent versionNotice = Component.text()
                .append(pluginPrefix.asComponent())
                .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcVersion")).color(NamedTextColor.RED))
                .append(Component.text().content(" " + pluginVersion).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            Audience adventureSender = adventure.sender(sender);
            adventureSender.sendMessage(versionNotice);
        //TODO: DONT RUN /WWCR UNTIL PLUGIN IS COMPLETELY LOADED.
        } else if (command.getName().equalsIgnoreCase("wwcr") && !translatorName.equals("Starting")) {
            //Reload command
            WWCReload wwcr = new WWCReload(sender, command, label, args);
            return wwcr.processCommand();
        } else if (command.getName().equalsIgnoreCase("wwcg") && hasValidTranslatorSettings(sender)) {
            //Global translation
            if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
                WWCGlobal wwcg = new WWCGlobal(sender, command, label, args);
                return wwcg.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwct") && hasValidTranslatorSettings(sender)) {
            //Per player translation
            if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
                WWCTranslate wwct = new WWCTranslate(sender, command, label, args);
                return wwct.processCommand(false);
            }
        } else if (command.getName().equalsIgnoreCase("wwctb") && hasValidTranslatorSettings(sender)) {
            //Book translation
            if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
                WWCTranslateBook wwctb = new WWCTranslateBook(sender, command, label, args);
                return wwctb.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwcts") && hasValidTranslatorSettings(sender)) {
            //Sign translation
            if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
                WWCTranslateSign wwcts = new WWCTranslateSign(sender, command, label, args);
                return wwcts.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwcti") && hasValidTranslatorSettings(sender)) {
            //Item translation
            if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
                WWCTranslateItem wwcti = new WWCTranslateItem(sender, command, label, args);
                return wwcti.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwcs")) {
            //Stats for translator
            WWCStats wwcs = new WWCStats(sender, command, label, args);
            return wwcs.processCommand();
        } else if (command.getName().equalsIgnoreCase("wwcc")) {
        	//Configuration GUI
        	if (checkSenderIdentity(sender)) {
        		WWCConfiguration wwcc = new WWCConfiguration(sender, command, label, args);
        		return wwcc.processCommand();
        	}
        } else if (command.getName().equalsIgnoreCase("wwctrl")) {
        	//Rate Limit Command
        	if (checkSenderIdentity(sender) && hasValidTranslatorSettings(sender)) {
        		WWCTranslateRateLimit wwctrl = new WWCTranslateRateLimit(sender, command, label, args);
        		return wwctrl.processCommand();
        	}
        }
        return true;
    }
    
    /* Easy Reload Method */
    public void reload() {
    	translatorName = "Starting";
    	cancelBackgroundTasks();
    	loadPluginConfigs();
    }
    
    /* Load Plugin Configs */
    public void loadPluginConfigs() {
    	setConfigManager(new ConfigurationHandler());
    	
    	//init main config, then init messages config, then load main settings
        getConfigManager().initMainConfig();
        getConfigManager().initMessagesConfig();
        getConfigManager().loadMainSettings();
        new BukkitRunnable() {
        	@Override
        	public void run() {
        		getConfigManager().loadTranslatorSettings();
        		/* Run tasks after translator loaded */
                //Check for updates
                Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new UpdateChecker(), 0, getUpdateCheckerDelay()*20); //Run update checker now
                
                //Load saved user data
                Bukkit.getScheduler().runTaskAsynchronously(instance, new LoadUserData());
                
                //Schedule automatic user data sync
                Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new SyncUserData(), getSyncUserDataDelay()*20, getSyncUserDataDelay()*20);
        	}
        }.runTaskAsynchronously(this);
        
    	//Register tab completers
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
    
    public void cancelBackgroundTasks() {
    	//Cancel + remove all tasks, chains
    	this.getServer().getScheduler().cancelTasks(this);
    	
        //Close all active GUIs
        playersUsingConfigurationGUI.clear();
        for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
        	try {
        		if (inventoryManager.getInventory(eaPlayer).get() instanceof SmartInventory && inventoryManager.getInventory(eaPlayer).get().getManager().equals(inventoryManager)) {
            		eaPlayer.closeInventory();
            	}
        	} catch (NoSuchElementException e) {
        		continue;
        	}
        }

        //Clear all supported langs
        supportedLanguages.clear();
        
        //Sync activeTranslators, playerRecords to disk
        getConfigManager().syncData();
            
        //Clear all active translating users, cache, playersUsingConfigGUI
        playerRecords.clear();
        activeTranslators.clear();
        cache.clear();
    }

    public void checkMCVersion() {
        String supportedVersions = "";
        for (int i = 0; i < CommonDefinitions.supportedMCVersions.length; i++) {
            supportedVersions += "(" + CommonDefinitions.supportedMCVersions[i] + ") ";
            if (Bukkit.getVersion().contains(CommonDefinitions.supportedMCVersions[i])) {
                return;
            }
        }
        //Not running a supported version of Bukkit, Spigot, or Paper
        getLogger().warning(getConfigManager().getMessagesConfig().getString("Messages.wwcUnsupportedVersion"));
        getLogger().warning(supportedVersions);
    }
    
    public @NonNull BukkitAudiences adventure() {
        if(this.adventure == null) {
          throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
    
    public boolean checkSenderIdentity(CommandSender sender) {
        if (!(sender instanceof Player)) {
            final TextComponent consoleNotice = Component.text()
                .append(pluginPrefix.asComponent())
                .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcNoConsole")).color(NamedTextColor.RED))
                .build();
            Audience adventureSender = adventure.sender(sender);
            adventureSender.sendMessage(consoleNotice);
            return false;
        }
        return true;
    }
    
    public boolean hasValidTranslatorSettings(CommandSender sender) {
    	if (getTranslatorName().equals("Starting")) {
    		final TextComponent notDone = Component.text()
                    .append(pluginPrefix.asComponent())
                    .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcNotDoneLoading")).color(NamedTextColor.YELLOW))
                    .build();
                Audience adventureSender = adventure.sender(sender);
                adventureSender.sendMessage(notDone);
    		return false;
    	} else if (getTranslatorName().equals("Invalid")) {
    		final TextComponent invalid = Component.text()
                    .append(pluginPrefix.asComponent())
                    .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcInvalidTranslator")).color(NamedTextColor.RED))
                    .build();
                Audience adventureSender = adventure.sender(sender);
                adventureSender.sendMessage(invalid);
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
        } try {
            EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(this, "wwc_glow"));
            Enchantment.registerEnchantment(glow);
        } catch (IllegalArgumentException e) {
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /* Setters */
    public void setConfigManager(ConfigurationHandler i) {
        configurationManager = i;
    }
    
    public void addActiveTranslator(ActiveTranslator i) {
    	if (!activeTranslators.contains(i)) {
    		activeTranslators.add(i);
    	}
    }
    
    public void removeActiveTranslator(ActiveTranslator i) {
    	activeTranslators.remove(i);
    }
    
    public void addPlayerUsingConfigurationGUI(Player p) {
    	if (!playersUsingConfigurationGUI.contains(p)) {
    		playersUsingConfigurationGUI.add(p);
    	}
    }
    
    public void removePlayerUsingConfigurationGUI(Player p) {
    	playersUsingConfigurationGUI.remove(p);
    }
    
    public void addCacheTerm(CachedTranslation input) {
        if (cache.size() < getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize")) {
            cache.add(input);
        } else { //cache size is greater than X; let's remove the least used thing
            CachedTranslation leastAmountOfTimes = new CachedTranslation("","","","");
            leastAmountOfTimes.setNumberOfTimes(Integer.MAX_VALUE);
            synchronized (cache) {
            	for (CachedTranslation eaTrans : cache) {
            		if (eaTrans.getNumberOfTimes() < leastAmountOfTimes.getNumberOfTimes()) {
            			leastAmountOfTimes = eaTrans;
            		}
            	}
            }
            
            removeCacheTerm(leastAmountOfTimes);
            if (cache.size() < getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize")) {
                cache.add(input);
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
        pluginPrefix = Component.text()
                .content("[").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true)
                .append(Component.text().content(i).color(TextColor.color(0x5757c4)))
                .append(Component.text().content("]").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
                .build();
        pluginPrefixString = i;
    }

    public void setSupportedTranslatorLanguages(List < SupportedLanguageObject > in) {
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
    
    /* Getters */
    public ActiveTranslator getActiveTranslator(String uuid) {
        if (activeTranslators.size() > 0) //just return false if there are no active translators, less code to run
        {
        	synchronized (activeTranslators) {
        		for (ActiveTranslator eaTranslator: activeTranslators) {
                    if (eaTranslator.getUUID().equals(uuid)) //if uuid matches up with one in ArrayList
                    {
                        return eaTranslator;
                    }
                }
        	}
        }
        return null;
    }
    
    public PlayerRecord getPlayerRecord(String UUID, boolean createNewIfNotExisting) {
        if (playerRecords.size() > 0) {
        	synchronized (playerRecords) {
        		for (PlayerRecord eaRecord: playerRecords) {
                    //If the player is in the ArrayList
                    if (eaRecord.getUUID().toString().equals(UUID))  {
                        return eaRecord;
                    }
                }
        	}
        }
        if (createNewIfNotExisting) {
            //Create + add new record
            PlayerRecord newRecord = new PlayerRecord("--------", UUID, 0, 0);
            addPlayerRecord(newRecord);
            return newRecord;
        }
        return null;
    }

    public List < ActiveTranslator > getActiveTranslators() {
        return activeTranslators;
    }

    public List < Player > getPlayersUsingGUI() {
    	return playersUsingConfigurationGUI;
    }
    
    public List <CachedTranslation> getCache() {
        return cache;
    }
    
    public List < PlayerRecord > getPlayerRecords() {
        return playerRecords;
    }
    
    public List < SupportedLanguageObject > getSupportedTranslatorLanguages() {
    	return supportedLanguages;
    }
    
    public TextComponent getPluginPrefix() {
        return pluginPrefix;
    }

    public String getPluginLang() {
        return pluginLang;
    }

    public String getPrefixName() {
        return pluginPrefixString;
    }
    
    public String getTranslatorName() {
    	return translatorName;
    }

    public boolean getbStats() {
        return enablebStats;
    }
    
    public boolean getOutOfDate() {
        return outOfDate;
    }
    
    public double getPluginVersion() {
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
    
    public ConfigurationHandler getConfigManager() {
        return configurationManager;
    }
}