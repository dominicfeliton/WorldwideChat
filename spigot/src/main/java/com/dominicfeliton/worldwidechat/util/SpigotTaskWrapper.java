package com.dominicfeliton.worldwidechat.util;

import org.bukkit.scheduler.BukkitTask;

public class SpigotTaskWrapper implements CommonTask {

    private final BukkitTask bukkitTask;

    public SpigotTaskWrapper(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }

    @Override
    public int getTaskId() {
        return bukkitTask.getTaskId();
    }

    @Override
    public Object getUnderlyingTask() {
        return bukkitTask;
    }
}
