package com.expl0itz.worldwidechat.runnables;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;
import com.expl0itz.worldwidechat.misc.PlayerRecord;
import com.google.common.io.Files;

public class LoadUserData implements Runnable{

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @Override
    public void run() 
    {
        /* Load all saved user data */
        File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
        File statsFolder = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
        File badDataFolder = new File(main.getDataFolder() + File.separator + "corrupted" + File.separator);
        badDataFolder.mkdir();
		userDataFolder.mkdir();
		statsFolder.mkdir();
        
        /* Prepare add each file to Translator Array in main class */
        int invalidConfigs = 0;
        
        /* Delete old corrupted files */
        for (File eaCorrupt: badDataFolder.listFiles()) {
            eaCorrupt.delete();
        }
        
        /* Load user records (/wwcs) */
        for (File eaFile : statsFolder.listFiles()) {
            FileConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
            if (currFileConfig.contains("attemptedTranslations")
                    && currFileConfig.contains("successfulTranslations")
                    && currFileConfig.contains("lastTranslationTime")) {
                PlayerRecord currRecord = new PlayerRecord(currFileConfig.getString("lastTranslationTime"), 
                        eaFile.getName().substring(0, eaFile.getName().indexOf(".")),
                        currFileConfig.getInt("attemptedTranslations"),
                        currFileConfig.getInt("successfulTranslations"));
                main.addPlayerRecord(currRecord);
            } else { //move corrupted files to corrupted dir; they will be deleted on next run
                try {
                    File badDataFile = new File(badDataFolder.toString() + File.separator + eaFile.getName().toString());
                    Files.move(eaFile, badDataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                invalidConfigs++;
            }
        }
        
        /* If translator settings are invalid, do not do anything else... */
    	if (main.getTranslatorName().equals("Invalid")) {
    		return;
    	}
        
        /* Load user files (last translation session, etc.)*/
        for (File eaFile : userDataFolder.listFiles()) {
            FileConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
            if ((currFileConfig.getString("inLang").equalsIgnoreCase("None") || CommonDefinitions.getSupportedTranslatorLang(currFileConfig.getString("inLang")) != null
                    && (CommonDefinitions.getSupportedTranslatorLang(currFileConfig.getString("outLang")) != null))
                    && currFileConfig.contains("signTranslation")
                    && currFileConfig.contains("bookTranslation")
                    && currFileConfig.contains("rateLimit")
                    && currFileConfig.isString("rateLimitPreviousRecordedTime")) { //If file has proper entries
                ActiveTranslator currentTranslator = new ActiveTranslator(eaFile.getName().substring(0, eaFile.getName().indexOf(".")), //add active translator to arraylist
                        currFileConfig.getString("inLang"),
                        currFileConfig.getString("outLang"),
                        false);
                currentTranslator.setTranslatingSign(currFileConfig.getBoolean("signTranslation"));
                currentTranslator.setTranslatingBook(currFileConfig.getBoolean("bookTranslation"));
                currentTranslator.setRateLimit(currFileConfig.getInt("rateLimit"));
                if (!currFileConfig.getString("rateLimitPreviousRecordedTime").equals("None")) {
                	currentTranslator.setRateLimitPreviousTime(Instant.parse(currFileConfig.getString("rateLimitPreviousRecordedTime")));
                }
                main.addActiveTranslator(currentTranslator);
            } else { //move corrupted or old files to corrupted dir; they will be deleted on next run
                try {
                    File badDataFile = new File(badDataFolder.toString() + File.separator + eaFile.getName().toString());
                    Files.move(eaFile, badDataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                invalidConfigs++;
            }
        } if (invalidConfigs > 0) {
            main.getLogger().warning(main.getConfigManager().getMessagesConfig().getString("Messages.wwcUserDataCorrupted").replace("%i", invalidConfigs + ""));
        }
        
        main.removeBackgroundTask("loadUserData");
    }
}
