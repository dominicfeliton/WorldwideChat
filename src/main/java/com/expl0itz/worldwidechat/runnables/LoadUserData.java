package com.expl0itz.worldwidechat.runnables;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.PlayerRecord;

public class LoadUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.getInstance();

	@Override
	public void run() {
		/* Load all saved user data */
		CommonDefinitions.sendDebugMessage("Starting LoadUserData!!!");
		File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
		File statsFolder = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
		userDataFolder.mkdir();
		statsFolder.mkdir();

		/* Load user records (/wwcs) */
		CommonDefinitions.sendDebugMessage("Loading user records or /wwcs...");
		for (File eaFile : statsFolder.listFiles()) {
			YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
			try {
				Reader currConfigStream = new InputStreamReader(main.getResource("default-player-record.yml"), "UTF-8");
				currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			currFileConfig.options().copyDefaults(true);
			main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
			PlayerRecord currRecord = new PlayerRecord(currFileConfig.getString("lastTranslationTime"),
					eaFile.getName().substring(0, eaFile.getName().indexOf(".")),
					currFileConfig.getInt("attemptedTranslations"),
					currFileConfig.getInt("successfulTranslations"));
			currRecord.setHasBeenSaved(true);
			main.addPlayerRecord(currRecord);
		}

		/* If translator settings are invalid, do not do anything else... */
		if (main.getTranslatorName().equalsIgnoreCase("Invalid")) {
			return;
		}

		/* Load user files (last translation session, etc.) */
		CommonDefinitions.sendDebugMessage("Loading user data or /wwct...");
		for (File eaFile : userDataFolder.listFiles()) {
			YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
			try {
				Reader currConfigStream = new InputStreamReader(main.getResource("default-active-translator.yml"), "UTF-8");
				currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			currFileConfig.options().copyDefaults(true);
			if (!currFileConfig.getString("inLang").equalsIgnoreCase("None") && CommonDefinitions.getSupportedTranslatorLang(currFileConfig.getString("inLang")).getLangCode().equals("")) {
				currFileConfig.set("inLang", "en");
			}
			if (CommonDefinitions.getSupportedTranslatorLang(currFileConfig.getString("outLang")).getLangCode().equals("")) {
				currFileConfig.set("outLang", "es");
			}
			main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
			ActiveTranslator currentTranslator = new ActiveTranslator(
					eaFile.getName().substring(0, eaFile.getName().indexOf(".")), // add active translator to arraylist
					currFileConfig.getString("inLang"), currFileConfig.getString("outLang"));
			currentTranslator.setTranslatingSign(currFileConfig.getBoolean("signTranslation"));
			currentTranslator.setTranslatingBook(currFileConfig.getBoolean("bookTranslation"));
			currentTranslator.setTranslatingItem(currFileConfig.getBoolean("itemTranslation"));
			currentTranslator.setTranslatingEntity(currFileConfig.getBoolean("entityTranslation"));
			currentTranslator.setTranslatingChat(currFileConfig.getBoolean("chatTranslation"));
			currentTranslator.setRateLimit(currFileConfig.getInt("rateLimit"));
			if (!currFileConfig.getString("rateLimitPreviousRecordedTime").equals("None")) {
				currentTranslator.setRateLimitPreviousTime(
						Instant.parse(currFileConfig.getString("rateLimitPreviousRecordedTime")));
			}
			currentTranslator.setHasBeenSaved(true);
			main.addActiveTranslator(currentTranslator);
		}
		main.getLogger().info(ChatColor.LIGHT_PURPLE
				+ CommonDefinitions.getMessage("wwcUserDataReloaded"));
	}
}
