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
        
        /* Add default options, if they do not exist */
    	mainConfig.addDefault("General.prefixName", "WWC");
    	mainConfig.addDefault("General.enablebStats", true);
    	mainConfig.addDefault("General.pluginLang", "en");
    	mainConfig.addDefault("General.updateCheckerDelay", 86400);
    	mainConfig.addDefault("General.syncUserDataDelay", 7200);
    	
    	mainConfig.addDefault("Chat.sendTranslationChat", true);
    	mainConfig.addDefault("Chat.sendPluginUpdateChat", true);
    	
    	mainConfig.addDefault("Translator.useWatsonTranslate", true);
    	mainConfig.addDefault("Translator.watsonAPIKey", "");
    	mainConfig.addDefault("Translator.watsonURL", "");
    	mainConfig.addDefault("Translator.useGoogleTranslate", false);
    	mainConfig.addDefault("Translator.googleTranslateAPIKey", "");
    	mainConfig.addDefault("Translator.useAmazonTranslate", false);
    	mainConfig.addDefault("Translator.amazonAccessKey", "");
    	mainConfig.addDefault("Translator.amazonSecretKey", "");
    	mainConfig.addDefault("Translator.amazonRegion", "");
    	mainConfig.addDefault("Translator.translatorCacheSize", 10);
    	mainConfig.addDefault("Translator.rateLimit", 0);
        
    	mainConfig.options().copyDefaults(true);
    	try {
			mainConfig.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        /* Get plugin lang */
        for (int i = 0; i < CommonDefinitions.supportedPluginLangCodes.length; i++) {
            if (CommonDefinitions.supportedPluginLangCodes[i].equalsIgnoreCase(getMainConfig().getString("General.pluginLang"))) {
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
        //Sync User Data Delay
        try {
            if ((getMainConfig().getInt("General.syncUserDataDelay") > 10)) {
                main.setSyncUserDataDelay(getMainConfig().getInt("General.syncUserDataDelay"));
                main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigSyncDelayEnabled").replace("%i", main.getSyncUserDataDelay() + ""));
            } else {
            	main.setSyncUserDataDelay(7200);
                main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigSyncDelayInvalid"));
            }
        } catch (Exception e) {
        	main.setSyncUserDataDelay(7200);
            main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigSyncDelayInvalid"));
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
            }
        } catch (Exception e) {
        	main.getLogger().severe("(" + main.getTranslatorName() + ") " + e.getMessage());
        	e.printStackTrace();
        	main.setTranslatorName("Invalid");
        }
        //Rate-limit Settings
        try {
        	if (getMainConfig().getInt("Translator.rateLimit") > 0) {
        		main.setRateLimit(getMainConfig().getInt("Translator.rateLimit"));
        		main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigRateLimitEnabled").replace("%i", "" + main.getRateLimit()));
        	}
        } catch (Exception e) {
        	main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigRateLimitInvalid"));
        	main.setRateLimit(0);
        }
        //Cache Settings
        try {
        	if (getMainConfig().getInt("Translator.translatorCacheSize") > 0) {
                main.getLogger().info(ChatColor.LIGHT_PURPLE + getMessagesConfig().getString("Messages.wwcConfigCacheEnabled").replace("%i", "" + getMainConfig().getInt("Translator.translatorCacheSize")));
            } else {
                main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigCacheDisabled"));
                getMainConfig().set("Translator.translatorCacheSize", 10);
                try {
    				getMainConfig().save(configFile);
    			} catch (IOException e1) {
    				e1.printStackTrace();
    			}
            }
        } catch (Exception e) {
        	main.getLogger().warning(getMessagesConfig().getString("Messages.wwcConfigCacheInvalid"));
        	getMainConfig().set("Translator.translatorCacheSize", 10);
            try {
				getMainConfig().save(configFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
            userSettingsConfig.set("signTranslation", inTranslator.getTranslatingSign());
           
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
    
    public void syncData() {
    	//Sync activeTranslators to disk
        synchronized (main.getActiveTranslators()) {
        	//Save all new activeTranslators
            for (ActiveTranslator eaTranslator : main.getActiveTranslators()) {
                createUserDataConfig(eaTranslator);
            }
            //Delete any old activeTranslators
            File userSettingsDir = new File(main.getDataFolder() + File.separator + "data" + File.separator);
            for (String eaName : userSettingsDir.list()) {
            	File currFile = new File(userSettingsDir, eaName);
            	if (main.getActiveTranslator(currFile.getName().substring(0, currFile.getName().indexOf("."))) == null) {
            		currFile.delete();
            	}
            }
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