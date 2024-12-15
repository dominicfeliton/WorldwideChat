package com.dominicfeliton.worldwidechat;

import com.comphenix.protocol.ProtocolLibrary;
import com.dominicfeliton.worldwidechat.listeners.*;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.SpigotTaskWrapper;
import net.milkbowl.vault.chat.Chat;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitWorker;

import java.util.LinkedList;
import java.util.Queue;

public class SpigotWorldwideChatHelper extends WorldwideChatHelper {

    WorldwideChat main;

    CommonRefs refs;

    ServerAdapterFactory adapter;

    private final Queue<Listener> listenerQueue = new LinkedList<>();

    public SpigotWorldwideChatHelper() {
        super();
        this.main = WorldwideChat.instance;
        this.refs = main.getServerFactory().getCommonRefs();
        this.adapter = main.getServerFactory();
    }

    // TODO: Make most of this logic common between us and Paper and derivs
    @Override
    public void checkVaultSupport() {
        // Skip if config says so
        if (!main.isVaultSupport()) {
            main.setChat(null);
            return;
        }
        ;

        // Check if vault is installed at all
        if (main.getServer().getPluginManager().getPlugin("Vault") == null) {
            main.setChat(null);
            main.getLogger().warning(refs.getPlainMsg("wwcNoVaultPlugin"));
            return;
        }

        // Attempt to register vault chat
        RegisteredServiceProvider<Chat> rsp = main.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null && rsp.getProvider() != null) {
            main.setChat(rsp.getProvider());
            main.getLogger().info(refs.getPlainMsg("wwcVaultChatProviderFound",
                    rsp.getProvider().getName(),
                    "&d"));
        } else {
            main.setChat(null);
            main.getLogger().warning(refs.getPlainMsg("wwcNoVaultChatProvider"));
        }
    }

    // TODO: Generalize this between spigot/paper
    @Override
    public void registerEventHandlers() {
        // Unregister all previously registered listeners for this plugin
        while (!listenerQueue.isEmpty()) {
            Listener listener = listenerQueue.poll();
            HandlerList.unregisterAll(listener);
        }
        if (main.getProtocolManager() != null) {
            main.getProtocolManager().removePacketListeners(main);
        }

        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        SpigotChatListener chat = new SpigotChatListener();
        pluginManager.registerEvent(
                AsyncPlayerChatEvent.class,
                chat,
                main.getChatPriority(),
                (listener, event) -> {
                    ((SpigotChatListener) listener).onPlayerChat((AsyncPlayerChatEvent) event);
                },
                main
        );
        listenerQueue.add(chat);

        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.20")) >= 0) {
            SignListener sign = new SignListener();
            pluginManager.registerEvents(new SignListener(), main);
            listenerQueue.add(sign);
        }

        NotifsOnJoinListener join = new NotifsOnJoinListener();
        pluginManager.registerEvents(join, main);
        listenerQueue.add(join);

        SpigotPlayerLocaleListener locale = new SpigotPlayerLocaleListener();
        pluginManager.registerEvents(locale, main);
        listenerQueue.add(locale);

        TranslateInGameListener translate = new TranslateInGameListener();
        pluginManager.registerEvents(translate, main);
        listenerQueue.add(translate);

        ConfigListener inv = new ConfigListener();
        pluginManager.registerEvents(inv, main);
        listenerQueue.add(inv);

        // ProtocolLib Setup
        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.19")) >= 0 &&
                main.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            main.getLogger().info("DEBUG: PROTOCOL ENABLED!");
            main.setProtocolManager(ProtocolLibrary.getProtocolManager());

            // Register listeners
            new ChatPacketListener();
        }

        main.getLogger().info(refs.getPlainMsg("wwcListenersInitialized",
                "",
                "&d"));
    }

    @Override
    public void cleanupTasks() {
        // Cancel + remove all tasks
        // Remember that the scheduler is thread safe on Bukkit, dork
        main.getServer().getScheduler().cancelTasks(main);
    }

    /**
     * Get active asynchronous tasks
     *
     * @return int - Number of active async tasks
     */
    private int getActiveAsyncTasks() {
        return getActiveAsyncTasks(-1);
    }

    /**
     * Get active asynchronous tasks (excluding a provided one)
     *
     * @param excludedId - Task ID to be excluded from this count
     * @return int - Number of active async tasks, excluding excludedId
     */
    private int getActiveAsyncTasks(int excludedId) {
        int workers = 0;
        if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
            for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
                if (worker.getOwner().equals(main) && worker.getTaskId() != excludedId) {
                    workers++;
                }
            }
        }
        return workers;
    }

    @Override
    public void runAsync(GenericRunnable in, SchedulerType schedulerType) {
        runAsync(true, in, schedulerType, null);
    }

    @Override
    public void runAsync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        refs.debugMsg("We are an async runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "!");


        if (!serverMustBeRunning) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(main, in, delay)));
            return;
        }

        if (!refs.serverIsStopping()) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLaterAsynchronously(main, in, delay)));
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        refs.debugMsg("We are an async runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "! Repeat: " + repeatTime + "!");

        if (!serverMustBeRunning) {
            // If server does not need to be running
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(main, in, delay, repeatTime)));
            return;
        }

        // If it does
        if (!refs.serverIsStopping()) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimerAsynchronously(main, in, delay, repeatTime)));
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(GenericRunnable in, SchedulerType schedulerType) {
        runSync(true, in, schedulerType, null);
    }

    @Override
    public void runSync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        refs.debugMsg("We are a sync runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "!");

        if (!serverMustBeRunning) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLater(main, in, delay)));
            return;
        }

        if (!refs.serverIsStopping()) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskLater(main, in, delay)));
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        refs.debugMsg("We are a sync runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "! Repeat: " + repeatTime + "!");

        if (!serverMustBeRunning) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimer(main, in, delay, repeatTime)));
            return;
        }

        if (!refs.serverIsStopping()) {
            in.setTask(new SpigotTaskWrapper(Bukkit.getScheduler().runTaskTimer(main, in, delay, repeatTime)));
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}
