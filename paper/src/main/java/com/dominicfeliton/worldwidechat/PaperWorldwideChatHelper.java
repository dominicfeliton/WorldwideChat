package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.*;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.LinkedList;
import java.util.Queue;

public class PaperWorldwideChatHelper extends SpigotWorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    // (We extend Spigot because a lot of Spigot methods work on Paper)
    // (You can switch back to just WorldwideChatHelper if we no longer want to rely on spigot WWCHelper at all)

    @Override
    public void registerEventHandlers() {
        refs.debugMsg("Size of listener queue: " + bukkitListenerQueue.size());
        // Unregister all previously registered listeners for this plugin
        while (!bukkitListenerQueue.isEmpty()) {
            Listener listener = bukkitListenerQueue.poll();
            HandlerList.unregisterAll(listener);
        }

        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.20")) >= 0) {
            // 1.2x supports sign editing
            PaperSignListener sign = new PaperSignListener();
            pluginManager.registerEvents(sign, main);
            bukkitListenerQueue.add(sign);
        }

        PaperChatListener chat = new PaperChatListener();
        pluginManager.registerEvent(
                AsyncChatEvent.class,
                chat,
                main.getChatPriority(),
                (listener, event) -> {
                    ((PaperChatListener) listener).onPlayerChat((AsyncChatEvent) event);
                },
                main
        );

        PaperPlayerLocaleListener locale = new PaperPlayerLocaleListener();
        pluginManager.registerEvents(locale, main);
        bukkitListenerQueue.add(locale);

        // Finish up
        sharedBukkitEventHandlers();
    }
}