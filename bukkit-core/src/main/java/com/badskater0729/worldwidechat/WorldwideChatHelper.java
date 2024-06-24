package com.badskater0729.worldwidechat;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class WorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    public void registerEventHandlers() {}

    // Scheduler Methods
    public enum SchedulerType {
        GLOBAL(Void.class),
        REGION(Void.class), // NOT IMPLEMENTED BUT SHOULD BE REGION.CLASS
        ENTITY(Entity.class),
        ASYNC(Void.class);

        private final Class<?> associatedType;

        SchedulerType(Class<?> associatedType) {
            this.associatedType = associatedType;
        }

        public Class<?> getAssociatedType() {
            return associatedType;
        }
    }

    public void runAsync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runSync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runSync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runSync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}

    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {}
}
