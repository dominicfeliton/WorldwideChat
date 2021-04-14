package com.expl0itz.worldwidechat.misc;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import com.expl0itz.worldwidechat.WorldwideChat;

public class ActiveTranslator {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    private String playerUUID = "";
    private String inLangCode = "";
    private String outLangCode = "";
    private String rateLimitPreviousTime = "None";
    
    private boolean hasBeenShownColorCodeWarning = false;
    private boolean translatingBook = false;
    private boolean translatingSign = false;

    public ActiveTranslator(String uuid, String langIn, String langOut, boolean hasBeenShownColorCodeWarning) {
    	playerUUID = uuid;
        inLangCode = langIn;
        outLangCode = langOut;
        this.hasBeenShownColorCodeWarning = hasBeenShownColorCodeWarning;
        
        /* Add player to records if they do not exist */
        main.getPlayerRecord(uuid, true);
    }

    /* Setters */
    public void setUUID(String i) {
        playerUUID = i;
    }

    public void setInLangCode(String i) {
        inLangCode = i;
    }

    public void setOutLangCode(String i) {
        outLangCode = i;
    }
    
    public void setCCWarning(boolean i) {
        hasBeenShownColorCodeWarning = i;
    }

    public void setTranslatingBook(boolean i) {
        translatingBook = i;
    }
    
    public void setTranslatingSign(boolean i) {
        translatingSign = i;
    }
    
    public void setRateLimitPreviousTime(Instant i) {
    	rateLimitPreviousTime = i.toString();
    	File userFile = main.getConfigManager().getUserSettingsFile(playerUUID);
    	if (userFile != null) {
    		FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userFile);
    		userConfig.set("rateLimitPreviousRecordedTime", i.toString());
    		try {
				userConfig.save(userFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /* Getters */
    public String getUUID() {
        return playerUUID;
    }

    public String getInLangCode() {
        return inLangCode;
    }

    public String getOutLangCode() {
        return outLangCode;
    }

    public String getRateLimitPreviousTime() {
    	return rateLimitPreviousTime;
    }
    
    public boolean getCCWarning() {
        return hasBeenShownColorCodeWarning;
    }
    
    public boolean getTranslatingBook() {
        return translatingBook;
    }
    
    public boolean getTranslatingSign() {
        return translatingSign;
    }
}