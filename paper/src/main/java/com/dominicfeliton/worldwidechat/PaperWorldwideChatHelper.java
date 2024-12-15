package com.dominicfeliton.worldwidechat;

import com.comphenix.protocol.ProtocolLibrary;
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

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    private final Queue<Listener> listenerQueue = new LinkedList<>();

    @Override
    public void registerEventHandlers() {
        refs.debugMsg("Size of listener queue: " + listenerQueue.size());
        // Unregister all previously registered listeners for this plugin
        while (!listenerQueue.isEmpty()) {
            Listener listener = listenerQueue.poll();
            HandlerList.unregisterAll(listener);
        }
        if (main.getProtocolManager() != null) {
            main.getProtocolManager().removePacketListeners(main);
        }

        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.20")) >= 0) {
            // 1.2x supports sign editing
            PaperSignListener sign = new PaperSignListener();
            pluginManager.registerEvents(sign, main);
            listenerQueue.add(sign);
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
        listenerQueue.add(chat);

        NotifsOnJoinListener join = new NotifsOnJoinListener();
        pluginManager.registerEvents(join, main);
        listenerQueue.add(join);

        PaperPlayerLocaleListener locale = new PaperPlayerLocaleListener();
        pluginManager.registerEvents(locale, main);
        listenerQueue.add(locale);

        TranslateInGameListener translate = new TranslateInGameListener();
        pluginManager.registerEvents(translate, main);
        listenerQueue.add(translate);

        ConfigListener inv = new ConfigListener();
        pluginManager.registerEvents(inv, main);
        listenerQueue.add(inv);

        // ProtocolLib Setup
        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.19")) >= 0 &&
                main.getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            main.getLogger().info("DEBUG: PROTOCOL ENABLED!");
            main.setProtocolManager(ProtocolLibrary.getProtocolManager());

            // Register listeners
            new ChatPacketListener();
        }
        main.getLogger().info(refs.getPlainMsg("wwcListenersInitialized",
                "",
                "&d"));
    }
}