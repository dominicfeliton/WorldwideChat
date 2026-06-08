package com.dominicfeliton.worldwidechat.util.storage;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class JdbcStorageUtils {

    private final WorldwideChat main = WorldwideChat.instance;
    private final CommonRefs refs = main.getServerFactory().getCommonRefs();
    private final JdbcStorageDialect dialect;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private final List<String> argList;
    private final boolean useSSL;
    private HikariDataSource hikari;

    public JdbcStorageUtils(JdbcStorageDialect dialect, String host, String port, String database,
                            String username, String password, List<String> argList, boolean useSSL) {
        this.dialect = dialect;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.argList = argList;
        this.useSSL = useSSL;
    }

    public void connect() throws SQLException {
        if (isConnected()) {
            refs.debugMsg(dialect.getDisplayName() + " storage is already connected.");
            return;
        }
        if (hikari != null) {
            hikari.close();
            hikari = null;
        }

        HikariDataSource testSource = new HikariDataSource();
        testSource.setConnectionTimeout(WorldwideChat.translatorFatalAbortSeconds * 1000L);
        testSource.setDataSourceClassName(dialect.getDataSourceClassName());

        Properties dataSourceProperties = new Properties();
        dataSourceProperties.put("serverName", host);
        dataSourceProperties.put(dialect.getPortPropertyName(), port);
        dataSourceProperties.put("databaseName", database);
        dataSourceProperties.put("user", username);
        dataSourceProperties.put("password", password);
        dataSourceProperties.put(dialect == JdbcStorageDialect.POSTGRESQL ? "ssl" : "useSSL", String.valueOf(useSSL));

        if (argList != null) {
            argList.stream()
                    .filter(arg -> arg.contains("="))
                    .forEach(arg -> {
                        String[] parts = arg.split("=", 2);
                        dataSourceProperties.put(parts[0], parts[1]);
                    });
        }

        dataSourceProperties.forEach((key, value) -> testSource.addDataSourceProperty((String) key, value));
        try (Connection ignored = testSource.getConnection()) {
            hikari = testSource;
        } catch (SQLException e) {
            testSource.close();
            throw e;
        }
    }

    public void disconnect() {
        if (isConnected()) {
            hikari.close();
            hikari = null;
        }
    }

    public boolean isConnected() {
        return hikari != null && !hikari.isClosed();
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException(dialect.getDisplayName() + " storage is not connected.");
        }
        return hikari.getConnection();
    }

    public DataSource getDataSource() {
        return hikari;
    }

    public JdbcStorageDialect getDialect() {
        return dialect;
    }
}
