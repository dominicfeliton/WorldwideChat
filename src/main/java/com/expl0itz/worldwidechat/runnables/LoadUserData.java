package com.expl0itz.worldwidechat.runnables;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
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
        File[] listOfFiles = userDataFolder.listFiles();
        
        /* Add each to Translator Array in main class */
        if (main.getTranslatorName().equalsIgnoreCase("Watson")) {
            List<String> list = Arrays.asList(new CommonDefinitions().getSupportedWatsonLangCodes());
            for (File eaFile : listOfFiles) {
                if (eaFile.isFile()) {
                    /* Make sure file is valid */
                    FileConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
                    if ((currFileConfig.getString("inLang").equalsIgnoreCase("None") || list.contains(currFileConfig.getString("inLang"))
                            && (list.contains(currFileConfig.getString("outLang"))))) { //If file has proper entries
                        main.addActiveTranslator(new ActiveTranslator(eaFile.getName().substring(0, eaFile.getName().indexOf(".")), //add active translator to arraylist
                                currFileConfig.getString("inLang"),
                                currFileConfig.getString("outLang"),
                                false));    
                    }
                }
            }
        }
    }

}
