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

    private MongoClient client;

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

    public void connect() throws MongoException {
        /* Check */
        if (client != null) {
            refs.debugMsg("Already connected???");
            return;
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

    public boolean isConnected() {
        return client != null;
    }

    public MongoDatabase getActiveDatabase() {
        return client.getDatabase(WorldwideChat.instance.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"));
    }

}
