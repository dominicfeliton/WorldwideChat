package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.PlayerRecord;

import me.clip.deluxechat.events.DeluxeChatEvent;

public class DeluxeChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeluxeChat(DeluxeChatEvent event) { 
        if (!main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("") || !main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")) {
        	PlayerRecord currRecord = main.getPlayerRecord(event.getPlayer().getUniqueId().toString(), true);
        	currRecord.setAttemptedTranslations(currRecord.getAttemptedTranslations()+1);
        	String out = CommonDefinitions.translateText(event.getChatMessage(), event.getPlayer());
            
            // This method gets called in addition to the main chat thread, so we are adding stats manually.
            currRecord.setSuccessfulTranslations(currRecord.getSuccessfulTranslations()+1);
            
            event.setChatMessage(out);
        }
    }
	
}
