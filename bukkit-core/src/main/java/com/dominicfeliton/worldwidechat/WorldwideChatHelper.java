package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.*;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;

import java.util.ArrayList;

public abstract class WorldwideChatHelper {
    protected WorldwideChat main = WorldwideChat.instance;

    protected CommonRefs refs = main.getServerFactory().getCommonRefs();

    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    public void checkVaultSupport() {
    }

    public void unregisterListeners() {
        ArrayList<RegisteredListener> listeners = HandlerList.getRegisteredListeners(main);
        for (RegisteredListener listener : listeners) {
            HandlerList.unregisterAll(listener.getListener());
        }
        refs.debugMsg("Size of internal registered listeners post removal: " + HandlerList.getRegisteredListeners(main).size());
    }

    public void sharedBukkitEventHandlers() {
        PluginManager pluginManager = main.getServer().getPluginManager();

        NotifsOnJoinListener join = new NotifsOnJoinListener();
        pluginManager.registerEvents(join, main);

        TranslateInGameListener translate = new TranslateInGameListener();
        pluginManager.registerEvents(translate, main);

        ConfigListener inv = new ConfigListener();
        pluginManager.registerEvents(inv, main);

        if (pluginManager.getPlugin("Citizens") != null) {
            CitizensListener citizens = new CitizensListener();
            pluginManager.registerEvents(citizens, main);

            main.getLogger().info(refs.getPlainMsg("wwcCitizensDetected",
                    "",
                    "&d"));
        }

        if (pluginManager.getPlugin("DecentHolograms") != null) {
            HologramListener hologram = new HologramListener();
            pluginManager.registerEvents(hologram, main);

            main.getLogger().info(refs.getPlainMsg("wwcDecentHologramsDetected",
                    "",
                    "&d"));
        }

        main.getLogger().info(refs.getPlainMsg("wwcListenersInitialized",
                "",
                "&d"));
    }

    public abstract void registerEventHandlers();

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
