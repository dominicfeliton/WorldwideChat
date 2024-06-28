package com.dominicfeliton.worldwidechat.util.storage;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class PostgresUtils {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();
    private HikariDataSource hikari;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private List<String> argList;
    private boolean ssl;


    public PostgresUtils(String host, String port, String database, String username, String password, List<String> argList, boolean ssl) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.argList = argList;
        this.ssl = ssl;
    }

    public void connect() throws SQLException {
        if (hikari != null) {
            refs.debugMsg("Already connected???");
            return;
        }

        HikariDataSource testSource = new HikariDataSource();
        testSource.setConnectionTimeout(WorldwideChat.translatorFatalAbortSeconds * 1000);
        testSource.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");

        Properties dataSourceProperties = new Properties();
        dataSourceProperties.put("serverName", host);
        dataSourceProperties.put("portNumber", port);
        dataSourceProperties.put("databaseName", database);
        dataSourceProperties.put("user", username);
        dataSourceProperties.put("password", password);
        dataSourceProperties.put("ssl", String.valueOf(ssl));

        // Custom arguments
        if (argList != null) {
            argList.stream()
                    .filter(arg -> arg.contains("="))
                    .forEach(arg -> {
                        String[] parts = arg.split("=", 2);
                        dataSourceProperties.put(parts[0], parts[1]);
                    });
        }

        dataSourceProperties.forEach((key, value) -> testSource.addDataSourceProperty((String) key, value));
        try (Connection conn = testSource.getConnection()) {
            hikari = testSource;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            hikari.close();
            hikari = null;
        }
    }

    public boolean isConnected() {
        return (hikari != null);
    }

    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

}
