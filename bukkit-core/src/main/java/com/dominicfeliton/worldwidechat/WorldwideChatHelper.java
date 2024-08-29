package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CommonTask;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import org.bukkit.entity.Panda;

public abstract class WorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    public void checkVaultSupport() {}

    public void registerEventHandlers() {}

    // Scheduler Methods
    public enum SchedulerType {
        GLOBAL,
        REGION,
        ENTITY,
        ASYNC;
    }

    public abstract void cleanupTasks();

    public abstract void runAsync(GenericRunnable in, SchedulerType schedulerType);

    public abstract void runAsync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runAsync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runAsync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runSync(GenericRunnable in, SchedulerType schedulerType);

    public abstract void runSync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runSync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runSync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);

    public abstract void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj);
}
