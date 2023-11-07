package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.ChatListener;
import com.badskater0729.worldwidechat.listeners.InventoryListener;
import com.badskater0729.worldwidechat.listeners.OnPlayerJoinListener;
import com.badskater0729.worldwidechat.listeners.TranslateInGameListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class WorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        pluginManager.registerEvents(new ChatListener(), main);
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized"));
    }

}
