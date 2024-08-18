package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperSetLocaleOnJoinListener extends AbstractSetLocaleOnJoinListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSetLocalOnFirstJoin()) {
            return;
        }

        // Attempt to get user localization
        Player player = e.getPlayer();
        if (!main.isPlayerRecord(player)) {
            return;
        }

        // Get locale and call super
        super.checkAndSetLocale(player, player.locale().getLanguage());
    }
}
