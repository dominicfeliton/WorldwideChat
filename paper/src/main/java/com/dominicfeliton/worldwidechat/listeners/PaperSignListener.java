package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperSignListener extends AbstractSignListener<PlayerOpenSignEvent> implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    @Override
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
