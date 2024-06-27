package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.*;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

public class PaperWorldwideChatHelper extends SpigotWorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup
    // (We extend Spigot because a lot of Spigot methods work on Paper)
    // (You can switch back to just WorldwideChatHelper if we no longer want to rely on spigot WWCHelper at all)

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    @Override
    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        if (adapter.getServerInfo().getValue().contains("1.2")) {
            // 1.2x supports sign editing
            pluginManager.registerEvents(new PaperSignListener(), main);
        }
        pluginManager.registerEvents(new PaperChatListener(), main);
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized", null));
    }
}