package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

public abstract class AbstractPlayerLocaleListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper helper = main.getServerFactory().getWWCHelper();

    @EventHandler(priority = EventPriority.LOWEST)
    public abstract void detectLocalLang(PlayerJoinEvent event);

    @EventHandler(priority = EventPriority.LOWEST)
    public abstract void detectLangChange(PlayerLocaleChangeEvent event);

    public void checkAndSetLocale(Player player, String code) {
        PlayerRecord record = main.getPlayerRecord(player, true);

        if (main.getTranslatorName().equalsIgnoreCase("Invalid")) {
            // If the plugin is disabled we look quite foolish
            refs.debugMsg("Invalid translator for locale detection.");
            return;
        }

        if (!refs.isSupportedLang(code, CommonRefs.LangType.LOCAL)) {
            refs.debugMsg("This user's local is not something we support. Not setting.");
            return;
        }

        if (record.getLocalizationCode().equalsIgnoreCase(code)) {
            refs.debugMsg("This user already has this local set.");
            return;
        }

        // This is a supported lang
        SupportedLang lang = refs.getSupportedLang(code, CommonRefs.LangType.LOCAL);
        record.setLocalizationCode(lang.getLangCode());
        refs.sendMsg("wwcOnJoinSetLocaleSuccess", new String[]{"&6" + lang.getNativeLangName(), "&6/translate"}, "&d", player);
    }
}
