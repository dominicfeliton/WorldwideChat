package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.*;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitWorker;

import static com.badskater0729.worldwidechat.WorldwideChat.asyncTasksTimeoutSeconds;

public class SpigotWorldwideChatHelper extends WorldwideChatHelper {

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    @Override
    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(), main);
        if (adapter.getServerInfo().getValue().contains("1.2")) {
            pluginManager.registerEvents(new SignListener(), main);
        }
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized", null));
    }

    @Override
    public void cleanupTasks(int taskID) {
        // Cancel + remove all tasks
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
    public void runAsync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
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
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
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
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
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
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
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
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}
