package com.badskater0729.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	//@EventHandler(priority = EventPriority.HIGHEST)
	// TODO: Make sure LOWEST is okay
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		try {
			if (!event.isAsynchronous()) {
				refs.debugMsg("chat event not async, skipping");
				return;
			}

			// Original WWC functionality/Translate Outgoing Messages
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer());
			String currInLang = currTranslator.getInLangCode();
			String currOutLang = currTranslator.getOutLangCode();
			if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
					|| (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				String eventText = refs.translateText(event.getMessage(), event.getPlayer());
				event.setMessage(eventText);
				refs.debugMsg("Setting message to \"" + eventText + "\"...");
			}
			
			// New WWC functionality/Translate Incoming Messages
			Set<Player> unmodifiedMessageRecipients = new HashSet<Player>();
			for (Player eaRecipient : event.getRecipients()) {
				ActiveTranslator testTranslator = main.getActiveTranslator(eaRecipient.getUniqueId());
				String testInLang = testTranslator.getInLangCode();
				String testOutLang = testTranslator.getOutLangCode();
				if ((   // Check if this translator wants their incoming messages to be translated
						!currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(eaRecipient) && testTranslator.getTranslatingChatIncoming())
						// Check if a previously outgoing translation was NOT what this user wants
						&& !(currInLang.equals(testInLang) && currOutLang.equals(testOutLang))) {

					// Translate message + get original format
					String translation = refs.translateText(event.getMessage() + " (Translated)", eaRecipient);
					String outMessageWithoutHover = String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation);

					// Convert to TextComponent
					TextComponent hoverOutMessage = Component.text()
							.content(outMessageWithoutHover)
							.build();
					if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat")) {
						hoverOutMessage = Component.text()
								.content(outMessageWithoutHover)
								.hoverEvent(HoverEvent.showText(Component.text(event.getMessage()).decorate(TextDecoration.ITALIC)))
								.build();
					}
					try {
						main.adventure().sender(eaRecipient).sendMessage(hoverOutMessage);
					} catch (IllegalStateException e) {}
				} else {
					unmodifiedMessageRecipients.add(eaRecipient);
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