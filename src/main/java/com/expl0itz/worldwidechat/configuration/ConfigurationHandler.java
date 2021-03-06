package com.expl0itz.worldwidechat.configuration;

import java.io.File;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CachedTranslation;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.watson.WatsonTranslation;

public class ConfigurationHandler {

    private WorldwideChat main = WorldwideChat.getInstance();

    private File messagesFile;
    private File configFile;
    private FileConfiguration messagesConfig;
    private FileConfiguration mainConfig;

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

        /* Get plugin lang */
        String pluginLang = "en";
        CommonDefinitions defs = new CommonDefinitions();
        for (int i = 0; i < defs.getSupportedPluginLangCodes().length; i++) {
            if (defs.getSupportedPluginLangCodes()[i].equalsIgnoreCase(getMainConfig().getString("General.pluginLang"))) {
                main.setPluginLang(getMainConfig().getString("General.pluginLang"));
                main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + pluginLang + ".");
            } else {
                main.getLogger().warning(ChatColor.RED + "Unable to detect a valid language in your config.yml. Defaulting to en...");
            }
        }
    }

    /* Init Other Configs Method */
    public void initMessagesConfig() {
        /* Init config files */
        messagesFile = new File(main.getDataFolder(), "messages-" + main.getPluginLang() + ".yml");

        /* Generate config files, if they do not exist */
        if (!messagesFile.exists()) {
            main.saveResource("messages-" + main.getPluginLang() + ".yml", true);
        }

        /* Load configs */
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /* Load Main Settings Method */
    public boolean loadMainSettings() {
        /* Get rest of General Settings */
        //Prefix
        try {
            if (!getMainConfig().getString("General.prefixName").equalsIgnoreCase("Default")) {
                main.setPrefixName(getMainConfig().getString("General.prefixName"));
            }
        } catch (Exception e) {
            main.getLogger().warning((getMessagesConfig().getString("Messages.wwcConfigInvalidPrefixSettings")));
        }
        //bStats
        main.setbStats(getMainConfig().getBoolean("General.enablebStats"));
        if (getMainConfig().getBoolean("General.enablebStats")) {
            Metrics metrics = new Metrics(WorldwideChat.getInstance(), WorldwideChat.getInstance().getbStatsID());
            main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigEnabledbStats"));
        } else {
            main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigDisabledbStats"));
        }
        //Update Checker Delay
        try {
            if ((getMainConfig().getInt("General.updateCheckerDelay") > 10)) {
                main.setUpdateCheckerDelay(getMainConfig().getInt("General.updateCheckerDelay"));
            } else {
                main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigBadUpdateDelay"));
            }
        } catch (Exception e) {
            main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigBadUpdateDelay"));
        }
        
        /* Translator Settings */
        if (getMainConfig().getBoolean("Translator.useWatsonTranslate") && !(getMainConfig().getBoolean("Translator.useGoogleTranslate"))) {
            WatsonTranslation test = new WatsonTranslation(
                    getMainConfig().getString("Translator.watsonAPIKey"),
                    getMainConfig().getString("Translator.watsonURL"),
                    main);
                test.testConnection();
            main.setTranslatorName("Watson");
        } else if (getMainConfig().getBoolean("Translator.useGoogleTranslate") && !(getMainConfig().getBoolean("Translator.useWatsonTranslate"))) {
            //under construction, use watson
            main.getServer().getPluginManager().disablePlugin(main);
            return false;
        } else {
            main.getLogger().severe(getMessagesConfig().getString("Messages.wwcConfigInvalidTranslatorSettings"));
            main.getServer().getPluginManager().disablePlugin(main);
            return false;
        }
        
          //Cache Settings
        if (getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
            main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigCacheEnabled").replace("%i", "" + getMainConfig().getInt("Translator.translatorCacheSize")));
        } else {
            main.getLogger().warning(ChatColor.YELLOW + getMessagesConfig().getString("Messages.wwcConfigCacheDisabled"));
        }
        return true; // We made it, everything set successfully; return false == fatal error, plugin should disable after
    }
    
    /* Getters */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getMessagesFile() {
        return messagesFile;
    }
}