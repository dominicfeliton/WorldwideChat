package com.badskater0729.worldwidechat.runnables;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.threeten.bp.Instant;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import com.badskater0729.worldwidechat.util.storage.MongoDBUtils;
import com.badskater0729.worldwidechat.util.storage.SQLUtils;
import com.mongodb.MongoCommandException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.isSupportedTranslatorLang;
import static com.badskater0729.worldwidechat.util.CommonRefs.getSupportedTranslatorLang;

public class LoadUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public void run() {
		//TODO: Sanitize for bad inputs/accommodate for obj upgrades; if data is bad, we definitely shouldn't add it
		/* Load all saved user data */
		debugMsg("Starting LoadUserData!!!");
		File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
		File statsFolder = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
		userDataFolder.mkdir();
		statsFolder.mkdir();

		/* Load user records (/wwcs) */
		debugMsg("Loading user records or /wwcs...");
		if (SQLUtils.isConnected()) {
			try {
				/* Create tables if they do not exist already */
				Connection sqlConnection = SQLUtils.getConnection();
				PreparedStatement initActiveTranslators = sqlConnection.prepareStatement("CREATE TABLE IF NOT EXISTS activeTranslators "
						+ "(creationDate VARCHAR(256),playerUUID VARCHAR(100),inLangCode VARCHAR(12),outLangCode VARCHAR(12),rateLimit VARCHAR(256),"
						+ "rateLimitPreviousTime VARCHAR(256),translatingChatOutgoing VARCHAR(12), translatingChatIncoming VARCHAR(12),"
						+ "translatingBook VARCHAR(12),translatingSign VARCHAR(12),translatingItem VARCHAR(12),translatingEntity VARCHAR(12),PRIMARY KEY (playerUUID))");
				initActiveTranslators.executeUpdate();
				initActiveTranslators.close();
				PreparedStatement initPlayerRecords = sqlConnection.prepareStatement("CREATE TABLE IF NOT EXISTS playerRecords "
						+ "(creationDate VARCHAR(256),playerUUID VARCHAR(100),attemptedTranslations VARCHAR(256),successfulTranslations VARCHAR(256),"
						+ "lastTranslationTime VARCHAR(256),PRIMARY KEY (playerUUID))");
				initPlayerRecords.executeUpdate();
				initPlayerRecords.close();
				
				// Load PlayerRecord using SQL
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
		} else if (MongoDBUtils.isConnected()) {
			/* Initialize collections, create if they do not exist */
			MongoDatabase database = MongoDBUtils.getActiveDatabase();
			try {
				database.createCollection("ActiveTranslators");
				database.createCollection("PlayerRecords");
			} catch (MongoCommandException e) {}
			MongoCollection<Document> playerRecordCol = database.getCollection("PlayerRecords");
			
			// Load PlayerRecord using MongoDB
			FindIterable<Document> iterDoc = playerRecordCol.find();
			Iterator<Document> it = iterDoc.iterator();
			while (it.hasNext()) {
				Document currDoc = it.next();
				PlayerRecord recordToAdd = new PlayerRecord(
						currDoc.getString("lastTranslationTime"),
						currDoc.getString("playerUUID"),
						currDoc.getInteger("attemptedTranslations"),
						currDoc.getInteger("successfulTranslations")
						);
				recordToAdd.setHasBeenSaved(true);
				main.addPlayerRecord(recordToAdd);
			}
		} else {
			for (File eaFile : statsFolder.listFiles()) {
				// Load current file
				YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
				try {
					Reader currConfigStream = new InputStreamReader(main.getResource("default-player-record.yml"), "UTF-8");
					currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				currFileConfig.options().copyDefaults(true);
				
				// Create new PlayerRecord from current file
				main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
				PlayerRecord currRecord = new PlayerRecord(currFileConfig.getString("lastTranslationTime"),
						eaFile.getName().substring(0, eaFile.getName().indexOf(".")),
						currFileConfig.getInt("attemptedTranslations"),
						currFileConfig.getInt("successfulTranslations"));
				currRecord.setHasBeenSaved(true);
				main.addPlayerRecord(currRecord);
			}
		}

		/* If translator settings are invalid, do not do anything else... */
		if (main.getTranslatorName().equalsIgnoreCase("Invalid")) {
			return;
		}

		/* Load user files (last translation session, etc.) */
		debugMsg("Loading user data or /wwct...");
		if (SQLUtils.isConnected()) {
			try {
				// Load ActiveTranslator using SQL
				ResultSet rs = SQLUtils.getConnection().createStatement().executeQuery("SELECT * FROM activeTranslators");
				while (rs.next()) {
					String inLang = rs.getString("inLangCode");
					String outLang = rs.getString("outLangCode");
					if (!validLangCodes(inLang, outLang)) {
						inLang = "en";
						outLang = "es";
					}
					ActiveTranslator translatorToAdd = new ActiveTranslator(
							rs.getString("playerUUID"),
							inLang,
							outLang
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
		} else if (MongoDBUtils.isConnected()) {
			// Load Active Translator using MongoDB
			MongoDatabase database = MongoDBUtils.getActiveDatabase();
			MongoCollection<Document> activeTranslatorCol = database.getCollection("ActiveTranslators");
			FindIterable<Document> iterDoc = activeTranslatorCol.find();
			Iterator<Document> it = iterDoc.iterator();
			while (it.hasNext()) {
				Document currDoc = it.next();
				String inLang = currDoc.getString("inLangCode");
				String outLang = currDoc.getString("outLangCode");
				if (!validLangCodes(inLang, outLang)) {
					inLang = "en";
					outLang = "es";
				}
				ActiveTranslator translatorToAdd = new ActiveTranslator(
						currDoc.getString("playerUUID"),
						inLang,
						outLang
						);
				if (!currDoc.getString("rateLimitPreviousTime").equalsIgnoreCase("None")) {
					translatorToAdd.setRateLimitPreviousTime(Instant.parse(currDoc.getString("rateLimitPreviousTime")));
				}
				translatorToAdd.setTranslatingChatOutgoing(currDoc.getBoolean("translatingChatOutgoing"));
				translatorToAdd.setTranslatingChatIncoming(currDoc.getBoolean("translatingChatIncoming"));
				translatorToAdd.setTranslatingBook(currDoc.getBoolean("translatingBook"));
				translatorToAdd.setTranslatingSign(currDoc.getBoolean("translatingSign"));
				translatorToAdd.setTranslatingItem(currDoc.getBoolean("translatingItem"));
				translatorToAdd.setTranslatingEntity(currDoc.getBoolean("translatingEntity"));
				translatorToAdd.setRateLimit(currDoc.getInteger("rateLimit"));
				translatorToAdd.setHasBeenSaved(true);
				main.addActiveTranslator(translatorToAdd);
			}
		} else {
			for (File eaFile : userDataFolder.listFiles()) {
				// Load current user translation file
				YamlConfiguration currFileConfig = YamlConfiguration.loadConfiguration(eaFile);
				try {
					Reader currConfigStream = new InputStreamReader(main.getResource("default-active-translator.yml"), "UTF-8");
					currFileConfig.setDefaults(YamlConfiguration.loadConfiguration(currConfigStream));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				currFileConfig.options().copyDefaults(true);
				
				/* Sanity checks on inLang and outLang */
				String inLang = currFileConfig.getString("inLang");
				String outLang = currFileConfig.getString("outLang");
				if (!validLangCodes(inLang, outLang)) {
					currFileConfig.set("inLang", "en");
					currFileConfig.set("outLang", "es");
					
					main.getConfigManager().saveCustomConfig(currFileConfig, eaFile, false);
				}
				
				// Create new ActiveTranslator with current file data
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
		}
		main.getLogger().info(ChatColor.LIGHT_PURPLE
				+ getMsg("wwcUserDataReloaded"));
	}
	
	private boolean validLangCodes(String inLang, String outLang) {
		// If inLang is invalid, or None is associated with Amazon Translate
		if ((!inLang.equalsIgnoreCase("None") && !isSupportedTranslatorLang(inLang, "in"))
				|| (inLang.equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
			return false;
		}
		// If outLang code is not supported with current translator
		if (!isSupportedTranslatorLang(outLang, "out")) {
			return false;
		}
		// If inLang and outLang codes are equal
		if (getSupportedTranslatorLang(outLang, "out").getLangCode().equals(getSupportedTranslatorLang(inLang, "in").getLangCode())) {
		    debugMsg("Langs are the same?");
			return false;
		}
		return true;
	}
}
