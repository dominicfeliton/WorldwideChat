package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.*;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public class SpigotWorldwideChatHelper extends WorldwideChatHelper {

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

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

}
