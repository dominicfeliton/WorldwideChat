package com.badskater0729.worldwidechat.listeners;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSignOpenEvent;

public class SignListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignEdit(PlayerSignOpenEvent event) {
        Player currPlayer = event.getPlayer();
        if (main.isActiveTranslator(currPlayer)) {
            ActiveTranslator currTranslator = main.getActiveTranslator(currPlayer);
            if (currTranslator.getTranslatingSign()) {
                event.setCancelled(true);
                if (!currTranslator.getSignWarning()) {
                    refs.sendFancyMsg("wwctsWarning", "", "&d&o", currPlayer);
                    currTranslator.setSignWarning(true);
                }
            }
        }
    }
}
