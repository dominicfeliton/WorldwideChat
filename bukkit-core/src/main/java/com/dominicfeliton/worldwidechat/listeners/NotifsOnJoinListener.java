package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotifsOnJoinListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;

    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void sendNotifsOnPlayerJoin(PlayerJoinEvent event) {
        Player currPlayer = event.getPlayer();

        // Check if plugin has updates
        if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")) && (main.getOutOfDate())
                && (currPlayer.hasPermission("worldwidechat.chatupdate"))) {
            final TextComponent outOfDate = Component.text()
                    .content(refs.getPlainMsg("wwcUpdaterOutOfDateChat", currPlayer))
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text().content(" (").color(NamedTextColor.GOLD))
                    .append(Component.text().content("https://github.com/dominicfeliton/WorldwideChat/releases")
                            .color(NamedTextColor.GOLD)
                            .clickEvent(ClickEvent.openUrl("https://github.com/dominicfeliton/WorldwideChat/releases"))
                            .decoration(TextDecoration.UNDERLINED, true))
                    .append(Component.text().content(")").color(NamedTextColor.GOLD)).build();
            refs.sendMsg(currPlayer, outOfDate);
        }

        /* Global translate is disabled, and user has a translation config */
        if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
                && !main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
                && main.isActiveTranslator(currPlayer)) {
            ActiveTranslator currTranslator = main.getActiveTranslator(currPlayer);
            String out = currTranslator.getOutLangCode();
            String in = currTranslator.getInLangCode();

            String fOutLang = formatLangs(out, "out");
            String fInLang = formatLangs(in, "in");

            if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
                refs.sendMsg("wwcOnJoinTranslationNotificationSourceLang", new String[]{"&6" + fInLang, "&6" + fOutLang}, "&d", currPlayer);
            } else {
                refs.sendMsg("wwcOnJoinTranslationNotificationNoSourceLang", "&6" + fOutLang, "&d", currPlayer);
            }
            /* Global translate is enabled, and user does not have a translation config */
        } else if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
                && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
                && !main.isActiveTranslator(event.getPlayer())) {
            ActiveTranslator currTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
            String out = currTranslator.getOutLangCode();
            String in = currTranslator.getInLangCode();

            String fOutLang = formatLangs(out, "out");
            String fInLang = formatLangs(in, "in");

            if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
                refs.sendMsg("wwcGlobalOnJoinTranslationNotificationSourceLang", new String[]{"&6" + fInLang, "&6" + fOutLang}, "&d", currPlayer);
            } else {
                refs.sendMsg("wwcGlobalOnJoinTranslationNotificationNoSourceLang", "&6" + fOutLang, "&d", currPlayer);
            }
            /* Global translate is enabled, but user ALSO has a translation config */
        } else if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
                && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")
                && main.isActiveTranslator(event.getPlayer())) {
            ActiveTranslator currTranslator = main.getActiveTranslator(currPlayer);

            String out = currTranslator.getOutLangCode();
            String in = currTranslator.getInLangCode();

            String fOutLang = formatLangs(out, "out");
            String fInLang = formatLangs(in, "in");

            if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
                refs.sendMsg("wwcOverrideGlobalOnJoinTranslationNotificationSourceLang", new String[]{"&6" + fInLang, "&6" + fOutLang}, "&d", currPlayer);
            } else {
                refs.sendMsg("wwcOverrideGlobalOnJoinTranslationNotificationNoSourceLang", "&6" + fOutLang, "&d", currPlayer);
            }
        }
    }

    private String formatLangs(String code, String type) {
        return refs.getSupportedLang(code, type).getNativeLangName().isEmpty() ?
                refs.getSupportedLang(code, type).getLangCode() : refs.getSupportedLang(code, type).getNativeLangName();
    }
}