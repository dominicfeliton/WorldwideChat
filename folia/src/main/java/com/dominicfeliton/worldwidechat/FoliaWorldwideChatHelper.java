package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CommonTask;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

public class FoliaWorldwideChatHelper extends PaperWorldwideChatHelper {
    private final FoliaSchedulerDispatcher schedulerDispatcher;

    public FoliaWorldwideChatHelper() {
        this(new FoliaSchedulerDispatcher(new BukkitFoliaSchedulerAccess(WorldwideChat.instance)));
    }

    FoliaWorldwideChatHelper(FoliaSchedulerDispatcher schedulerDispatcher) {
        this.schedulerDispatcher = schedulerDispatcher;
    }

    // SCHEDULER (NEW)
    @Override
    public void cleanupTasks() {
        refs.debugMsg("Folia cleanup tasks...");

        main.getServer().getGlobalRegionScheduler().cancelTasks(main);
        main.getServer().getAsyncScheduler().cancelTasks(main);
    }

    // Core method for running tasks + repeating tasks
    private void runTask(SchedulerType schedulerType, GenericRunnable task, Object[] taskObjs, long delay, long period) {
        CommonTask commonTask = schedulerDispatcher.dispatch(schedulerType, task, taskObjs, delay, period);
        if (commonTask != null) {
            task.setTask(commonTask);
        }
    }

    @Override
    public void runAsync(GenericRunnable in, SchedulerType schedulerType) {
        runAsync(true, in, schedulerType, null);
    }

    @Override
    public void runAsync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        // set repeatTime == 0 so we don't actually repeat
        runAsyncRepeating(serverMustBeRunning, delay, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
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
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    // Wrappers for compatibility with Bukkit/Spigot
    @Override
    public void runSync(GenericRunnable in, SchedulerType schedulerType) {
        runSync(true, in, schedulerType, null);
    }

    @Override
    public void runSync(GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, int delay, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        // No more main thread on Folia
        runAsync(serverMustBeRunning, delay, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        // No more main thread on Folia
        runAsyncRepeating(serverMustBeRunning, delay, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, GenericRunnable in, SchedulerType schedulerType, Object[] schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void sendActionBar(Component message, CommandSender sender) {
        if (!(sender instanceof Entity entity) || message == null) return;

        GenericRunnable delivery = new GenericRunnable() {
            @Override
            protected void execute() {
                sendActionBarNow(sender, message);
            }
        };

        try {
            if (Bukkit.isOwnedByCurrentRegion(entity)) {
                delivery.run();
                return;
            }
        } catch (RuntimeException | LinkageError ignored) {
        }

        try {
            runSync(true, 0, delivery, SchedulerType.ENTITY, new Object[]{entity});
        } catch (RuntimeException | LinkageError ignored) {
        }
    }

    private void sendActionBarNow(CommandSender sender, Component message) {
        try {
            sender.sendActionBar(message);
        } catch (RuntimeException | LinkageError ignored) {
        }
    }
}
