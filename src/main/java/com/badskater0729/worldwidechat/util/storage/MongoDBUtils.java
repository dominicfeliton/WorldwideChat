package com.badskater0729.worldwidechat.util.storage;

import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtils {

	private static MongoClient mongoClient;
	
	public static boolean isConnected() {
		return (mongoClient == null ? false : true);
	}
	
	public static MongoClient getConnection() {
		return mongoClient;
	}
	
	public static MongoDatabase getActiveDatabase() {
		return mongoClient.getDatabase(WorldwideChat.instance.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"));
	}
	
	public static void connect(String host, String port, String databaseName, String username, String password, List<String> list) throws MongoException {
		/* Setup valid mongoDB URL */
		String url = "mongodb://" + username + ":" + password + "@" + host + ":" + port + "/?connectTimeoutMS=" + WorldwideChat.translatorFatalAbortSeconds*1000 + "&serverSelectionTimeoutMS=" + WorldwideChat.translatorFatalAbortSeconds*1000;
		if (list != null && list.size() > 0) {
			for (String eaArg : list) {
				url += "&" + eaArg.substring(0, eaArg.indexOf("=")) + "=" + eaArg.substring(eaArg.indexOf("=")+1);
				CommonDefinitions.sendDebugMessage(eaArg.substring(0, eaArg.indexOf("=")) + ":" + eaArg.substring(eaArg.indexOf("=")+1));
			}
		}
		MongoClient testClient = MongoClients.create(url);
		
		/* Attempt connection, test with ping command */
		MongoDatabase database = testClient.getDatabase(databaseName);
		Bson command = new BsonDocument("ping", new BsonInt64(1));
        database.runCommand(command);
		
        /* Set client */
		mongoClient = testClient;
	}
	
	public static void disconnect() {
		if (isConnected()) {
			mongoClient.close();
			mongoClient = null;
		}
	}
	
}
