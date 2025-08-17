package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;

import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public abstract class AbstractPlayerLocaleListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper helper = main.getServerFactory().getWWCHelper();

    @EventHandler(priority = EventPriority.LOWEST)
    public abstract void detectLocalLang(PlayerJoinEvent event);

    @EventHandler(priority = EventPriority.LOWEST)
    public abstract void detectLangChange(PlayerLocaleChangeEvent event);

    public void checkAndSetLocale(Player player, String code) {
        if (player == null) return;
        checkAndSetLocaleAsync(player.getUniqueId(), code, player);
    }

    public void checkAndSetLocaleAsync(UUID uuid, String code, Player schedulerAnchor) {
        if (uuid == null || code == null) return;

        helper.runAsync(true, 0, new GenericRunnable() {
            @Override
            protected void execute() {
                if (main.getTranslatorName().equalsIgnoreCase("Invalid")) return;
                if (!refs.isSupportedLang(code, CommonRefs.LangType.LOCAL)) return;

                PlayerRecord record = main.getPlayerRecord(uuid.toString(), true);
                if (record == null) return;

                String existing = record.getLocalizationCode();
                if (existing != null && existing.equalsIgnoreCase(code)) return;

                SupportedLang lang = refs.getSupportedLang(code, CommonRefs.LangType.LOCAL);
                if (lang == null) return;

                record.setLocalizationCode(lang.getLangCode());
                String nativeName = lang.getNativeLangName();

                helper.runSync(true, 0, new GenericRunnable() {
                    @Override
                    protected void execute() {
                        Player p = Bukkit.getPlayer(uuid);
                        refs.sendMsg("wwcOnJoinSetLocaleSuccess",
                                new String[]{"&6" + nativeName, "&6/translate"},
                                "&d",
                                p);
                    }
                }, ENTITY, new Object[]{schedulerAnchor});
            }
        }, ENTITY, new Object[]{schedulerAnchor});
    }
}