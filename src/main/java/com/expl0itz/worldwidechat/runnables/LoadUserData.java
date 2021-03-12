package com.expl0itz.worldwidechat.runnables;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

public class LoadUserData implements Runnable{

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @Override
    public void run() 
    {
        /* Load all saved user data */
        File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
		userDataFolder.mkdir();
        File[] listOfFiles = userDataFolder.listFiles();
        
        /* Add each to Translator Array in main class */
        CommonDefinitions defs = new CommonDefinitions();
        int invalidConfigs = 0;
        for (File eaFile : listOfFiles) {
            /* Make sure file is valid */
            FileConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
            if ((currFileConfig.getString("inLang").equalsIgnoreCase("None") || defs.isSupportedLangForSource(currFileConfig.getString("inLang"), main.getTranslatorName())
                    && (defs.isSupportedLangForTarget(currFileConfig.getString("outLang"), main.getTranslatorName())))) { //If file has proper entries
                main.addActiveTranslator(new ActiveTranslator(eaFile.getName().substring(0, eaFile.getName().indexOf(".")), //add active translator to arraylist
                        currFileConfig.getString("inLang"),
                        currFileConfig.getString("outLang"),
                        false));    
            } else { //Invalid file; delete it
                eaFile.delete();
                invalidConfigs++;
            }
        }
        if (invalidConfigs > 0) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUserDataCorrupted").replace("%i", invalidConfigs + ""));
        }
    }
}
