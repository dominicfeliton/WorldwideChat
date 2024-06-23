package com.badskater0729.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class ChatListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		try {
			// TODO: Boilerplate code. Make paper/spigot one class except for raw implementation?
			if (!event.isAsynchronous()) {
				refs.debugMsg("chat event not async, skipping");
				return;
			}
			refs.debugMsg("Begin spigot chatlistener.");

			// Original WWC functionality/Translate Outgoing Messages
			refs.debugMsg("Begin translating outgoing messages...");
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
			String currInLang = currTranslator.getInLangCode();
			String currOutLang = currTranslator.getOutLangCode();
			if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
					|| (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				String eventText = refs.translateText(event.getMessage(), event.getPlayer());
				event.setMessage(eventText);
			}
			
			// New WWC functionality/Translate Incoming Messages
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
				String translation = refs.translateText(event.getMessage(), eaRecipient);
				if (translation.equalsIgnoreCase(event.getMessage())) {
					refs.debugMsg("Translation unsuccessful/same as original message for " + eaRecipient.getName());
					unmodifiedMessageRecipients.add(eaRecipient);
					continue;
				}

				String outMessageWithoutHover = String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation);

				// Convert to TextComponent
				Component hoverOutMessage = refs.deserial(outMessageWithoutHover);
				if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
					hoverOutMessage = Component.text()
							.content(outMessageWithoutHover)
							.hoverEvent(HoverEvent.showText(Component.text(event.getMessage()).decorate(TextDecoration.ITALIC)))
							.build();
				}

				// Add globe icon
				hoverOutMessage = hoverOutMessage
						.append(Component.space())
						.append(Component.text("\uD83C\uDF10", NamedTextColor.LIGHT_PURPLE));

				if (main.getServerFactory().getServerInfo().getKey().equals("Paper")) {
					eaRecipient.sendMessage(refs.serial(hoverOutMessage));
				} else {
					try {
						main.adventure().sender(eaRecipient).sendMessage(hoverOutMessage);
					} catch (IllegalStateException e) {}
				}
			}
			event.getRecipients().retainAll(unmodifiedMessageRecipients);
		} catch (Exception e) {
			if (!refs.serverIsStopping()) {
				throw e;
			}
		}
	}
}