package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.api.utils.collection.DList;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class HologramListener implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();
    protected WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDecentHoloRightClick(HologramClickEvent event) {
        if (!event.isAsynchronous()) {
            refs.debugMsg("HologramClickEvent ignored because it is synchronous");
            return;
        }

        if (!event.getClick().equals(ClickType.RIGHT)) {
            refs.debugMsg("Right click not detected.");
            return;
        }

        Player p = event.getPlayer();
        if (!main.isActiveTranslator(p) || !main.getActiveTranslator(p).getTranslatingSign()) {
            refs.debugMsg("player is not an active translator or is not translating signs");
            return;
        }

        DList<HologramPage> allText = event.getHologram().getPages();
        List<Component> translations = new ArrayList<>();

        // Process each page
        for (HologramPage hologramPage : allText) {
            List<String> currentPage = new ArrayList<>();
            for (HologramLine eaLine : hologramPage.getLines()) {
                currentPage.add(eaLine.getContent());
            }
            List<String> translatedPage = refs.translateText(currentPage, p, true);

            Component out = Component.empty();
            for (int i = 0; i < translatedPage.size(); i++) {
                String line = translatedPage.get(i);
                out = out.append(Component.text(line));
                if (i+1 < translatedPage.size()) {
                    out = out.append(Component.newline());
                }
            }
            translations.add(out);
        }

        // Send each hologram in chat
        int page = 1;
        String dashes = "---------------------";
        int middleIndex = dashes.length() / 2;
        for (Component eachPage : translations) {
            String result = dashes.substring(0, middleIndex) + " &2&l(&a&l" + page + "&2&l)&r&6 " + dashes.substring(middleIndex);
            refs.sendMsg(p, "&6" + result, false);
            refs.sendMsg(p, eachPage, false);
            page++;
        }
        refs.sendMsg(p, "&6" + dashes + "----", false);
    }

}
