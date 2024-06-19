package com.badskater0729.worldwidechat.listeners;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.sisu.inject.Legacy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            refs.debugMsg("Using paperChatListener.");

            /* Original WWC functionality/Translate Outgoing Messages */
            refs.debugMsg("Begin translating outgoing messages...");
            ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
            String currInLang = currTranslator.getInLangCode();
            String currOutLang = currTranslator.getOutLangCode();
            if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
                    || (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
                Component originalText = refs.deserial(refs.translateText(refs.serial(event.originalMessage()), event.getPlayer()));
                event.message(originalText);
            }

            /* New WWC functionality/Translate Incoming Messages */
            refs.debugMsg("Begin translating incoming messages...");
            Set<Audience> unmodifiedMessageRecipients = new HashSet<Audience>();
            for (Audience eaRecipient : event.viewers()) {
                // Do not handle non-players
                if (!(eaRecipient instanceof Player)) {
                    refs.debugMsg("Ignoring Console");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }
                Player currPlayer = (Player) eaRecipient;

                ActiveTranslator testTranslator = main.getActiveTranslator(currPlayer);
                String testInLang = testTranslator.getInLangCode();
                String testOutLang = testTranslator.getOutLangCode();

                if (!main.isActiveTranslator(currPlayer)) {
                    refs.debugMsg("Skipping " + currPlayer.getName() + ", not an active translator!");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                if (currTranslator.getUUID().equals(testTranslator.getUUID())) {
                    refs.debugMsg("Skipping " + currPlayer.getName() + ", can't request an incoming translation of their own message! (player==player)");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                if (!testTranslator.getTranslatingChatIncoming()) {
                    refs.debugMsg("Skipping " + currPlayer.getName() + ", does not want their incoming chat to be translated.");
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                if (currInLang.equals(testInLang) && currOutLang.equals(testOutLang)) {
                    unmodifiedMessageRecipients.add(eaRecipient);
                    refs.debugMsg("Skipping " + currPlayer.getName() + ", currIn/OutLang == currIn/OutLang (???).");
                    continue;
                }
                // If all checks pass, translate an incoming message for the current translator.

                // Translate message + convert to Component
                String originalText = refs.serial(event.message());
                String translation = refs.translateText(originalText, currPlayer);
                Component hoverOutMessage = refs.deserial(translation + " \uD83C\uDF10");

                // Add hover text w/original message
                if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
                    hoverOutMessage = hoverOutMessage
                            .hoverEvent(HoverEvent.showText(Component.text(originalText).decorate(TextDecoration.ITALIC)));
                }

                // Re-render original message but with new text.
                refs.debugMsg("Rendering new message for current player ( " + currPlayer.getName() + "  : " + refs.serial(hoverOutMessage) + " )");
                Component outMsg = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), hoverOutMessage, Audience.audience(event.viewers()));
                currPlayer.sendMessage(outMsg);

                // Render original message? (dumb)
                // TODO: Currently there is a bug where event.renderer().render permanently overwrites.
                // Anyone with /wwctci auto overwrites anyone with /wwctco.
                // ^ Less confirmed...but it looks like other /wwctci sessions override each other.
                // Ensure this does not happen? Super hard to track down...
                refs.debugMsg("Setting original message ( " + refs.serial(event.message()) + " )");
                event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), Audience.audience(event.viewers()));
            }
            event.viewers().retainAll(unmodifiedMessageRecipients);
        } catch (Exception e) {
            if (!refs.serverIsStopping()) {
                throw e;
            }
        }
    }
}
