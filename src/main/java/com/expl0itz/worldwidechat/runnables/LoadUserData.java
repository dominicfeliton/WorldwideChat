package com.expl0itz.worldwidechat.runnables;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.PlayerRecord;
import com.expl0itz.worldwidechat.util.SQLUtils;

public class LoadUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;

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
		if (!SQLUtils.isConnected()) {
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
		} else {
			try {
				ResultSet rs = SQLUtils.getConnection().createStatement().executeQuery("SELECT * FROM playerRecords");
				while (rs.next()) {
					PlayerRecord recordToAdd = new PlayerRecord(
							rs.getString("lastTranslationTime"),
							rs.getString("playerUUID"),
							rs.getInt("attemptedTranslations"),
							rs.getInt("successfulTranslations")
							);
					recordToAdd.setHasBeenSaved(true);
					main.addPlayerRecord(recordToAdd);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/* If translator settings are invalid, do not do anything else... */
		if (main.getTranslatorName().equalsIgnoreCase("Invalid")) {
			return;
		}

		/* Load user files (last translation session, etc.) */
		CommonDefinitions.sendDebugMessage("Loading user data or /wwct...");
		if (!SQLUtils.isConnected()) {
			for (File eaFile : userDataFolder.listFiles()) {
				YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
				try {
					Reader currConfigStream = new InputStreamReader(main.getResource("default-active-translator.yml"), "UTF-8");
					currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				currFileConfig.options().copyDefaults(true);
				/* Hopefully temporary check that disables use of none with Amazon Translate until we find a workaround */
				if ((!currFileConfig.getString("inLang").equalsIgnoreCase("None") && CommonDefinitions.getSupportedTranslatorLang(currFileConfig.getString("inLang")).getLangCode().equals(""))
						|| (currFileConfig.getString("inLang").equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
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
				currentTranslator.setTranslatingChatOutgoing(currFileConfig.getBoolean("chatTranslationOutgoing"));
				currentTranslator.setTranslatingChatIncoming(currFileConfig.getBoolean("chatTranslationIncoming"));
				currentTranslator.setRateLimit(currFileConfig.getInt("rateLimit"));
				if (!currFileConfig.getString("rateLimitPreviousRecordedTime").equals("None")) {
					currentTranslator.setRateLimitPreviousTime(
							Instant.parse(currFileConfig.getString("rateLimitPreviousRecordedTime")));
				}
				currentTranslator.setHasBeenSaved(true);
				main.addActiveTranslator(currentTranslator);
			}
		} else {
			try {
				ResultSet rs = SQLUtils.getConnection().createStatement().executeQuery("SELECT * FROM activeTranslators");
				while (rs.next()) {
					ActiveTranslator translatorToAdd = new ActiveTranslator(
							rs.getString("playerUUID"),
							rs.getString("inLangCode"),
							rs.getString("outLangCode")
							);
					if (!rs.getString("rateLimitPreviousTime").equalsIgnoreCase("None")) {
						translatorToAdd.setRateLimitPreviousTime(Instant.parse(rs.getString("rateLimitPreviousTime")));
					}
					translatorToAdd.setTranslatingChatOutgoing(rs.getBoolean("translatingChatOutgoing"));
					translatorToAdd.setTranslatingChatIncoming(rs.getBoolean("translatingChatIncoming"));
					translatorToAdd.setTranslatingBook(rs.getBoolean("translatingBook"));
					translatorToAdd.setTranslatingSign(rs.getBoolean("translatingSign"));
					translatorToAdd.setTranslatingItem(rs.getBoolean("translatingItem"));
					translatorToAdd.setTranslatingEntity(rs.getBoolean("translatingEntity"));
					translatorToAdd.setRateLimit(rs.getInt("rateLimit"));
					translatorToAdd.setHasBeenSaved(true);
					main.addActiveTranslator(translatorToAdd);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		main.getLogger().info(ChatColor.LIGHT_PURPLE
				+ CommonDefinitions.getMessage("wwcUserDataReloaded"));
	}
}
