package com.expl0itz.worldwidechat.configuration;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;

public class WWCConfigurationHandler {
	
	private WorldwideChat main;
	private String pluginLang = "en"; //TODO: Support multiple langs
	
	private File messagesFile;
	private FileConfiguration messagesConfig;
	
	public WWCConfigurationHandler(WorldwideChat main, String l)
	{
		this.main = main;
		pluginLang = l;
	}
	
	/* Init Config Method */
	public void initConfigs()
	{
		/* Init config files */
		messagesFile = new File(main.getDataFolder(), "messages-" + pluginLang + ".yml");
		
		/* Generate config files, if they do not exist */
		if (!messagesFile.exists()) 
		{
			main.saveResource("messages-" + pluginLang + ".yml", false);
		}
		
		/* Load config files */
		messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
	}
	
	/* Getters */
	public FileConfiguration getMessagesConfig()
	{
		return messagesConfig;
	}
	
	public File getMessagesFile()
	{
		return messagesFile;
	}
}
