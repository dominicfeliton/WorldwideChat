package com.expl0itz.worldwidechat.misc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.expl0itz.worldwidechat.WorldwideChat;

public class PlayerRecord {

    private int attemptedTranslations = 0;
    private int successfulTranslations = 0;
    
    private String lastTranslationTime = "";
    private String playerUUID = "";
    
    public PlayerRecord(String lastTranslationTime, String playerUUID, int attemptedTranslations, int successfulTranslations) {
        this.attemptedTranslations = attemptedTranslations;
        this.successfulTranslations = successfulTranslations;
        this.lastTranslationTime = lastTranslationTime;
        this.playerUUID = playerUUID;
    }
    
    /* Setters */
    public void setAttemptedTranslations(int i) {
        attemptedTranslations = i;
    }
    
    public void setSuccessfulTranslations(int i) {
        successfulTranslations = i;
    }
    
    public void setLastTranslationTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date();
        lastTranslationTime = formatter.format(date);
    }
    
    public void setUUID(String i) {
        playerUUID = i;
    }
    
    public void writeToConfig() {
        File currentConfigFile = WorldwideChat.getInstance().getConfigManager().getStatsFile(playerUUID);
        FileConfiguration userStatsConfig = YamlConfiguration.loadConfiguration(currentConfigFile);
        userStatsConfig.createSection("lastTranslationTime");
        userStatsConfig.set("lastTranslationTime", lastTranslationTime);
        
        userStatsConfig.createSection("playerUUID");
        userStatsConfig.set("playerUUID", playerUUID);
        
        userStatsConfig.createSection("attemptedTranslations");
        userStatsConfig.set("attemptedTranslations", attemptedTranslations);
        
        userStatsConfig.createSection("successfulTranslations");
        userStatsConfig.set("successfulTranslations", successfulTranslations);
        try {
            userStatsConfig.save(currentConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* Getters */
    public int getAttemptedTranslations() {
        return attemptedTranslations;
    }
    
    public int getSuccessfulTranslations() {
        return successfulTranslations;
    }
    
    public String getLastTranslationTime() {
        return lastTranslationTime;
    }
    
    public String getUUID() {
        return playerUUID;
    }
}
