package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.AbstractSetLocaleOnJoinListener;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotSetLocaleOnJoinListener extends AbstractSetLocaleOnJoinListener implements Listener {

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

        // Get locale
        String locale = player.getLocale();
        Matcher match = Pattern.compile("^(.*?)_").matcher(locale);
        if (!match.find()) {
            refs.debugMsg("No locale detected! ???");
            return;
        } else {
            locale = match.group(1);
        }

        // We have a locale. Now call super
        super.checkAndSetLocale(player, locale);
    }
}
