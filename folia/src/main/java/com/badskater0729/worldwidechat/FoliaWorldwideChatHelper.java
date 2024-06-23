package com.badskater0729.worldwidechat;

import com.badskater0729.worldwidechat.listeners.InventoryListener;
import com.badskater0729.worldwidechat.listeners.OnPlayerJoinListener;
import com.badskater0729.worldwidechat.listeners.TranslateInGameListener;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;

public class FoliaWorldwideChatHelper extends WorldwideChatHelper {

    WorldwideChat main = WorldwideChat.instance;

    CommonRefs refs = main.getServerFactory().getCommonRefs();

    ServerAdapterFactory adapter = main.getServerFactory();

    @Override
    public void registerEventHandlers() {
        // EventHandlers + check for plugins
        PluginManager pluginManager = main.getServer().getPluginManager();
        // TODO
        // if (adapter.getServerInfo().getValue().contains("1.2")) {
        //    // 1.2x supports sign editing
        //    pluginManager.registerEvents(new PaperSignListener(), main);
        //}
        //pluginManager.registerEvents(new PaperChatListener(), main);
        pluginManager.registerEvents(new OnPlayerJoinListener(), main);
        pluginManager.registerEvents(new TranslateInGameListener(), main);
        pluginManager.registerEvents(new InventoryListener(), main);
        main.getLogger().info(ChatColor.LIGHT_PURPLE
                + refs.getMsg("wwcListenersInitialized", null));
    }

}