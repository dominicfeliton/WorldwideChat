package com.expl0itz.worldwidechat.util.storage;

import java.util.List;

import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoDBUtils {

	private static MongoClient mongoClient;
	
	public static boolean isConnected() {
		return (mongoClient == null ? false : true);
	}
	
	public static MongoClient getConnection() {
		return mongoClient;
	}
	
	public static void connect(String host, String port, String database, String username, String password, List<String> list) {
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
		//testClient
		
		mongoClient = testClient;
	}
	
}
