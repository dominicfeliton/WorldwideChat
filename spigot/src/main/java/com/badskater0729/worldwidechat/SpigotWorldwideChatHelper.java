package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.*;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SpigotWorldwideChatHelper extends WorldwideChatHelper {

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    @Override
    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(), main);
        if (adapter.getServerInfo().getValue().contains("1.2")) {
            pluginManager.registerEvents(new SignListener(), main);
        }
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized", null));
    }

    @Override
    public void runAsync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runAsync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a bukkit runnable on " + main.getCurrPlatform());
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskLaterAsynchronously(main, delay);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskLaterAsynchronously(main, delay);
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a bukkit runnable on " + main.getCurrPlatform());
        }

        if (!serverMustBeRunning) {
            // If server does not need to be running
            ((BukkitRunnable) in).runTaskTimerAsynchronously(main, delay, repeatTime);
            return;
        }

        // If it does
        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskTimerAsynchronously(main, delay, repeatTime);
        }
    }

    @Override
    public void runAsyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runAsyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSync(true, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSync(serverMustBeRunning, 0, in, schedulerType, schedulerObj);
    }

    @Override
    public void runSync(boolean serverMustBeRunning, int delay, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a bukkit runnable on " + main.getCurrPlatform());
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskLater(main, delay);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskLater(main, delay);
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int delay, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        if (!(in instanceof BukkitRunnable)) {
            refs.debugMsg("Not a Bukkit Runnable but we are on " + main.getCurrPlatform() + "!! INVESTIGATE!");
            return;
        } else {
            refs.debugMsg("We are a bukkit runnable on " + main.getCurrPlatform());
        }

        if (!serverMustBeRunning) {
            ((BukkitRunnable) in).runTaskTimer(main, delay, repeatTime);
            return;
        }

        if (!refs.serverIsStopping()) {
            ((BukkitRunnable) in).runTaskTimer(main, delay, repeatTime);
        }
    }

    @Override
    public void runSyncRepeating(boolean serverMustBeRunning, int repeatTime, Runnable in, SchedulerType schedulerType, Object schedulerObj) {
        runSyncRepeating(serverMustBeRunning, 0, repeatTime, in, schedulerType, schedulerObj);
    }

}
