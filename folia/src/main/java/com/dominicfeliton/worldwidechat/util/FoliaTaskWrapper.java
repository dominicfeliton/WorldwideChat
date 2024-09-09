package com.dominicfeliton.worldwidechat.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class FoliaTaskWrapper implements CommonTask {
    private final ScheduledTask foliaTask;

    public FoliaTaskWrapper(ScheduledTask foliaTask) {
        this.foliaTask = foliaTask;
    }

    @Override
    public void cancel() {
        foliaTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return foliaTask.isCancelled();
    }

    @Override
    public int getTaskId() {
        return -1;  // Folia does not use task IDs
    }

    @Override
    public ScheduledTask getUnderlyingTask() {
        return foliaTask;  // Return the underlying ScheduledTask
    }
}