package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CommonRefs;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FoliaWorldwideChatHelper extends PaperWorldwideChatHelper {

    WorldwideChat main;

    CommonRefs refs;

    ServerAdapterFactory adapter;

    public FoliaWorldwideChatHelper() {
        super();
        this.main = WorldwideChat.instance;
        this.refs = main.getServerFactory().getCommonRefs();
        this.adapter = main.getServerFactory();
    }

    // TODO: Make sure task cancellation works on folia like in paper identical 1:1
    @Override
    public void checkVaultSupport() {
        // Vault is not supported on Folia
        main.getLogger().warning(refs.getMsg("wwcNoVaultOnFolia", null));
        main.setChat(null); // jic
    }

    // SCHEDULER (NEW)
    @Override
    public void cleanupTasks(int taskID) {
        refs.debugMsg("Folia cleanup tasks...");

        main.getServer().getGlobalRegionScheduler().cancelTasks(main);
        main.getServer().getAsyncScheduler().cancelTasks(main);
    }

    // Core method for running tasks + repeating tasks
    private void runTask(SchedulerType schedulerType, Runnable task, Object[] taskObjs, long delay, long period) {
        // TODO: Make sure /20 is correct for ticks

        // First convert to Consumer<ScheduledTask>
        Consumer<ScheduledTask> converted = (run) -> task.run();

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
                // TODO: TEST!
                if (taskObjs.length == 0) {
                    main.getLogger().severe("Requested region scheduler but did not pass a location/world! Please contact the dev.");
                    return;
                }

                if (taskObjs[0] instanceof Location) {
                    if (period > 0) {
                        main.getServer().getRegionScheduler().runAtFixedRate(main, (Location)taskObjs[0], converted, delay, period);
                    } else if (period == 0 && delay > 0) {
                        main.getServer().getRegionScheduler().runDelayed(main, (Location)taskObjs[0], converted, delay);
                    } else {
                        main.getServer().getRegionScheduler().run(main, (Location)taskObjs[0], converted);
                    }
                } else if (taskObjs[0] instanceof World) {
                    if (taskObjs.length < 3 || !(taskObjs[1] instanceof Integer) || !(taskObjs[2] instanceof Integer)) {
                        main.getLogger().severe("Requested region scheduler and passed a world but did not pass chunkX/Z! Please contact the dev.");
                        return;
                    }

                    if (period > 0) {
                        main.getServer().getRegionScheduler().runAtFixedRate(main, (World)taskObjs[0], (Integer)taskObjs[1], (Integer)taskObjs[2], converted, delay, period);
                    } else if (period == 0 && delay > 0) {
                        main.getServer().getRegionScheduler().runDelayed(main, (World)taskObjs[0], (Integer)taskObjs[1], (Integer)taskObjs[2], converted, delay);
                    } else {
                        main.getServer().getRegionScheduler().run(main, (World)taskObjs[0], (Integer)taskObjs[1], (Integer)taskObjs[2], converted);
                    }
                } else {
                    main.getLogger().severe("Requested region scheduler but did not pass a location/world! Please contact the dev.");
                    return;
                }
                break;
            case ENTITY:
                if (taskObjs.length == 0 || !(taskObjs[0] instanceof Entity)) {
                    main.getLogger().severe("Requested entity scheduler but did not pass an entity! Please contact the dev.");
                    return;
                }
                Object taskObj = taskObjs[0];

                if (period > 0) {
                    ((Entity)taskObj).getScheduler().runAtFixedRate(main, converted, null, delay, period);
                } else if (period == 0 && delay > 0) {
                    ((Entity)taskObj).getScheduler().runDelayed(main, converted, null, delay);
                } else {
                    ((Entity)taskObj).getScheduler().run(main, converted, null);
                }
                break;
            case ASYNC:
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
    public void runAsync(Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        // set repeatTime == 0 so we don't actually repeat
        runAsyncRepeating(serverMustBeRunning, delay, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
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
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    // Wrappers for compatibility with Bukkit/Spigot
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
        // No more main thread on Folia
        runAsync(serverMustBeRunning, delay, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        // No more main thread on Folia
        runAsyncRepeating(serverMustBeRunning, delay, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}