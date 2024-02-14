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

import java.util.ArrayList;
import java.util.List;

public class PaperChatListener implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        try {
            // TODO: Revisit and check if we are running async, do not spawn new task if we are (for both listeners)
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
            Component formattedMsg = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), Audience.audience(event.viewers()));
            String formattedMsgStr = LegacyComponentSerializer.legacyAmpersand().serialize(formattedMsg);

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
                if ((   // Check if this testTranslator wants their incoming messages to be translated
                        !currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(currPlayer) && testTranslator.getTranslatingChatIncoming())
                        // Check if this testTranslator doesn't already want the current chat message
                        && !(currInLang.equals(testInLang) && currOutLang.equals(testOutLang))) {
                    // Send the message in a new task, to avoid delaying the chat message for others
                    BukkitRunnable chatHover = new BukkitRunnable() {
                        @Override
                        public void run() {
                            String translation = refs.translateText(formattedMsgStr + " (Translated)", currPlayer);
                            Component hoverOutMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(translation);

                            // TODO: Make this a translator feature alongside a config option
                            if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
                                hoverOutMessage = hoverOutMessage
                                        .hoverEvent(HoverEvent.showText(Component.text(formattedMsgStr).decorate(TextDecoration.ITALIC)));
                            }
                            if (currPlayer.isOnline()) currPlayer.sendMessage(hoverOutMessage);
                        }
                    };
                    refs.runAsync(chatHover);
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
