package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import org.mockbukkit.mockbukkit.ServerMock;

import java.sql.SQLException;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.fail;

public class TestCommon {

    public static void reload(ServerMock server, WorldwideChat plugin) {
        reload(server, plugin, false);
    }

    public static void reload(ServerMock server, WorldwideChat plugin, boolean wipe) {
        if (wipe) {
            try {
                DataStorageUtils.fullDataWipe();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            plugin.getCache().invalidateAll();
            plugin.getCache().cleanUp();
            plugin.getActiveTranslators().clear();
            plugin.getPlayerRecords().clear();
        }
        try {
            DataStorageUtils.syncData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        plugin.cancelBackgroundTasks(false);
        plugin.onEnable();

    }

    public static void waitForCondition(BooleanSupplier condition, long timeoutMillis, long checkIntervalMillis) {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                fail("Condition was not met within the timeout period.");
            }
            try {
                Thread.sleep(checkIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted while waiting for condition.");
            }
        }
    }

}
