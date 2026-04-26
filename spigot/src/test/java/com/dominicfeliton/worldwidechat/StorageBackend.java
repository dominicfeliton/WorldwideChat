package com.dominicfeliton.worldwidechat;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
        config.set("Storage.useSQL", false);
        config.set("Storage.useMongoDB", false);
        config.set("Storage.usePostgreSQL", false);

        config.set("Storage.sqlHostname", "localhost");
        config.set("Storage.sqlPort", 3306);
        config.set("Storage.sqlDatabaseName", "cooldatabase");
        config.set("Storage.sqlUsername", "root");
        config.set("Storage.sqlPassword", "password");
        config.set("Storage.sqlUseSSL", false);

        config.set("Storage.mongoHostname", "localhost");
        config.set("Storage.mongoPort", 27017);
        config.set("Storage.mongoDatabaseName", "cooldatabase");
        config.set("Storage.mongoUsername", "admin");
        config.set("Storage.mongoPassword", "password");
        config.set("Storage.mongoOptionalArgs", new String[]{"authSource=admin"});

        config.set("Storage.postgresHostname", "localhost");
        config.set("Storage.postgresPort", 5432);
        config.set("Storage.postgresDatabaseName", "cooldatabase");
        config.set("Storage.postgresUsername", "admin");
        config.set("Storage.postgresPassword", "password");
        config.set("Storage.postgresSSL", false);

        if (configKey != null) {
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
}
