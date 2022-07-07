package com.expl0itz.worldwidechat.util.storage;

import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.expl0itz.worldwidechat.util.CommonDefinitions;
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
	
	public static void connect(String host, String port, String databaseName, String username, String password, List<String> list) throws MongoException {
		/* Setup valid mongoDB URL */
		String url = "mongodb://" + username + ":" + password + "@" + host + ":" + port;
		if (list != null && list.size() > 0) {
			url += "/?" + list.get(0);
			list.remove(0);
			for (String eaArg : list) {
				url += "&" + eaArg.substring(0, eaArg.indexOf("=")) + "=" + eaArg.substring(eaArg.indexOf("=")+1);
				CommonDefinitions.sendDebugMessage(eaArg.substring(0, eaArg.indexOf("=")) + ":" + eaArg.substring(eaArg.indexOf("=")+1));
			}
		}
		MongoClient testClient = MongoClients.create(url);
		
		/* Attempt connection, test with ping command */
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		Bson command = new BsonDocument("ping", new BsonInt64(1));
        Document commandResult = database.runCommand(command);
		
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
