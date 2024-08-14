package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.listeners.AbstractChatListener;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

public class SpigotChatListener extends AbstractChatListener<AsyncPlayerChatEvent> implements Listener {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	@Override
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		try {
			// TODO: Boilerplate code. Make paper/spigot one class except for raw implementation?
			if (!event.isAsynchronous()) {
				refs.debugMsg("chat event not async, skipping");
				return;
			}
			refs.debugMsg("Begin spigot chatlistener.");
			boolean channel = main.isUseSeparateChatChannel();

			// Translate Outgoing Messages
			refs.debugMsg("Begin translating outgoing messages...");
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
			String currInLang = currTranslator.getInLangCode();
			String currOutLang = currTranslator.getOutLangCode();
			String outgoingText = event.getMessage();
			if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
					|| (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				outgoingText = refs.translateText(event.getMessage(), event.getPlayer());
				if (!main.isUseSeparateChatChannel()) {
					event.setMessage(outgoingText);
				}
			}
			
			// Translate Incoming Messages
			refs.debugMsg("Begin translating incoming messages...");

			Set<Player> unmodifiedMessageRecipients = new HashSet<Player>();
			for (Player eaRecipient : event.getRecipients()) {
				ActiveTranslator testTranslator = main.getActiveTranslator(eaRecipient.getUniqueId());
				String testInLang = testTranslator.getInLangCode();
				String testOutLang = testTranslator.getOutLangCode();

				if (!main.isActiveTranslator(eaRecipient)) {
					refs.debugMsg("Skipping " + eaRecipient.getName() + ", not an active translator!");
					unmodifiedMessageRecipients.add(eaRecipient);
					continue;
				}

				if (currTranslator.getUUID().equals(testTranslator.getUUID())) {
					refs.debugMsg("Skipping " + eaRecipient.getName() + ", can't request an incoming translation of their own message! (player==player)");
					unmodifiedMessageRecipients.add(eaRecipient);
					continue;
				}

				if (!testTranslator.getTranslatingChatIncoming()) {
					refs.debugMsg("Skipping " + eaRecipient.getName() + ", does not want their incoming chat to be translated.");
					unmodifiedMessageRecipients.add(eaRecipient);
					continue;
				}

				if (currInLang.equals(testInLang) && currOutLang.equals(testOutLang)) {
					unmodifiedMessageRecipients.add(eaRecipient);
					refs.debugMsg("Skipping " + eaRecipient.getName() + ", currIn/OutLang == currIn/OutLang (???).");
					continue;
				}

				// If all checks pass, translate an incoming message for the current translator.
				// Translate message + get original format
				String translation = refs.translateText(outgoingText, eaRecipient);
				if (translation.equalsIgnoreCase(event.getMessage())) {
					refs.debugMsg("Translation unsuccessful/same as original message for " + eaRecipient.getName());
					unmodifiedMessageRecipients.add(eaRecipient);
					continue;
				}

				Component incomingMessage = formatMessage(event, eaRecipient, translation, true);
				sendChatMessage(eaRecipient, incomingMessage);
			}
			event.getRecipients().retainAll(unmodifiedMessageRecipients);


			// If we are on a separate chat channel & pending outgoing message, send to remaining recipients
			if (main.isUseSeparateChatChannel() && !outgoingText.equals(event.getMessage())) {
				refs.debugMsg("Sending pending outgoing message...");
				for (Player eaRecipient : event.getRecipients()) {
					Component outgoingMessage = formatMessage(event, eaRecipient, outgoingText, false);
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

	private Component formatMessage(AsyncPlayerChatEvent event, Player eaRecipient, String translation, boolean incoming) {
		Chat chat = main.getChat();
		Component outMsg;
		if (chat != null) {
			// Vault Support
			outMsg = super.getVaultMessage(eaRecipient, event.getPlayer(), event.getMessage(), event.getPlayer().getDisplayName());
		} else {
			// No Vault Support
			TextComponent icon = main.getTranslateIcon();
			outMsg = Component.empty()
					.append(icon)
					.append(refs.deserial(String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation)));
		}

		if (incoming && main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
			outMsg = outMsg
					.hoverEvent(HoverEvent.showText(Component.text(event.getMessage()).decorate(TextDecoration.ITALIC)));
		}

		return outMsg;
	}

	private void sendChatMessage(Player eaRecipient, Component outMessage) {
		if (main.getServerFactory().getServerInfo().getKey().equals("Paper")) {
			// If we are on Paper but using Spigot, we assume that Adventure is not installed.
			// Note that this does not support hover text.
			eaRecipient.sendMessage(refs.serial(outMessage));
		} else {
			try {
				main.adventure().sender(eaRecipient).sendMessage(outMessage);
			} catch (IllegalStateException e) {}
		}
	}
}