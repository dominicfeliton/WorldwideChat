package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.TranslationProgressIndicator;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ASYNC;

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

        List<List<String>> allText = new ArrayList<>();

        // Process each page
        for (HologramPage hologramPage : event.getHologram().getPages()) {
            List<String> currentPage = new ArrayList<>();
            for (HologramLine eaLine : hologramPage.getLines()) {
                currentPage.add(eaLine.getContent());
            }
            allText.add(currentPage);
        }

        translateHologramPages(p, allText);
    }

    protected void translateHologramPages(Player player, List<List<String>> pages) {
        TranslationProgressIndicator.Handle status = refs.beginObjectStatusMsg("wwcSignTranslateStart", "", "&d&l", player);

        GenericRunnable translate = new GenericRunnable() {
            @Override
            protected void execute() {
                boolean anyPageTranslated = false;
                int page = 1;

                try {
                    for (List<String> currentPage : pages) {
                        List<String> translatedPage = translateHologramPage(currentPage, player);
                        if (!translatedPage.equals(currentPage)) {
                            anyPageTranslated = true;
                        }
                        sendHologramPage(player, page, translatedPage);
                        page++;
                    }

                    refs.sendMsg(player, "&6" + getHologramDashes() + "----", false);
                    if (anyPageTranslated) {
                        refs.finishStatusMsg(status, "wwcSignDone", "", "&a&o", player);
                    } else {
                        refs.failStatusMsg(status, player);
                    }
                } catch (RuntimeException | LinkageError e) {
                    refs.failStatusMsg(status, player);
                    throw e;
                }
            }
        };

        wwcHelper.runAsync(translate, ASYNC, null);
    }

    protected List<String> translateHologramPage(List<String> currentPage, Player player) {
        return refs.translateObjectText(currentPage, player);
    }

    private void sendHologramPage(Player player, int page, List<String> translatedPage) {
        refs.sendMsg(player, "&6" + getHologramPageHeader(page), false);
        refs.sendMsg(player, buildHologramPage(translatedPage), false);
    }

    private Component buildHologramPage(List<String> translatedPage) {
        Component out = Component.empty();
        for (int i = 0; i < translatedPage.size(); i++) {
            String line = translatedPage.get(i);
            out = out.append(Component.text(line));
            if (i + 1 < translatedPage.size()) {
                out = out.append(Component.newline());
            }
        }
        return out;
    }

    private String getHologramPageHeader(int page) {
        String dashes = getHologramDashes();
        int middleIndex = dashes.length() / 2;
        return dashes.substring(0, middleIndex) + " &2&l(&a&l" + page + "&2&l)&r&6 " + dashes.substring(middleIndex);
    }

    private String getHologramDashes() {
        return "---------------------";
    }

}
