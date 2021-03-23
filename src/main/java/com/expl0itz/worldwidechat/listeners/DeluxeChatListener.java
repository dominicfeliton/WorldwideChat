package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;

import me.clip.deluxechat.events.DeluxeChatEvent;

public class DeluxeChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeluxeChat(DeluxeChatEvent event) { 
        if (main.getActiveTranslator(event.getPlayer().getUniqueId().toString()) instanceof ActiveTranslator || main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) {
            String out = new ChatListener().processPlayerChat(event.getPlayer(), event.getChatMessage());
            event.setChatMessage(out);
        }
    }
}
