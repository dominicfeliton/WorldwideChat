package com.expl0itz.worldwidechat.configuration;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.WWCDefinitions;
import com.expl0itz.worldwidechat.watson.WWCWatson;

public class WWCConfigurationHandler {
	
	private WorldwideChat main;
	
	private File messagesFile;
	private File configFile;
	private FileConfiguration messagesConfig;
	private FileConfiguration mainConfig;
	
	public WWCConfigurationHandler(WorldwideChat main)
	{
		this.main = main;
	}
	
	/* Init Main Config Method */
	public void initMainConfig()
	{
		/* Init config file */
		configFile = new File(main.getDataFolder(), "config.yml");
		
		/* Generate config file, if it does not exist */
		if (!configFile.exists())
		{
			main.saveResource("config.yml", false);
		}
		
		/* Load main config */
		mainConfig = YamlConfiguration.loadConfiguration(configFile);
		
		/* Get plugin lang */
		String pluginLang = "en";
		WWCDefinitions defs = new WWCDefinitions();
		for (int i = 0; i < defs.getSupportedPluginLangCodes().length; i++)
		{
			if (defs.getSupportedPluginLangCodes()[i].equalsIgnoreCase(getMainConfig().getString("General.pluginLang")))
			{
				main.setPluginLang(getMainConfig().getString("General.pluginLang"));
			    main.getLogger().info(ChatColor.LIGHT_PURPLE + "Detected language " + pluginLang + ".");
			}
			else
			{
				main.getLogger().warning(ChatColor.RED + "Unable to detect a valid language in your config.yml. Defaulting to en...");
			}
		}
	}
	
	/* Init Other Configs Method */
	public void initMessagesConfig()
	{
		/* Init config files */
		messagesFile = new File(main.getDataFolder(), "messages-" + main.getPluginLang() + ".yml");
		
		/* Generate config files, if they do not exist */
		if (!messagesFile.exists()) 
		{
			main.saveResource("messages-" + main.getPluginLang() + ".yml", true);
		}
		
		/* Load configs */
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
	}
	
	/* Load Main Settings Method */
	public void loadMainSettings()
	{
		/* Get rest of General Settings */
		if (!getMainConfig().getString("General.prefixName").equalsIgnoreCase("Default"));
		{
			main.setPrefixName(getMainConfig().getString("General.prefixName"));
		}
		main.setbStats(getMainConfig().getBoolean("General.enablebStats"));
		
		/* Translator Settings */
		if (getMainConfig().getBoolean("Translator.useWatsonTranslate"))
		{
			testWatson();
			main.setTranslatorName("Watson");
		}
		else if (getMainConfig().getBoolean("Translator.useGoogleTranslate"))
		{
			//sendmsg under construction, use watson
			main.getServer().getPluginManager().disablePlugin(main);
		}
		else
		{
			//sendmsg invalid translator settings! disabling...
			main.getServer().getPluginManager().disablePlugin(main);
		}
	}
	
	/* Other Functions */
	public void testWatson()
	{
		WWCWatson test = new WWCWatson(
		    	   getMainConfig().getString("Translator.watsonAPIKey"),
		    	   getMainConfig().getString("Translator.watsonURL"), 
		    	   main);
		test.testConnection();
	}
	
	/* Getters */
	public FileConfiguration getMainConfig()
	{
		return mainConfig;
	}
	
	public FileConfiguration getMessagesConfig()
	{
		return messagesConfig;
	}
	
	public File getConfigFile()
	{
		return configFile;
	}
	
	public File getMessagesFile()
	{
		return messagesFile;
	}
}
