package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.PaperChatListener;
import com.dominicfeliton.worldwidechat.listeners.PaperPlayerLocaleListener;
import com.dominicfeliton.worldwidechat.listeners.PaperSignListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

public class PaperWorldwideChatHelper extends SpigotWorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    // (We extend Spigot because a lot of Spigot methods work on Paper)
    // (You can switch back to just WorldwideChatHelper if we no longer want to rely on spigot WWCHelper at all)

    @Override
    public void registerEventHandlers() {
        // Unregister all previously registered listeners for this plugin
        unregisterListeners();

        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        if (main.getCurrMCVersion().compareTo(new ComparableVersion("1.20")) >= 0) {
            // 1.2x supports sign editing
            PaperSignListener sign = new PaperSignListener();
            pluginManager.registerEvents(sign, main);
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

        // Finish up
        sharedBukkitEventHandlers();
    }

    @Override
    public void sendActionBar(Component message, CommandSender sender) {
        sender.sendActionBar(message);
    }
}