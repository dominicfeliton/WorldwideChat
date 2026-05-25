package com.dominicfeliton.worldwidechat.util.storage;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

public class MongoDBUtils {
    private String host;
    private String port;
    private String databaseName;
    private String username;
    private String password;
    private List<String> argList;

    private volatile MongoClient client;

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    public MongoDBUtils(String host, String port, String databaseName, String username, String password, List<String> argList) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.argList = argList;
    }

    public synchronized void connect() throws MongoException {
        /* Check */
        if (client != null && isConnected()) {
            refs.debugMsg("Already connected???");
            return;
        }
        if (client != null) {
            disconnect();
        }

        /* Setup valid mongoDB URL */
        String connectionString = "";
        if (argList != null && !argList.isEmpty()) {
            String args = argList.stream()
                    .collect(Collectors.joining("&"));

            connectionString = String.format("mongodb://%s:%s@%s:%s/?%s",
                    username, password, host, port, args);
        } else {
            connectionString = String.format("mongodb://%s:%s@%s:%s",
                    username, password, host, port);
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        MongoClient testClient = MongoClients.create(settings);

        /* Attempt connection, test with ping command */
        try {
            ping(testClient);
        } catch (RuntimeException e) {
            testClient.close();
            throw e;
        }

        /* Set client */
        client = testClient;
    }

    public synchronized void disconnect() {
        MongoClient activeClient = client;
        if (activeClient != null) {
            client = null;
            activeClient.close();
        }
    }

    public boolean isConnected() {
        MongoClient activeClient = client;
        if (activeClient == null) {
            return false;
        }
        try {
            ping(activeClient);
            return true;
        } catch (MongoException | IllegalStateException e) {
            return false;
        }
    }

    public MongoDatabase getActiveDatabase() {
        MongoClient activeClient = client;
        if (activeClient == null) {
            throw new IllegalStateException("MongoDB client is not connected.");
        }
        return activeClient.getDatabase(databaseName);
    }

    public MongoClient getClient() {
        return client;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    private void ping(MongoClient activeClient) {
        MongoDatabase database = activeClient.getDatabase(databaseName);
        Bson command = new BsonDocument("ping", new BsonInt64(1));
        database.runCommand(command);
    }

}
