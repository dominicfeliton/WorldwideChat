package com.dominicfeliton.worldwidechat;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.fail;

enum StorageBackend {
    YAML(null, 0),
    MYSQL("Storage.useSQL", 3306),
    POSTGRES("Storage.usePostgreSQL", 5432),
    MONGO("Storage.useMongoDB", 27017);

    private final String configKey;
    private final int port;

    StorageBackend(String configKey, int port) {
        this.configKey = configKey;
        this.port = port;
    }

    void applyTo(YamlConfiguration config) {
        String sqlDatabaseName = WWCTestSupport.storageDatabaseName(MYSQL);
        String mongoDatabaseName = WWCTestSupport.storageDatabaseName(MONGO);
        String postgresDatabaseName = WWCTestSupport.storageDatabaseName(POSTGRES);

        config.set("Storage.useSQL", false);
        config.set("Storage.useMongoDB", false);
        config.set("Storage.usePostgreSQL", false);

        config.set("Storage.sqlHostname", "localhost");
        config.set("Storage.sqlPort", 3306);
        config.set("Storage.sqlDatabaseName", sqlDatabaseName);
        config.set("Storage.sqlUsername", "root");
        config.set("Storage.sqlPassword", "password");
        config.set("Storage.sqlUseSSL", false);

        config.set("Storage.mongoHostname", "localhost");
        config.set("Storage.mongoPort", 27017);
        config.set("Storage.mongoDatabaseName", mongoDatabaseName);
        config.set("Storage.mongoUsername", "admin");
        config.set("Storage.mongoPassword", "password");
        config.set("Storage.mongoOptionalArgs", new String[]{"authSource=admin"});

        config.set("Storage.postgresHostname", "localhost");
        config.set("Storage.postgresPort", 5432);
        config.set("Storage.postgresDatabaseName", postgresDatabaseName);
        config.set("Storage.postgresUsername", "admin");
        config.set("Storage.postgresPassword", "password");
        config.set("Storage.postgresSSL", false);

        if (configKey != null) {
            prepareDatabase(WWCTestSupport.storageDatabaseName(this));
            config.set(configKey, true);
        }
    }

    void assertPortReady() {
        if (this == YAML) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 1000);
        } catch (IOException e) {
            fail("Docker-backed " + this + " storage is required for this suite. Run dev/setup-dev-env.sh before Maven tests.");
        }
    }

    boolean isConnected(WorldwideChat plugin) {
        return switch (this) {
            case YAML -> !plugin.isSQLConnValid(true) && !plugin.isMongoConnValid(true) && !plugin.isPostgresConnValid(true);
            case MYSQL -> plugin.isSQLConnValid(true);
            case POSTGRES -> plugin.isPostgresConnValid(true);
            case MONGO -> plugin.isMongoConnValid(true);
        };
    }

    void prepareDatabase(String databaseName) {
        validateDatabaseName(databaseName);
        try {
            switch (this) {
                case MYSQL -> createMysqlDatabase(databaseName);
                case POSTGRES -> createPostgresDatabase(databaseName);
                case YAML, MONGO -> {
                }
            }
        } catch (SQLException e) {
            fail("Could not prepare random " + this + " test database " + databaseName + ": " + e.getMessage());
        }
    }

    Connection adminConnection(String databaseName) throws SQLException {
        return switch (this) {
            case MYSQL -> DriverManager.getConnection("jdbc:mysql://localhost:3306/" + databaseName, "root", "password");
            case POSTGRES -> DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + databaseName, "admin", "password");
            default -> throw new IllegalArgumentException("Expected JDBC backend.");
        };
    }

    private void createMysqlDatabase(String databaseName) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "password");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
        }
    }

    private void createPostgresDatabase(String databaseName) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "admin", "password")) {
            connection.setAutoCommit(true);
            if (postgresDatabaseExists(connection, databaseName)) {
                return;
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE DATABASE " + databaseName);
            }
        }
    }

    private boolean postgresDatabaseExists(Connection connection, String databaseName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void validateDatabaseName(String databaseName) {
        if (!databaseName.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Unsafe test database name: " + databaseName);
        }
    }
}
