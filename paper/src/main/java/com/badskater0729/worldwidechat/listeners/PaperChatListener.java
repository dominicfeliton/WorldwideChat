package com.badskater0729.worldwidechat.listeners;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.sisu.inject.Legacy;

import java.util.ArrayList;
import java.util.List;

public class PaperChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        try {
            if (!event.isAsynchronous()) {
                refs.debugMsg("chat event not async, skipping");
                return;
            }
            String originalText = LegacyComponentSerializer.legacyAmpersand().serialize(event.originalMessage());

            /* Original WWC functionality/Translate Outgoing Messages */
            ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
            String currInLang = currTranslator.getInLangCode();
            String currOutLang = currTranslator.getOutLangCode();
            if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
                    || (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
                Component newText = LegacyComponentSerializer.legacyAmpersand().deserialize(refs.translateText(originalText, event.getPlayer()));
                event.message(newText);
            }

            /* New WWC functionality/Translate Incoming Messages */
            //String strMsgContent = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());

            List<Audience> unmodifiedMessageRecipients = new ArrayList<Audience>();
            for (Audience eaRecipient : event.viewers()) {
                // Do not handle non-players
                if (!(eaRecipient instanceof Player currPlayer)) {
                    refs.debugMsg("Ignoring Console");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                ActiveTranslator testTranslator = main.getActiveTranslator(currPlayer.getUniqueId());
                String testInLang = testTranslator.getInLangCode();
                String testOutLang = testTranslator.getOutLangCode();
                if ((   // Check if this translator wants their incoming messages to be translated
                        !currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(currPlayer) && testTranslator.getTranslatingChatIncoming())
                        // Check if a previously outgoing translation was NOT what this user wants
                        && !(currInLang.equals(testInLang) && currOutLang.equals(testOutLang))) {

                    // Translate message + convert to Component
                    String translation = refs.translateText(originalText + " (Translated)", currPlayer);
                    Component hoverOutMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(translation);

                    // TODO: Make this a translator feature alongside a config option
                    // Add hover text w/original message
                    if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
                        hoverOutMessage = hoverOutMessage
                                .hoverEvent(HoverEvent.showText(Component.text(originalText).decorate(TextDecoration.ITALIC)));
                    }

                    // Re-render original message but with new text.
                    Component outMsg = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), hoverOutMessage, Audience.audience(event.viewers()));
                    currPlayer.sendMessage(outMsg);
                } else {
                    unmodifiedMessageRecipients.add(eaRecipient);
                }
            }
            event.viewers().clear();
            event.viewers().addAll(unmodifiedMessageRecipients);
        } catch (Exception e) {
            if (!refs.serverIsStopping()) {
                throw e;
            }
        }
    }
}
