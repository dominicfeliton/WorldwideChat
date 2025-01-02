package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.AbstractSignListener;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;

public class SpigotSignListener extends AbstractSignListener<PlayerSignOpenEvent> implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    @Override
    public void onSignEdit(PlayerSignOpenEvent event) {
        // Event name is different on Paper

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
