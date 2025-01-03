package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class PaperPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSyncUserLocal()) {
            refs.debugMsg("locale detection disabled");
            return;
        }

        // Attempt to get user localization
        Player player = e.getPlayer();
        if (main.isPlayerRecord(player)) {
            refs.debugMsg("User has a player record");
            return;
        }

        // Get locale and call super
        refs.debugMsg("Attempting to set locale!!");
        super.checkAndSetLocale(player, player.locale().getLanguage());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLangChange(PlayerLocaleChangeEvent event) {
        if (!main.getSyncUserLocal()) {
            return;
        }

        Player player = event.getPlayer();

        GenericRunnable change = new GenericRunnable() {
            @Override
            protected void execute() {
                PaperPlayerLocaleListener.super.checkAndSetLocale(player, main.getServer().getPlayer(player.getUniqueId()).locale().getLanguage());
            }
        };

        helper.runSync(true, 50, change, ENTITY, new Object[]{player});
    }
}
