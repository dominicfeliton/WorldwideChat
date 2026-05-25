package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CachedTranslation;
import com.dominicfeliton.worldwidechat.util.storage.DataStorageUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class WWCTestSupport {

    private static final int DEFAULT_TRANSLATOR_CACHE_SIZE = 100;

    private static ServerMock server;
    private static WorldwideChat plugin;

    private WWCTestSupport() {
    }

    static synchronized void start() {
        if (plugin != null) {
            return;
        }

        server = MockBukkit.mock();
        plugin = MockBukkit.load(WorldwideChat.class);
        waitForPluginReady();
        configureStorage(StorageBackend.YAML);
        reload();
        assertEquals("JUnit/MockBukkit Testing Translator", plugin.getTranslatorName());
    }

    static ServerMock server() {
        start();
        return server;
    }

    static WorldwideChat plugin() {
        start();
        return plugin;
    }

    static PlayerMock addOpPlayer(String name) {
        PlayerMock player = server().addPlayer(name);
        player.setOp(true);
        return player;
    }

    static PlayerMock addPlayer(String name) {
        PlayerMock player = server().addPlayer(name);
        player.setOp(false);
        return player;
    }

    static void reset() {
        start();
        wipeCurrentStorage();
        clearRuntimeState();
        configureStorage(StorageBackend.YAML);
        reload();
    }

    static void useStorageBackend(StorageBackend backend) {
        start();
        backend.assertPortReady();
        configureStorage(backend);
        reload();
        if (!backend.isConnected(plugin)) {
            fail("Docker-backed " + backend + " storage is required for this suite. Run dev/setup-dev-env.sh before Maven tests.");
        }
        wipeCurrentStorage();
        clearRuntimeState();
        reload();
    }

    static void reload() {
        plugin.cancelBackgroundTasks(false);
        if (plugin.isEnabled()) {
            plugin.onEnable();
        } else {
            server.getPluginManager().enablePlugin(plugin);
        }
        waitForPluginReady();
        drainScheduler();
    }

    static void reloadExpectingInvalidTranslator() {
        plugin.cancelBackgroundTasks(false);
        plugin.onEnable();
        waitForCondition(() -> plugin != null && "Invalid".equals(plugin.getTranslatorName()));
        drainScheduler();
    }

    static void wipeStorageAndReload() {
        wipeCurrentStorage();
        clearRuntimeState();
        reload();
    }

    static void drainScheduler() {
        server.getScheduler().performTicks(30);
        server.getScheduler().waitAsyncTasksFinished();
        server.getScheduler().waitAsyncEventsFinished();
        server.getScheduler().performTicks(5);
        server.getScheduler().waitAsyncTasksFinished();
        server.getScheduler().waitAsyncEventsFinished();
    }

    static void waitForCondition(BooleanSupplier condition) {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > 5000) {
                fail("Condition was not met within the timeout period.");
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Thread was interrupted while waiting for condition.");
            }
        }
    }

    static void addCacheTerm(String inLang, String outLang, String inputPhrase, String outputPhrase) {
        plugin.addCacheTerm(new CachedTranslation(inLang, outLang, inputPhrase), outputPhrase);
    }

    static void configureStorage(StorageBackend backend) {
        YamlConfiguration config = plugin.getConfigManager().getMainConfig();
        backend.applyTo(config);
        config.set("Translator.testModeTranslator", true);
        config.set("Translator.useGoogleTranslate", false);
        config.set("Translator.useAmazonTranslate", false);
        config.set("Translator.useLibreTranslate", false);
        config.set("Translator.useDeepLTranslate", false);
        config.set("Translator.useAzureTranslate", false);
        config.set("Translator.useSystranTranslate", false);
        config.set("Translator.useChatGPT", false);
        config.set("Translator.useOpenAICompatible", false);
        config.set("Translator.enableGuidelinesAIChecks", false);
        config.set("Translator.guidelinesAIModel", "");
        config.set("Translator.useOllama", false);
        config.set("Translator.translatorCacheSize", DEFAULT_TRANSLATOR_CACHE_SIZE);
        config.set("General.enableDebugMode", false);
        config.set("General.enablebStats", false);
        config.set("General.syncUserLocalization", true);
        config.set("General.objectTranslationConcurrencyLimit", 4);
        config.set("General.translationCapacityLimit", 0);
        config.set("Chat.sendActionBar", true);
        config.set("Chat.separateChatChannel.force", false);
        plugin.getConfigManager().saveMainConfig(false);
    }

    static void wipeCurrentStorage() {
        try {
            DataStorageUtils.fullDataWipe();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void clearRuntimeState() {
        new ArrayList<>(server.getOnlinePlayers()).forEach(PlayerMock::disconnect);
        plugin.getCache().invalidateAll();
        plugin.getCache().cleanUp();
        plugin.getConfigManager().getMainConfig().set("Translator.translatorCacheSize", DEFAULT_TRANSLATOR_CACHE_SIZE);
        plugin.setCacheProperties(DEFAULT_TRANSLATOR_CACHE_SIZE);
        plugin.getActiveTranslators().clear();
        plugin.getPlayerRecords().clear();
    }

    static void useDirectPermissionChecks() {
        try {
            Field currPlatform = WorldwideChat.class.getDeclaredField("currPlatform");
            currPlatform.setAccessible(true);
            currPlatform.set(plugin, "Folia");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void waitForPluginReady() {
        waitForCondition(() -> plugin != null && !"Starting".equals(plugin.getTranslatorName()));
    }
}
