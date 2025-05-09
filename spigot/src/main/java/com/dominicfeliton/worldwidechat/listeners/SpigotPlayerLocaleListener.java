package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class SpigotPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSyncUserLocal()) {
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
        // TODO BUG REPORT ON SPIGOT UPSTREAM:
        // This event only gets the LAST language the user had.
        // Meaning if you switch from english to french, it will detect english.
        // But if you then switch back to english it will detect french.
        // :(

        if (!main.getSyncUserLocal()) {
            return;
        }

        Player player = event.getPlayer();

        GenericRunnable change = new GenericRunnable() {
            @Override
            protected void execute() {
                // Get locale
                String locale = getLocale(player.getLocale());
                if (locale == null) {
                    return;
                }

                SpigotPlayerLocaleListener.super.checkAndSetLocale(player, locale);
            }
        };

        helper.runSync(true, 50, change, ENTITY, new Object[]{player});
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
