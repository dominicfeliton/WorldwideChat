package com.badskater0729.worldwidechat.runnables;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.util.*;
import java.util.stream.Collectors;

import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.storage.PostgresUtils;
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

public class LoadUserData implements Runnable {

	private WorldwideChat main = WorldwideChat.instance;
	
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private MongoDBUtils mongo = main.getMongoSession();

	private SQLUtils sql = main.getSqlSession();

	private PostgresUtils postgres = main.getPostgresSession();

	// USE LOCALLY PASSED TRANSLATOR NAME.
	private String transName;

	public LoadUserData(String transName) {
		this.transName = transName;
	}

	@Override
	public void run() {
		//TODO: Sanitize for bad inputs/accommodate for obj upgrades; if data is bad, we definitely shouldn't add it
		// TODO: Common method for SQL/Postgres
		/* Load all saved user data */
		refs.debugMsg("Starting LoadUserData!!!");
		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
		File userDataFolder = new File(main.getDataFolder() + File.separator + "data" + File.separator);
		File statsFolder = new File(main.getDataFolder() + File.separator + "stats" + File.separator);
		userDataFolder.mkdir();
		statsFolder.mkdir();

		/* Load user records (/wwcs) */
		refs.debugMsg("Loading user records or /wwcs...");
		if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
			try (Connection sqlConnection = sql.getConnection()) {
				/* Create tables if they do not exist already */
				setupTables();

				// Warn about old table structs
				refs.detectOutdatedTable("activeTranslators");
				refs.detectOutdatedTable("playerRecords");
				
				// Load PlayerRecord using SQL
				try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM playerRecords")) {
					while (rs.next()) {
						PlayerRecord recordToAdd = new PlayerRecord(
								rs.getString("lastTranslationTime"),
								rs.getString("playerUUID"),
								rs.getInt("attemptedTranslations"),
								rs.getInt("successfulTranslations")
						);
						recordToAdd.setLocalizationCode("");
						if (rs.getString("localizationCode") != null && !rs.getString("localizationCode").isEmpty() && refs.checkIfValidLocalLang(rs.getString("localizationCode"))) {
							recordToAdd.setLocalizationCode(rs.getString("localizationCode"));
						}
						recordToAdd.setHasBeenSaved(true);
						main.addPlayerRecord(recordToAdd);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB") && main.isMongoConnValid(false)) {
			/* Initialize collections, create if they do not exist */
			MongoDatabase database = mongo.getActiveDatabase();
			try {
				database.createCollection("ActiveTranslators");
				database.createCollection("PlayerRecords");
			} catch (MongoCommandException e) {
				e.printStackTrace();
			}
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
				recordToAdd.setLocalizationCode("");
				if (currDoc.getString("localizationCode") != null && !currDoc.getString("localizationCode").isEmpty()) {
					recordToAdd.setLocalizationCode(currDoc.getString("localizationCode"));
				}
				recordToAdd.setHasBeenSaved(true);
				main.addPlayerRecord(recordToAdd);
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
			try (Connection postgresConnection = postgres.getConnection()) {
				/* Create tables if they do not exist already */
				setupTables();

				// Warn about old table structs
				refs.detectOutdatedTable("activeTranslators");
				refs.detectOutdatedTable("playerRecords");

				// Load PlayerRecord using Postgres
				try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM playerRecords")) {
					while (rs.next()) {
						PlayerRecord recordToAdd = new PlayerRecord(
								rs.getString("lastTranslationTime"),
								rs.getString("playerUUID"),
								rs.getInt("attemptedTranslations"),
								rs.getInt("successfulTranslations")
						);
						recordToAdd.setLocalizationCode("");
						if (rs.getString("localizationCode") != null && !rs.getString("localizationCode").isEmpty() && refs.checkIfValidLocalLang(rs.getString("localizationCode"))) {
							recordToAdd.setLocalizationCode(rs.getString("localizationCode"));
						}
						recordToAdd.setHasBeenSaved(true);
						main.addPlayerRecord(recordToAdd);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
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
				currRecord.setLocalizationCode("");
				if (currFileConfig.getString("localizationCode") != null && !currFileConfig.getString("localizationCode").isEmpty() && refs.checkIfValidLocalLang(currFileConfig.getString("localizationCode"))) {
					currRecord.setLocalizationCode(currFileConfig.getString("localizationCode"));
				}
				currRecord.setHasBeenSaved(true);
				main.addPlayerRecord(currRecord);
			}
		}

		/* If translator settings are invalid, do not do anything else... */
		if (transName.equalsIgnoreCase("Invalid")) {
			return;
		}

		/* Load user files (last translation session, etc.) */
		refs.debugMsg("Loading user data or /wwct...");
		if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
			try (Connection sqlConnection = sql.getConnection()) {
				// Load ActiveTranslator using SQL
				try (ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
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
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false)) {
			try (Connection postgresConnection = postgres.getConnection()) {
				// Load ActiveTranslator using Postgres
				try (ResultSet rs = postgresConnection.createStatement().executeQuery("SELECT * FROM activeTranslators")) {
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
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else if (mainConfig.getBoolean("Storage.useMongoDB") && main.isMongoConnValid(false)) {
			// Load Active Translator using MongoDB
			MongoDatabase database = mongo.getActiveDatabase();
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
				+ refs.getMsg("wwcUserDataReloaded", null));
	}
	
	private boolean validLangCodes(String inLang, String outLang) {
		// If inLang is invalid, or None is associated with Amazon Translate
		if ((!inLang.equalsIgnoreCase("None") && !refs.isSupportedTranslatorLang(inLang, "in"))
				|| (inLang.equalsIgnoreCase("None") && transName.equalsIgnoreCase("Amazon Translate"))) {
			return false;
		}
		// If outLang code is not supported with current translator
		if (!refs.isSupportedTranslatorLang(outLang, "out")) {
			return false;
		}
		// If inLang and outLang codes are equal
		if (refs.getSupportedTranslatorLang(outLang, "out").getLangCode().equals(refs.getSupportedTranslatorLang(inLang, "in").getLangCode())) {
		    refs.debugMsg("Langs are the same?");
			return false;
		}
		return true;
	}

	private void setupTables() throws SQLException {
		for (Map.Entry<String, Map<String, String>> entry : CommonRefs.tableSchemas.entrySet()) {
			String tableName = entry.getKey();
			Map<String, String> tableSchema = entry.getValue();
			createOrUpdateTable(tableName, tableSchema);
		}
	}

	private void createOrUpdateTable(String tableName, Map<String, String> tableSchema) throws SQLException {
		YamlConfiguration mainConfig = main.getConfigManager().getMainConfig();
		// TODO: Check case-sensitivity on MySql on WINDOWS

		if (mainConfig.getBoolean("Storage.useSQL") && main.isSQLConnValid(false)) {
			try (Connection conn = sql.getConnection()) {
				String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
				createTableQuery += tableSchema.entrySet().stream()
						.map(column -> column.getKey() + " " + column.getValue())
						.collect(Collectors.joining(", "));
				createTableQuery += ", PRIMARY KEY (playerUUID))";
				refs.debugMsg(createTableQuery);

				try (PreparedStatement createTable = conn.prepareStatement(createTableQuery)) {
					createTable.executeUpdate();
				}

				// TODO: Test HEAVILY
				String getColumnsQuery = "SELECT COLUMN_NAME " +
						"FROM INFORMATION_SCHEMA.COLUMNS " +
						"WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = ?";
				try (PreparedStatement getColumns = conn.prepareStatement(getColumnsQuery)) {
					getColumns.setString(1, tableName);
					ResultSet columnsResult = getColumns.executeQuery();

					Set<String> existingColumns = new HashSet<>();
					while (columnsResult.next()) {
						existingColumns.add(columnsResult.getString("COLUMN_NAME"));
					}

					for (Map.Entry<String, String> column : tableSchema.entrySet()) {
						String columnName = column.getKey();
						String columnType = column.getValue();
						if (!existingColumns.contains(columnName)) {
							refs.debugMsg("Adding column " + columnName);
							String addColumnQuery = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
							try (PreparedStatement addColumn = conn.prepareStatement(addColumnQuery)) {
								addColumn.executeUpdate();
							}
						}
					}
				}
				refs.debugMsg("Done with initial table creation/modification...");
			}
		} else if (mainConfig.getBoolean("Storage.usePostgreSQL") && main.isPostgresConnValid(false))  {
			try (Connection conn = postgres.getConnection()) {
				String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
				createTableQuery += tableSchema.entrySet().stream()
						.map(column -> column.getKey() + " " + column.getValue())
						.collect(Collectors.joining(", "));
				createTableQuery += ", PRIMARY KEY (playerUUID))";
				refs.debugMsg(createTableQuery);

				try (PreparedStatement createTable = conn.prepareStatement(createTableQuery)) {
					createTable.executeUpdate();
				}

				String getColumnsQuery = "SELECT column_name " +
						"FROM information_schema.columns " +
						"WHERE table_schema = current_schema() AND table_name = ?";
				try (PreparedStatement getColumns = conn.prepareStatement(getColumnsQuery)) {
					getColumns.setString(1, tableName.toLowerCase());
					ResultSet columnsResult = getColumns.executeQuery();

					Set<String> existingColumns = new HashSet<>();
					while (columnsResult.next()) {
						existingColumns.add(columnsResult.getString("column_name").toLowerCase());
					}

					for (Map.Entry<String, String> column : tableSchema.entrySet()) {
						String columnName = column.getKey().toLowerCase();
						String columnType = column.getValue();
						if (!existingColumns.contains(columnName)) {
							refs.debugMsg("Adding column " + columnName);
							String addColumnQuery = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
							try (PreparedStatement addColumn = conn.prepareStatement(addColumnQuery)) {
								addColumn.executeUpdate();
							}
						}
					}
				}
				refs.debugMsg("Done with initial table creation/modification for PostgreSQL...");
			}
		}
	}
}
