package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.CitizensListener;
import com.dominicfeliton.worldwidechat.listeners.ConfigListener;
import com.dominicfeliton.worldwidechat.listeners.NotifsOnJoinListener;
import com.dominicfeliton.worldwidechat.listeners.TranslateInGameListener;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.CommonTask;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import org.bukkit.entity.Panda;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.LinkedList;
import java.util.Queue;

public abstract class WorldwideChatHelper {
    protected WorldwideChat main = WorldwideChat.instance;

    protected CommonRefs refs = main.getServerFactory().getCommonRefs();

    protected final Queue<Listener> bukkitListenerQueue = new LinkedList<>();

    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    public void checkVaultSupport() {
    }

    public void sharedBukkitEventHandlers() {
        PluginManager pluginManager = main.getServer().getPluginManager();

        NotifsOnJoinListener join = new NotifsOnJoinListener();
        pluginManager.registerEvents(join, main);
        bukkitListenerQueue.add(join);

        TranslateInGameListener translate = new TranslateInGameListener();
        pluginManager.registerEvents(translate, main);
        bukkitListenerQueue.add(translate);

        ConfigListener inv = new ConfigListener();
        pluginManager.registerEvents(inv, main);
        bukkitListenerQueue.add(inv);

        if (pluginManager.getPlugin("Citizens") != null) {
            CitizensListener citizens = new CitizensListener();
            pluginManager.registerEvents(citizens, main);
            bukkitListenerQueue.add(citizens);

            main.getLogger().info(refs.getPlainMsg("wwcCitizensDetected",
                    "",
                    "&d"));
        }

        main.getLogger().info(refs.getPlainMsg("wwcListenersInitialized",
                "",
                "&d"));
    }

    public void registerEventHandlers() {
    }

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
