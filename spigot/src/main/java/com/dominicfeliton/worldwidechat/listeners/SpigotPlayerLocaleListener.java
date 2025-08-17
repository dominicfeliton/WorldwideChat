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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotPlayerLocaleListener extends AbstractPlayerLocaleListener implements Listener {

    private static final Pattern LOCALE_PREFIX = Pattern.compile("^(.*?)_");

    @EventHandler(priority = EventPriority.LOWEST)
    public void detectLocalLang(PlayerJoinEvent e) {
        if (!main.getSyncUserLocal()) return;

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (main.isPlayerRecord(player)) return;

        helper.runSync(true, 25, new GenericRunnable() {
            @Override
            protected void execute() {
                String code = getLocale(player.getLocale());
                if (code == null) return;

                SpigotPlayerLocaleListener.super.checkAndSetLocaleAsync(uuid, code, player);
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
                String code = getLocale(player.getLocale());
                if (code == null) return;

                SpigotPlayerLocaleListener.super.checkAndSetLocaleAsync(uuid, code, player);
            }
        }, SchedulerType.ENTITY, new Object[]{player});
    }

    private String getLocale(String locale) {
        Matcher match = LOCALE_PREFIX.matcher(locale);
        if (!match.find()) {
            refs.debugMsg("No locale detected! ???");
            return null;
        }
        return match.group(1);
    }
}