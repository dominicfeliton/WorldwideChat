package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.InventoryListener;
import com.badskater0729.worldwidechat.listeners.OnPlayerJoinListener;
import com.badskater0729.worldwidechat.listeners.PaperChatListener;
import com.badskater0729.worldwidechat.listeners.TranslateInGameListener;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public class PaperWorldwideChatHelper extends WorldwideChatHelper {
    // Store additional, WorldwideChat-exclusive methods here
    // Also required for our maven setup

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    @Override
    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        main.getLogger().info("Registering Paper chat listener...");
        pluginManager.registerEvents(new PaperChatListener(), main);
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized"));
    }

}
