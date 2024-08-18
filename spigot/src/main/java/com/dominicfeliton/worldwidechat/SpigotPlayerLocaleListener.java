package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.AbstractPlayerLocaleListener;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSetLocalOnFirstJoin()) {
            return;
        }

        // Attempt to get user localization
        Player player = e.getPlayer();
        if (main.isPlayerRecord(player)) {
            return;
        }

        // Get locale
        String locale = getLocale(player.getLocale());
        if (locale == null) {
            return;
        }

        // We have a locale. Now call super
        super.checkAndSetLocale(player, locale);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLangChange(PlayerLocaleChangeEvent event) {
        Player player = event.getPlayer();

        // Get locale
        String locale = getLocale(player.getLocale());
        if (locale == null) {
            return;
        }

        // We have a locale. Now call super
        super.checkAndSetLocale(player, locale);
    }

    private String getLocale(String locale) {
        Matcher match = Pattern.compile("^(.*?)_").matcher(locale);
        if (!match.find()) {
            refs.debugMsg("No locale detected! ???");
            return null;
        } else {
            return match.group(1);
        }
    }
}
