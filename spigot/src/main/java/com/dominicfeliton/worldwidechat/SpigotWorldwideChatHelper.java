package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.*;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.Queue;

import static com.dominicfeliton.worldwidechat.WorldwideChat.asyncTasksTimeoutSeconds;

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
        };

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

    @Override
    public void registerEventHandlers() {
        // Unregister all previously registered listeners for this plugin
        while (!listenerQueue.isEmpty()) {
            Listener listener = listenerQueue.poll();
            HandlerList.unregisterAll(listener);
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

        if (adapter.getServerInfo().getValue().contains("1.2")) {
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

        InventoryListener inv = new InventoryListener();
        pluginManager.registerEvents(inv, main);
        listenerQueue.add(inv);

        main.getLogger().info(refs.getPlainMsg("wwcListenersInitialized",
                "",
                "&d"));
    }

    @Override
    public void cleanupTasks(int taskID) {
        // Cancel + remove all tasks
        // Remember that the scheduler is thread safe on Bukkit, dork
        main.getServer().getScheduler().cancelTasks(main);

        // Wait for completion + kill all background tasks
        // Thanks to:
        // https://gist.github.com/blablubbabc/e884c114484f34cae316c48290b21d8e#file-someplugin-java-L37
        if (!main.getTranslatorName().equals("JUnit/MockBukkit Testing Translator")) {
            final long asyncTasksTimeoutMillis = (long) asyncTasksTimeoutSeconds * 1000;
            final long asyncTasksStart = System.currentTimeMillis();
            boolean asyncTasksTimeout = false;
            while (getActiveAsyncTasks(taskID) > 0) {
                // Send interrupt signal
                try {
                    for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
                        if (worker.getOwner().equals(main) && worker.getTaskId() != taskID) {
                            refs.debugMsg("Sending interrupt to task with ID " + worker.getTaskId() + "...");
                            worker.getThread().interrupt();
                        }
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    refs.debugMsg("Thread successfully aborted and threw an InterruptedException.");
                }

                // Disable once we reach timeout
                if (System.currentTimeMillis() - asyncTasksStart > asyncTasksTimeoutMillis) {
                    asyncTasksTimeout = true;
                    refs.debugMsg(
                            "Waited " + asyncTasksTimeoutSeconds + " seconds for " + this.getActiveAsyncTasks()
                                    + " remaining async tasks to complete. Disabling/reloading regardless...");
                    break;
                }
            }
            final long asyncTasksTimeWaited = System.currentTimeMillis() - asyncTasksStart;
            if (!asyncTasksTimeout && asyncTasksTimeWaited > 1) {
                refs.debugMsg("Waited " + asyncTasksTimeWaited + " ms for async tasks to finish.");
            }
        }
    }

    /**
     * Get active asynchronous tasks
     * @return int - Number of active async tasks
     */
    private int getActiveAsyncTasks() {
        return getActiveAsyncTasks(-1);
    }

    /**
     * Get active asynchronous tasks (excluding a provided one)
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
    public void runAsync(Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are an async bukkit runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "!");
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskLaterAsynchronously(main, delay);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskLaterAsynchronously(main, delay);
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are an async bukkit runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "! Repeat: " + repeatTime + "!");
        }

        if (!serverMustBeRunning) {
            // If server does not need to be running
            ((BukkitRunnable) in).runTaskTimerAsynchronously(main, delay, repeatTime);
            return;
        }

        // If it does
        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskTimerAsynchronously(main, delay, repeatTime);
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a sync bukkit runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "!");
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskLater(main, delay);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskLater(main, delay);
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a sync bukkit runnable on " + main.getCurrPlatform() + " Scheduler Type " + schedulerType + "! Delay: " + delay + "! Repeat: " + repeatTime + "!");
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskTimer(main, delay, repeatTime);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskTimer(main, delay, repeatTime);
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}
