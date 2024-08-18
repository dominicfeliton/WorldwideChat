package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.sisu.inject.Legacy;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PaperChatListener extends AbstractChatListener<AsyncChatEvent> implements Listener {

    private final WorldwideChat main;
    private final CommonRefs refs;

    public PaperChatListener() {
        super();
        this.main = WorldwideChat.instance;
        this.refs = main.getServerFactory().getCommonRefs();
    }

    @Override
    public void onPlayerChat(AsyncChatEvent event) {
        try {
            if (!event.isAsynchronous()) {
                refs.debugMsg("chat event not async, skipping");
                return;
            }
            refs.debugMsg("Using paperChatListener.");
            boolean channel = main.isForceSeparateChatChannel();

            // Translate Outgoing Messages
            refs.debugMsg("Begin translating outgoing messages...");
            ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
            String currInLang = currTranslator.getInLangCode();
            String currOutLang = currTranslator.getOutLangCode();
            Component outgoingText = event.message();
            if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
                    || (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
                outgoingText = refs.deserial(refs.translateText(refs.serial(event.originalMessage()), event.getPlayer()));
                if (!channel) {
                    event.message(outgoingText);
                }
            }

            // Translate Incoming Messages
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
                String savedText = refs.serial(outgoingText);
                String translation = refs.translateText(savedText, currPlayer);
                if (translation.equalsIgnoreCase(savedText)) {
                    refs.debugMsg("Translation unsuccessful/same as original message for " + currPlayer.getName());
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                // Re-render original message but with new text.
                Component outMsg = formatMessage(event, currPlayer, refs.deserial(translation), refs.deserial(savedText), true);
                currPlayer.sendMessage(outMsg);
            }
            event.viewers().retainAll(unmodifiedMessageRecipients);

            // Send Pending Outgoing Messages (If force == true)
            if (channel && !outgoingText.equals(event.message())) {
                refs.debugMsg("Init pending outgoing message...");
                for (Audience eaRecipient : event.viewers()) {
                    Component outgoingMessage;
                    if (eaRecipient instanceof Player) {
                        outgoingMessage = formatMessage(event, (Player)eaRecipient, outgoingText, event.message(), false);
                    } else {
                        outgoingMessage = formatMessage(event, null, outgoingText, event.message(), false);
                    }
                    eaRecipient.sendMessage(outgoingMessage);
                }

                refs.debugMsg("Cancelling chat event.");
                event.setCancelled(true);
            }
        } catch (Exception e) {
            if (!refs.serverIsStopping()) {
                throw e;
            }
        }
    }

    private Component formatMessage(AsyncChatEvent event, Player targetPlayer, Component translation, Component original, boolean incoming) {
        // Vault Support (if it exists)
        Component outMsg = super.getVaultMessage(event.getPlayer(), translation, event.getPlayer().name());

        // Add hover text w/original message
        if ((incoming && (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")))
        || (!incoming && main.getConfigManager().getMainConfig().getBoolean("Chat.sendOutgoingHoverTextChat"))) {
            refs.debugMsg("Add hover!");
            outMsg = outMsg
                    .hoverEvent(HoverEvent.showText(super.getVaultHoverMessage(event.getPlayer(), original, event.getPlayer().name(), targetPlayer)));
        }

        return outMsg;
    }
}
