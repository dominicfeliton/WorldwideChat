package com.badskater0729.worldwidechat.util.storage;

import java.util.List;

import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtils {

	private CommonRefs refs = new CommonRefs();
	private String host;
	private String port;
	private String databaseName;
	private String username;
	private String password;
	private List<String> argList;

	private MongoClient client;

	public MongoDBUtils(String host, String port, String databaseName, String username, String password, List<String> argList) {
		this.host = host;
		this.port = port;
		this.databaseName = databaseName;
		this.username = username;
		this.password = password;
		this.argList = argList;
	}
	
	public void connect() throws MongoException {
		/* Check */
		if (client != null) {
			refs.debugMsg("Already connected???");
			return;
		}

		/* Setup valid mongoDB URL */
		String url = "mongodb://" + username + ":" + password + "@" + host + ":" + port + "/?connectTimeoutMS=" + WorldwideChat.translatorFatalAbortSeconds*1000 + "&serverSelectionTimeoutMS=" + WorldwideChat.translatorFatalAbortSeconds*1000;
		if (argList != null && argList.size() > 0) {
			for (String eaArg : argList) {
				url += "&" + eaArg.substring(0, eaArg.indexOf("=")) + "=" + eaArg.substring(eaArg.indexOf("=")+1);
				refs.debugMsg(eaArg.substring(0, eaArg.indexOf("=")) + ":" + eaArg.substring(eaArg.indexOf("=")+1));
			}
		}
		MongoClient testClient = MongoClients.create(url);
		
		/* Attempt connection, test with ping command */
		MongoDatabase database = testClient.getDatabase(databaseName);
		Bson command = new BsonDocument("ping", new BsonInt64(1));
        database.runCommand(command);
		
        /* Set client */
		client = testClient;
	}

	public void disconnect() {
		if (isConnected()) {
			client.close();
			client = null;
		}
	}

	public MongoClient getClient() {
		return client;
	}

	public boolean isConnected() {
		return client != null;
	}

	public MongoDatabase getActiveDatabase() {
		return client.getDatabase(WorldwideChat.instance.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"));
	}
	
}
