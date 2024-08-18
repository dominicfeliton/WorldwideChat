package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

public class PaperPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSetLocalOnFirstJoin()) {
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
        // TODO BUG REPORT ON SPIGOT UPSTREAM:
        // This event only gets the LAST language the user had.
        // Meaning if you switch from english to french, it will detect english.
        // But if you then switch back to english it will detect french.
        // :(
        if (!main.getSetLocalOnFirstJoin()) {
            return;
        }

        Player player = event.getPlayer();
        super.checkAndSetLocale(player, player.locale().getLanguage());
    }
}
