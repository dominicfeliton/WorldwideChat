package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.*;
import com.badskater0729.worldwidechat.util.CommonRefs;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaWorldwideChatHelper extends PaperWorldwideChatHelper {

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    // SCHEDULER (NEW)
    @Override
    public void cleanupTasks(int taskID) {
        // TODO, check if async wait thread thing works here?
        refs.debugMsg("Folia cleanup tasks...");

        main.getServer().getGlobalRegionScheduler().cancelTasks(main);
    }

    // Core method for running tasks + repeating tasks
    private void runTask(SchedulerType schedulerType, Runnable task, Object taskObj, long delay, long period) {
        // First convert to Consumer<ScheduledTask>
        Consumer<ScheduledTask> converted = (run) -> task.run();

        // TODO: runDelayed() delay cannot be 0.
        // TODO: Check if runAtFixedRate delay can be 0.

        switch (schedulerType) {
            case GLOBAL:
                if (period > 0) {
                    main.getServer().getGlobalRegionScheduler().runAtFixedRate(main, converted, delay, period);
                } else if (period == 0 && delay > 0) {
                    main.getServer().getGlobalRegionScheduler().runDelayed(main, converted, delay);
                } else {
                    main.getServer().getGlobalRegionScheduler().run(main, converted);
                }
                break;
            case REGION:
                refs.debugMsg("Region scheduler not implemented yet. Using global scheduler.");
                if (period > 0) {
                    main.getServer().getGlobalRegionScheduler().runAtFixedRate(main, converted, delay, period);
                } else if (period == 0 && delay > 0) {
                    main.getServer().getGlobalRegionScheduler().runDelayed(main, converted, delay);
                } else {
                    main.getServer().getGlobalRegionScheduler().run(main, converted);
                }
                break;
            case ENTITY:
                if (!(taskObj instanceof Entity)) {
                    main.getLogger().severe("Requested entity scheduler but did not pass an entity! Please contact the dev.");
                    return;
                }
                if (period > 0) {
                    ((Entity)taskObj).getScheduler().runAtFixedRate(main, converted, null, delay, period);
                } else if (period == 0 && delay > 0) {
                    ((Entity)taskObj).getScheduler().runDelayed(main, converted, null, delay);
                } else {
                    ((Entity)taskObj).getScheduler().run(main, converted, null);
                }
                break;
            case ASYNC:
                // TODO: Make sure /20 is correct
                if (period > 0) {
                    main.getServer().getAsyncScheduler().runAtFixedRate(main, converted, delay/20, period/20, TimeUnit.SECONDS);
                } else if (period == 0 && delay > 0) {
                    main.getServer().getAsyncScheduler().runDelayed(main, converted, delay/20, TimeUnit.SECONDS);
                } else {
                    main.getServer().getAsyncScheduler().runNow(main, converted);
                }
                break;
        }
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
        // set repeatTime == 0 so we don't actually repeat
        runAsyncRepeating(serverMustBeRunning, delay, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        refs.debugMsg("Requesting an async task on Folia: " + schedulerType.name() + " | Delay: " + delay + " | Repeat: " + repeatTime);

        if (!serverMustBeRunning) {
            // If server does not need to be running
            runTask(schedulerType, in, schedulerObj, delay, repeatTime);
            return;
        }

        // If it does
        if (!refs.serverIsStopping()) {
            runTask(schedulerType, in, schedulerObj, delay, repeatTime);
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    // Wrappers for compatibility with Bukkit/Spigot
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
        // No more main thread on Folia
        runAsync(serverMustBeRunning, delay, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        // No more main thread on Folia
        runAsyncRepeating(serverMustBeRunning, delay, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}