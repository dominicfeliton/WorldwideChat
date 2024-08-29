package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.bukkit.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager {
    // This task is not necessary right now.
    // Keeping it as it may be in the future if in-depth task management is ever needed.

    /*
    private final Set<CommonTask> taskRegistry = ConcurrentHashMap.newKeySet(); // Thread-safe set
    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    public void registerTask(CommonTask task) {
        taskRegistry.add(task);
    }

    public void unregisterTask(CommonTask task) {
        taskRegistry.remove(task);
    }

    public int getActiveTaskCount() {
        return taskRegistry.size();
    }

    public void waitForAndClearTasks(CommonTask taskToIgnore) {
        // TODO: Remove debug msgs
        refs.debugMsg("Running tasks before cancel calls: " + main.getServer().getScheduler().getActiveWorkers().size());
        final long timeoutMillis = WorldwideChat.asyncTasksTimeoutSeconds * 1000L;
        final long startTime = System.currentTimeMillis();

        for (CommonTask task : taskRegistry) {
            if (task != taskToIgnore) {
                task.cancel();
            } else {
                refs.debugMsg("Not killing current task :)");
            }
        }
        refs.debugMsg("TaskRegistry before clear: " + getActiveTaskCount());
        refs.debugMsg("Running tasks after cancel calls: " + main.getServer().getScheduler().getActiveWorkers().size());
        taskRegistry.clear();
    }

    public void killAlLTasks(CommonTask taskToIgnore) {
        refs.debugMsg("Killing tasks!");

    }
     */
}
