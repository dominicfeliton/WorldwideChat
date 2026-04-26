package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.storage.JdbcStorageUtils;
import com.dominicfeliton.worldwidechat.util.storage.PostgresUtils;
import com.dominicfeliton.worldwidechat.util.storage.SQLUtils;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcStorageUtilsTest extends WWCIntegrationTest {

    @Test
    void mysqlConnectsDisconnectsAndReconnects() throws Exception {
        StorageBackend.MYSQL.assertPortReady();
        assertConnectDisconnectReconnect(new SQLUtils("localhost", "3306", "cooldatabase",
                "root", "password", List.of(), false));
    }

    @Test
    void postgresConnectsDisconnectsAndReconnects() throws Exception {
        StorageBackend.POSTGRES.assertPortReady();
        assertConnectDisconnectReconnect(new PostgresUtils("localhost", "5432", "cooldatabase",
                "admin", "password", List.of(), false));
    }

    @Test
    void failedMysqlConnectionDoesNotRemainConnected() {
        StorageBackend.MYSQL.assertPortReady();
        SQLUtils sql = new SQLUtils("localhost", "3306", "cooldatabase",
                "root", "wrong-password", List.of(), false);

        assertThrows(SQLException.class, sql::connect);
        assertFalse(sql.isConnected());
        sql.disconnect();
        assertFalse(sql.isConnected());
    }

    @Test
    void failedPostgresConnectionDoesNotRemainConnected() {
        StorageBackend.POSTGRES.assertPortReady();
        PostgresUtils postgres = new PostgresUtils("localhost", "5432", "cooldatabase",
                "admin", "wrong-password", List.of(), false);

        assertThrows(SQLException.class, postgres::connect);
        assertFalse(postgres.isConnected());
        postgres.disconnect();
        assertFalse(postgres.isConnected());
    }

    private void assertConnectDisconnectReconnect(JdbcStorageUtils storage) throws Exception {
        assertFalse(storage.isConnected());

        storage.connect();
        assertTrue(storage.isConnected());
        try (Connection connection = storage.getConnection()) {
            assertFalse(connection.isClosed());
        }

        storage.disconnect();
        assertFalse(storage.isConnected());

        storage.connect();
        assertTrue(storage.isConnected());
        storage.disconnect();
        assertFalse(storage.isConnected());
    }
}
