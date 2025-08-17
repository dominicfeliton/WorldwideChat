package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType;
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
        UUID uuid = player.getUniqueId();
        if (main.isPlayerRecord(player)) return;

        helper.runSync(true, 25, new GenericRunnable() {
            @Override
            protected void execute() {
                String code = player.locale().getLanguage();

                PaperPlayerLocaleListener.super.checkAndSetLocaleAsync(uuid, code, player);
            }
        }, SchedulerType.ENTITY, new Object[]{player});
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLangChange(PlayerLocaleChangeEvent event) {
        if (!main.getSyncUserLocal()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        helper.runSync(true, 25, new GenericRunnable() {
            @Override
            protected void execute() {
                String code = player.locale().getLanguage();

                PaperPlayerLocaleListener.super.checkAndSetLocaleAsync(uuid, code, player);
            }
        }, SchedulerType.ENTITY, new Object[]{player});
    }
}