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
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.sisu.inject.Legacy;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PaperChatListener extends AbstractChatListener<AsyncChatEvent> implements Listener, ChatRenderer.ViewerUnaware {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    @Override
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
                if (translation.equalsIgnoreCase(originalText)) {
                    refs.debugMsg("Translation unsuccessful/same as original message for " + currPlayer.getName());
                    unmodifiedMessageRecipients.add(eaRecipient);
                    continue;
                }

                // Re-render original message but with new text.
                Component outMsg;
                Chat chat = main.getChat();
                if (chat != null) {
                    outMsg = super.getVaultMessage(currPlayer, event.getPlayer(), refs.deserial(translation), event.getPlayer().displayName());
                } else {
                    refs.debugMsg("Rendering new message for current player ( " + currPlayer.getName() + "  : " + translation + " )");
                    outMsg = this.render(event.getPlayer(), event.getPlayer().displayName(), refs.deserial(translation));
                }

                // Add hover text w/original message
                if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
                    outMsg = outMsg
                            .hoverEvent(HoverEvent.showText(Component.text(originalText).decorate(TextDecoration.ITALIC)));
                }

                currPlayer.sendMessage(outMsg);
            }
            event.viewers().retainAll(unmodifiedMessageRecipients);
        } catch (Exception e) {
            if (!refs.serverIsStopping()) {
                throw e;
            }
        }
    }

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component component, @NotNull Component component1) {
        return component
                .append(Component.text(":"))
                .append(Component.space())
                .append(component1)
                .append(Component.space())
                .append(Component.text("\uD83C\uDF10", NamedTextColor.LIGHT_PURPLE));
    }
}
