package com.dominicfeliton.worldwidechat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.util.UUID;

public class PaperPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSyncUserLocal()) return;

        Player player = e.getPlayer();
        if (main.isPlayerRecord(player)) return;

        String code = player.locale().getLanguage();
        UUID uuid = player.getUniqueId();
        super.checkAndSetLocaleAsync(uuid, code, player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLangChange(PlayerLocaleChangeEvent event) {
        if (!main.getSyncUserLocal()) return;

        Player player = event.getPlayer();
        String code = player.locale().getLanguage();
        UUID uuid = player.getUniqueId();
        super.checkAndSetLocaleAsync(uuid, code, player);
    }
}