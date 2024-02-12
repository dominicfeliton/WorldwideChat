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
            Component formattedMessage = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), Audience.audience(event.viewers()));
            String formattedOriginalText = LegacyComponentSerializer.legacyAmpersand().serialize(formattedMessage);

            List<Audience> unmodifiedMessageRecipients = new ArrayList<Audience>();
            for (Audience eaRecipient : event.viewers()) {
                refs.debugMsg("Checking recipient");
                // Do not handle non-players
                if (!(eaRecipient instanceof Player)) {
                    refs.debugMsg("Recipient is not a player");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                Player currPlayer = (Player) eaRecipient;
                refs.debugMsg("Checks passed " + currPlayer.getName());
                ActiveTranslator testTranslator = main.getActiveTranslator(currPlayer.getUniqueId());
                String testInLang = testTranslator.getInLangCode();
                String testOutLang = testTranslator.getOutLangCode();
                if ((   // Check if this testTranslator wants their incoming messages to be translated
                        !currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(currPlayer) && testTranslator.getTranslatingChatIncoming())
                        // Check if this testTranslator doesn't already want the current chat message
                        && !(currInLang.equals(testInLang) && currOutLang.equals(testOutLang))) {
                    // Send the message in a new task, to avoid delaying the chat message for others
                    refs.debugMsg("???");
                    BukkitRunnable chatHover = new BukkitRunnable() {
                        @Override
                        public void run() {
                            String translation = refs.translateText(formattedOriginalText + " (Translated)", currPlayer);
                            Component hoverOutMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(translation);

                            if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
                                hoverOutMessage = hoverOutMessage
                                        .hoverEvent(HoverEvent.showText(Component.text(formattedOriginalText).decorate(TextDecoration.ITALIC)));
                            }
                            try {
                                // TODO for both listeners: check if player still exists before sending message
                                currPlayer.sendMessage(hoverOutMessage);
                            } catch (IllegalStateException e) {}
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
