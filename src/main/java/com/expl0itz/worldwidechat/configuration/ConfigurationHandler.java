package com.expl0itz.worldwidechat.configuration;

import java.io.File;
import java.io.IOException;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.amazontranslate.AmazonTranslation;
import com.expl0itz.worldwidechat.googletranslate.GoogleTranslation;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
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
        CommonDefinitions defs = new CommonDefinitions();
        for (int i = 0; i < defs.getSupportedPluginLangCodes().length; i++) {
            if (defs.getSupportedPluginLangCodes()[i].equalsIgnoreCase(getMainConfig().getString("General.pluginLang"))) {
                main.setPluginLang(getMainConfig().getString("General.pluginLang"));
                main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + main.getPluginLang() + ".");
                return;
            }
        }
        
        main.getLogger().warning(ChatColor.RED + "Unable to detect a valid language in your config.yml. Defaulting to en...");
    }

    /* Init Messages Method */
    public void initMessagesConfig() {
        /* Init config file */
        messagesFile = new File(main.getDataFolder(), "messages-" + main.getPluginLang() + ".yml");

        /* Always save new lang files */
        main.saveResource("messages-" + main.getPluginLang() + ".yml", true);

        /* Load config */
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /* Load Main Settings Method */
    public boolean loadMainSettings() {
        /* Get rest of General Settings */
        //Prefix
        try {
            if (!getMainConfig().getString("General.prefixName").equalsIgnoreCase("Default")) {
                main.setPrefixName(getMainConfig().getString("General.prefixName"));
            } else {
                main.setPrefixName("WWC"); //If default the entry for prefix, interpret as WWC
            }
        } catch (Exception e) {
            main.getLogger().warning((getMessagesConfig().getString("Messages.wwcConfigInvalidPrefixSettings")));
        }
        //Rate-limit Settings
        try {
        	if (getMainConfig().getInt("Translator.rateLimit") > 0) {
        		main.setRateLimit(getMainConfig().getInt("Translator.rateLimit"));
        		main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigRateLimitEnabled").replace("%i", "" + getMainConfig().getInt("Translator.rateLimit")));
        	}
        } catch (Exception e) {
        	main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigRateLimitInvalid"));
        }
        //bStats
        main.setbStats(getMainConfig().getBoolean("General.enablebStats"));
        if (getMainConfig().getBoolean("General.enablebStats")) {
            @SuppressWarnings("unused")
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
                main.setUpdateCheckerDelay(86400);
            }
        } catch (Exception e) {
            main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigBadUpdateDelay"));
            main.setUpdateCheckerDelay(86400);
        }
        
        /* Translator Settings */
        try {
        	if (getMainConfig().getBoolean("Translator.useWatsonTranslate") && (!(getMainConfig().getBoolean("Translator.useGoogleTranslate")) && (!(getMainConfig().getBoolean("Translator.useAmazonTranslate"))))) {
        		main.setTranslatorName("Watson");
        		WatsonTranslation test = new WatsonTranslation(
                    getMainConfig().getString("Translator.watsonAPIKey"),
                    getMainConfig().getString("Translator.watsonURL"));
                test.initializeConnection();
            } else if (getMainConfig().getBoolean("Translator.useGoogleTranslate") && (!(getMainConfig().getBoolean("Translator.useWatsonTranslate")) && (!(getMainConfig().getBoolean("Translator.useAmazonTranslate"))))) {
            	main.setTranslatorName("Google Translate");
            	GoogleTranslation test = new GoogleTranslation(
                    getMainConfig().getString("Translator.googleTranslateAPIKey"));
                test.initializeConnection();
            } else if (getMainConfig().getBoolean("Translator.useAmazonTranslate") && (!(getMainConfig().getBoolean("Translator.useGoogleTranslate")) && (!(getMainConfig().getBoolean("Translator.useWatsonTranslate"))))) {
            	main.setTranslatorName("Amazon Translate");
            	AmazonTranslation test = new AmazonTranslation(
            			getMainConfig().getString("Translator.amazonAccessKey"),
            			getMainConfig().getString("Translator.amazonSecretKey"),
            			getMainConfig().getString("Translator.amazonRegion"));
            	test.initializeConnection();
            } else {
                getMainConfig().set("Translator.useWatsonTranslate", false);
                getMainConfig().set("Translator.useGoogleTranslate", false);
                getMainConfig().set("Translator.useAmazonTranslate", false);
                getMainConfig().save(configFile);
                main.setTranslatorName("Invalid");
                //return false;
            }
        } catch (Exception e) {
        	main.getLogger().severe("(" + main.getTranslatorName() + ") " + e.getMessage());
        	e.printStackTrace();
        	main.setTranslatorName("Invalid");
        	//return false;
        }
        
        //Cache Settings
        main.getCache().clear();
        if (getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
            main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigCacheEnabled").replace("%i", "" + getMainConfig().getInt("Translator.translatorCacheSize")));
        } else {
            main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigCacheDisabled"));
        }
        return true; // We made it, everything set successfully; return false == fatal error and plugin disables itself
    }
 
    /* Per User Settings Saver */
    public void createUserDataConfig(ActiveTranslator inTranslator) {
        File userSettingsFile;
        FileConfiguration userSettingsConfig;
        userSettingsFile = new File(main.getDataFolder() + File.separator + "data" + File.separator, inTranslator.getUUID() + ".yml");

        /* Load config */
        userSettingsConfig = YamlConfiguration.loadConfiguration(userSettingsFile);
        
        /* Set data */
        try {
            userSettingsConfig.createSection("inLang");
            userSettingsConfig.set("inLang", inTranslator.getInLangCode());
            
            userSettingsConfig.createSection("outLang");
            userSettingsConfig.set("outLang", inTranslator.getOutLangCode());
            
            userSettingsConfig.createSection("bookTranslation");
            userSettingsConfig.set("bookTranslation", inTranslator.getTranslatingBook());
            
            userSettingsConfig.createSection("signTranslation");
            userSettingsConfig.set("signTranslation", inTranslator.getTranslatingBook());
           
            userSettingsConfig.createSection("rateLimit");
            userSettingsConfig.set("rateLimit", inTranslator.getRateLimit());
            
            userSettingsConfig.createSection("rateLimitPreviousRecordedTime");
            userSettingsConfig.set("rateLimitPreviousRecordedTime", inTranslator.getRateLimitPreviousTime());
            
            userSettingsConfig.save(userSettingsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /* Stats File Creator */
    public void createStatsConfig(PlayerRecord inRecord) {
        File userStatsFile;
        FileConfiguration userStatsConfig;
        userStatsFile = new File(main.getDataFolder() + File.separator + "stats" + File.separator, inRecord.getUUID() + ".yml");
        
        /* Load config */
        userStatsConfig = YamlConfiguration.loadConfiguration(userStatsFile);
        
        /* Set data */
        try {
            userStatsConfig.createSection("lastTranslationTime");
            userStatsConfig.set("lastTranslationTime", inRecord.getLastTranslationTime());
            
            userStatsConfig.createSection("attemptedTranslations");
            userStatsConfig.set("attemptedTranslations", inRecord.getAttemptedTranslations());
            
            userStatsConfig.createSection("successfulTranslations");
            userStatsConfig.set("successfulTranslations", inRecord.getSuccessfulTranslations());
            
            userStatsConfig.save(userStatsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /* Getters */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public File getUserSettingsFile(String uuid) {
        File outFile = new File(main.getDataFolder() + File.separator + "data" + File.separator, uuid + ".yml");
        if (outFile.exists()) {
            return outFile;
        }
        return null;
    }
    
    public File getStatsFile(String uuid) {
        File outFile = new File(main.getDataFolder() + File.separator + "stats" + File.separator, uuid + ".yml");
        if (outFile.exists()) {
            return outFile;
        }
        return null;
    }
    
    public File getConfigFile() {
        return configFile;
    }

    public File getMessagesFile() {
        return messagesFile;
    }
}