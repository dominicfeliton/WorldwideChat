package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperSignListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignEdit(PlayerOpenSignEvent event) {
        Player currPlayer = event.getPlayer();
        if (main.isActiveTranslator(currPlayer)) {
            ActiveTranslator currTranslator = main.getActiveTranslator(currPlayer);
            if (currTranslator.getTranslatingSign()) {
                event.setCancelled(true);
                if (!currTranslator.getSignWarning()) {
                    refs.sendMsg("wwctsWarning", "", "&d&o", currPlayer);
                    currTranslator.setSignWarning(true);
                }
            }
        }
    }
}
