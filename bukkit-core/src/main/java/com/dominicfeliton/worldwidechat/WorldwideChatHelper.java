package com.dominicfeliton.worldwidechat;

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

    public void cleanupTasks(int taskID) {}

    public void runAsync(Runnable in, SchedulerType schedulerType) {}

    public void runAsync(Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runSync(Runnable in, SchedulerType schedulerType) {}

    public void runSync(Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runSync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runSync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object[] schedulerObj) {}

    // TODO:
    /*
    record Task(Object wrapped, Consumer<Object> canceller) {
        void cancel() {
            this.canceller.accept(this.wrapped);
        }

        static Task wrapBukkit(final BukkitRunnable runnable) {
            return new Task(runnable, task -> ((BukkitRunnable) task).cancel());
        }

        static Task wrapFolia(final ScheduledTask scheduledTask) {
            return new Task(scheduledTask, task -> ((ScheduledTask) task).cancel());
        }
    }
    */
}
