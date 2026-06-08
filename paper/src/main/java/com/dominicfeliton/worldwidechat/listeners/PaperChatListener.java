package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs.GuidelinesCheckContext;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

public class PaperChatListener extends AbstractChatListener<AsyncChatEvent> implements Listener {

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
            String originalText = refs.serial(event.originalMessage());
            Component outgoingText = event.message();
            GuidelinesCheckContext guidelinesCheck = refs.createGuidelinesCheckContext(originalText, event.getPlayer());
            if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
                    || (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
                outgoingText = refs.deserial(refs.translateTextForChat(originalText, event.getPlayer(), guidelinesCheck));
                if (guidelinesCheck.isBlocked()) {
                    return;
                }
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
                String translation = refs.translateTextForChat(savedText, currPlayer, guidelinesCheck);
                if (guidelinesCheck.isBlocked()) {
                    return;
                }
                if (translation.equalsIgnoreCase(savedText)) {
                    refs.debugMsg("Translation unsuccessful/same as original message for " + currPlayer.getName());
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                // Re-render original message but with new text.
                Component outMsg = formatMessage(event, currPlayer, refs.deserial(translation), refs.deserial(savedText), true);
                refs.sendMsg(currPlayer, outMsg, false);
            }
            event.viewers().retainAll(unmodifiedMessageRecipients);

            // Send Pending Outgoing Messages (If force == true)
            if (channel && !outgoingText.equals(event.message())) {
                refs.debugMsg("Init pending outgoing message...");
                for (Audience eaRecipient : event.viewers()) {
                    Component outgoingMessage;
                    if (eaRecipient instanceof Player) {
                        outgoingMessage = formatMessage(event, (Player) eaRecipient, outgoingText, event.message(), false);
                    } else {
                        outgoingMessage = formatMessage(event, null, outgoingText, event.message(), false);
                    }
                    sendChatMessage(eaRecipient, outgoingMessage);
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
        Component outMsg = refs.getVaultMessage(event.getPlayer(), translation, event.getPlayer().name());

        // Add hover text w/original message
        if ((incoming && main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat"))
                || (!incoming && main.getConfigManager().getMainConfig().getBoolean("Chat.sendOutgoingHoverTextChat"))) {
            refs.debugMsg("Add hover!");
            outMsg = outMsg
                    .hoverEvent(HoverEvent.showText(refs.getVaultHoverMessage(event.getPlayer(), original, event.getPlayer().name(), targetPlayer)));
        }

        return outMsg;
    }

    private void sendChatMessage(Audience recipient, Component message) {
        if (recipient instanceof CommandSender commandSender) {
            refs.sendMsg(commandSender, message, false);
            return;
        }

        main.getServerFactory().getWWCHelper().runSync(true, 0, new GenericRunnable() {
            @Override
            protected void execute() {
                try {
                    recipient.sendMessage(message);
                } catch (RuntimeException | LinkageError ignored) {
                }
            }
        }, GLOBAL, null);
    }
}
