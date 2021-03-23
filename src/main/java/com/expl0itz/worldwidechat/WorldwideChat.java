package com.expl0itz.worldwidechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCReload;
import com.expl0itz.worldwidechat.commands.WWCStats;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
import com.expl0itz.worldwidechat.commands.WWCTranslateBook;
import com.expl0itz.worldwidechat.commands.WWCTranslateSign;
import com.expl0itz.worldwidechat.configuration.ConfigurationHandler;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslateSupportedLanguageObject;
import com.expl0itz.worldwidechat.listeners.BookReadListener;
import com.expl0itz.worldwidechat.listeners.ChatListener;
import com.expl0itz.worldwidechat.listeners.DeluxeChatListener;
import com.expl0itz.worldwidechat.listeners.OnPlayerJoinListener;
import com.expl0itz.worldwidechat.listeners.SignReadListener;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CachedTranslation;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.expl0itz.worldwidechat.runnables.LoadUserData;
import com.expl0itz.worldwidechat.runnables.UpdateChecker;
import com.expl0itz.worldwidechat.watson.WatsonSupportedLanguageObject;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
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
    private static TaskChainFactory taskChainFactory;
    
    private BukkitAudiences adventure;
    private ConfigurationHandler configurationManager;
    
    private HashMap < String,BukkitTask > backgroundTasks = new HashMap < String, BukkitTask > ();
    
    private ArrayList < WatsonSupportedLanguageObject > supportedWatsonLanguages;
    private ArrayList < GoogleTranslateSupportedLanguageObject > supportedGoogleTranslateLanguages;
    private ArrayList < ActiveTranslator > activeTranslators = new ArrayList < ActiveTranslator > ();
    private ArrayList < PlayerRecord > playerRecords = new ArrayList < PlayerRecord > ();
    private ArrayList < CachedTranslation > cache = new ArrayList < CachedTranslation > ();
    
    private double pluginVersion = 1.15;
    
    private int bStatsID = 10562;
    private int updateCheckerDelay = 86400;

    private boolean enablebStats = true;
    private boolean outOfDate = false;

    private String pluginPrefixString = "WWC";
    private String pluginLang = "en";
    private String translatorName = "Watson";

    /*Little bug about text components as of adventure 4.5.1:
     * If you do not use a NamedTextColor as your first color (ex: hex), the output will
     * be garbled with some annoying variables. We used the MC dark red to "get around"
     * this. Even though it's more of a good alternative solution now, keep this in mind if
     * this is still not patched + you start with a hex color.
     * */
    private TextComponent pluginPrefix = Component.text()
        .content("[").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true)
        .append(Component.text().content(pluginPrefixString).color(TextColor.color(0x5757c4)))
        .append(Component.text().content("]").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
        .build();

    /* Methods */
    public static WorldwideChat getInstance() {
        return instance;
    }
    
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
    
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }
    
    @Override
    public void onEnable() {
        // Initialize critical instances
        this.adventure = BukkitAudiences.create(this);
        taskChainFactory = BukkitTaskChainFactory.create(this);
        instance = this;
        
        if (loadPluginConfigs()) {
            getLogger().info(ChatColor.LIGHT_PURPLE + getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConnectionSuccess").replace("%o", translatorName));
            
            //Check current Server Version
            checkMCVersion();
            
            //EventHandlers - Check for plugins
            if (getServer().getPluginManager().getPlugin("DeluxeChat") != null) {
                //DeluxeChat is active on our server. Register the event handler.
                getServer().getPluginManager().registerEvents(new DeluxeChatListener(), this);
            } else {
                //No chat plugins on our server. Register the event handler.
                getServer().getPluginManager().registerEvents(new ChatListener(), this); 
            }
            getServer().getPluginManager().registerEvents(new OnPlayerJoinListener(), this);
            getServer().getPluginManager().registerEvents(new SignReadListener(), this);
            getServer().getPluginManager().registerEvents(new BookReadListener(), this);
            getLogger().info(ChatColor.LIGHT_PURPLE + getConfigManager().getMessagesConfig().getString("Messages.wwcListenersInitialized"));

            //Check for Updates
            BukkitTask updateChecker = Bukkit.getScheduler().runTaskAsynchronously(this, new UpdateChecker()); //Run update checker now
            backgroundTasks.put("updateChecker", updateChecker);
            
            //Load saved user data
            Bukkit.getScheduler().runTaskAsynchronously(this, new LoadUserData());
            getLogger().info(ChatColor.LIGHT_PURPLE + getConfigManager().getMessagesConfig().getString("Messages.wwcUserDataReloaded"));
            
            //We made it!
            getLogger().info(ChatColor.GREEN + getConfigManager().getMessagesConfig().getString("Messages.wwcEnabled").replace("%i", pluginVersion + ""));
        } else { //Config init failed
            getLogger().severe(ChatColor.RED + getConfigManager().getMessagesConfig().getString("Messages.wwcInitializationFail").replace("%o", translatorName));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        //Cleanly cancel/reset all background tasks (runnables, timers, vars, etc.)
        cancelBackgroundTasks();
        
        //Set static vars to null
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        instance = null;
        taskChainFactory = null;
        
        //All done.
        getLogger().info("Disabled WorldwideChat version " + pluginVersion + ".");
    }
    
    /* (Re)load Plugin Method */
    public boolean loadPluginConfigs() {
        setConfigManager(new ConfigurationHandler());
        //init main config, then init messages config, then load main settings
        getConfigManager().initMainConfig();
        getConfigManager().initMessagesConfig();
        if (getConfigManager().loadMainSettings()) { //now load settings
            return true;
        }
        return false;
    }
    
    /* Init all commands */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("wwc")) {
            //WWC version report
            final TextComponent versionNotice = Component.text()
                .append(pluginPrefix.asComponent())
                .append(Component.text().content(getConfigManager().getMessagesConfig().getString("Messages.wwcVersion")).color(NamedTextColor.RED))
                .append(Component.text().content(pluginVersion + "").color(NamedTextColor.LIGHT_PURPLE))
                .build();
            Audience adventureSender = adventure.sender(sender);
            adventureSender.sendMessage(versionNotice);
        } else if (command.getName().equalsIgnoreCase("wwcr")) {
            //Reload command
            WWCReload wwcr = new WWCReload(sender, command, label, args);
            return wwcr.processCommand();
        } else if (command.getName().equalsIgnoreCase("wwcg")) {
            //Global translation
            if (checkSenderIdentity(sender)) {
                WWCGlobal wwcg = new WWCGlobal(sender, command, label, args);
                return wwcg.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwct")) {
            //Per player translation
            if (checkSenderIdentity(sender)) {
                WWCTranslate wwct = new WWCTranslate(sender, command, label, args);
                return wwct.processCommand(false);
            }
        } else if (command.getName().equalsIgnoreCase("wwctb")) {
            if (checkSenderIdentity(sender)) {
                WWCTranslateBook wwctb = new WWCTranslateBook(sender, command, label, args);
                return wwctb.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwcts")) {
            if (checkSenderIdentity(sender)) {
                WWCTranslateSign wwcts = new WWCTranslateSign(sender, command, label, args);
                return wwcts.processCommand();
            }
        } else if (command.getName().equalsIgnoreCase("wwcs")) {
            WWCStats wwcs = new WWCStats(sender, command, label, args);
            return wwcs.processCommand();
        }
        return true;
    }
    
    public void cancelBackgroundTasks() {
        //Clear all active translating users
        activeTranslators.clear();
        cache.clear();

        //Cancel + remove all tasks
        for (String eachTask : backgroundTasks.keySet()) {
            backgroundTasks.get(eachTask).cancel();
            backgroundTasks.remove(eachTask);
        }
    }

    public void checkMCVersion() {
        CommonDefinitions defs = new CommonDefinitions();
        String supportedVersions = "";
        for (int i = 0; i < defs.getSupportedMCVersions().length; i++) {
            supportedVersions += "(" + defs.getSupportedMCVersions()[i] + ") ";
            if (Bukkit.getVersion().contains(defs.getSupportedMCVersions()[i])) {
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
    
    /* Common Methods */
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
    
    /* Setters */
    public void setConfigManager(ConfigurationHandler i) {
        configurationManager = i;
    }
    
    public void addBackgroundTask (String name, BukkitTask i) {
        backgroundTasks.put(name, i);
    }
    
    public void removeBackgroundTask (String name) {
        while (backgroundTasks.get(name) != null) {
            backgroundTasks.get(name).cancel();
            backgroundTasks.remove(name);
        }
    }
    
    public void addActiveTranslator(ActiveTranslator i) {
        activeTranslators.add(i);
    }
    
    public void removeActiveTranslator(ActiveTranslator i) {
        for (Iterator < ActiveTranslator > aList = activeTranslators.iterator(); aList.hasNext();) {
            ActiveTranslator activeTranslators = aList.next();
            if (activeTranslators == i) {
                aList.remove();
            }
        }
    }
    
    public void addPlayerRecord(PlayerRecord i) {
        playerRecords.add(i);
    }
    
    public void removePlayerRecord(PlayerRecord i) {
        for (Iterator < PlayerRecord > aList = playerRecords.iterator(); aList.hasNext();) {
            PlayerRecord playerRecords = aList.next();
            if (playerRecords == i) {
                aList.remove();
            }
        }
    }
    
    public void addCacheTerm(CachedTranslation input) {
        if (cache.size() < getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize")) {
            //DEBUG: getLogger().info("Added new term to cache.");
            cache.add(input);
        } else { //cache size is greater than X; let's remove the least used thing
            //DEBUG: getLogger().info("Removing extra term!");
            CachedTranslation leastAmountOfTimes = new CachedTranslation("","","","");
            leastAmountOfTimes.setNumberOfTimes(Integer.MAX_VALUE);
            for (int i = 0; i < cache.size(); i++) {
                if (cache.get(i).getNumberOfTimes() < leastAmountOfTimes.getNumberOfTimes())
                {
                    leastAmountOfTimes = cache.get(i);
                }
            }
            removeCacheTerm(leastAmountOfTimes);
            //DEBUG: getLogger().info("Removed.");
            cache.add(input);
        }
    }

    public void removeCacheTerm(CachedTranslation i) {
        for (Iterator < CachedTranslation > aList = cache.iterator(); aList.hasNext();) {
            CachedTranslation cachedTranslation = aList.next();
            if (cachedTranslation == i) {
                aList.remove();
            }
        }
    }

    public void setPrefixName(String i) {
        pluginPrefix = Component.text()
                .content("[").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true)
                .append(Component.text().content(i).color(TextColor.color(0x5757c4)))
                .append(Component.text().content("]").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
                .build();
        pluginPrefixString = i;
    }

    public void setSupportedWatsonLanguages(ArrayList < WatsonSupportedLanguageObject > in) {
        supportedWatsonLanguages = in;
    }
    
    public void setSupportedGoogleTranslateLanguages(ArrayList < GoogleTranslateSupportedLanguageObject > in) {
        supportedGoogleTranslateLanguages = in;
    }
    
    public void setUpdateCheckerDelay(int i) {
        updateCheckerDelay = i;
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
    public HashMap < String, BukkitTask > getBackgroundTasks() {
        return backgroundTasks;
    }
    
    public ActiveTranslator getActiveTranslator(String uuid) {
        if (activeTranslators.size() > 0) //just return false if there are no active translators, less code to run
        {
            for (ActiveTranslator eaTranslator: activeTranslators) {
                if (eaTranslator.getUUID().equals(uuid)) //if uuid matches up with one in ArrayList, we chillin'
                {
                    return eaTranslator;
                }
            }
        }
        return null;
    }

    public ArrayList < ActiveTranslator > getActiveTranslators() {
        return activeTranslators;
    }

    public PlayerRecord getPlayerRecord(String UUID) {
        if (playerRecords.size() > 0) 
        {
            for (PlayerRecord eaRecord: playerRecords) {
                if (eaRecord.getUUID().equals(UUID)) 
                {
                    return eaRecord;
                }
            }
        }
        return null;
    }
    
    public ArrayList < PlayerRecord > getPlayerRecords() {
        return playerRecords;
    }
    
    public ArrayList < WatsonSupportedLanguageObject > getSupportedWatsonLanguages() {
        return supportedWatsonLanguages;
    }
    
    public ArrayList < GoogleTranslateSupportedLanguageObject > getSupportedGoogleTranslateLanguages() {
        return supportedGoogleTranslateLanguages;
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
    
    public ConfigurationHandler getConfigManager() {
        return configurationManager;
    }
    
    public ArrayList <CachedTranslation> getCache() {
        return cache;
    }
}