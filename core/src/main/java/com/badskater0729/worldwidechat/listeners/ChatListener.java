package com.badskater0729.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;

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
			/* Original WWC functionality/Translate Outgoing Messages */
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
			String currInLang = currTranslator.getInLangCode();
			String currOutLang = currTranslator.getOutLangCode();
			if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
					|| (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				event.setMessage(refs.translateText(event.getMessage(), event.getPlayer()));
			}
			
			/* New WWC functionality/Translate Incoming Messages */
			List<Player> unmodifiedMessageRecipients = new ArrayList<Player>();
			for (Player eaRecipient : event.getRecipients()) {
				ActiveTranslator testTranslator = main.getActiveTranslator(eaRecipient.getUniqueId());
				String testInLang = testTranslator.getInLangCode();
				String testOutLang = testTranslator.getOutLangCode();
				if ((   /* Check if this testTranslator wants their incoming messages to be translated */
						!currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(eaRecipient) && testTranslator.getTranslatingChatIncoming())
						/* Check if this testTranslator doesn't already want the current chat message */
						&& !(currInLang.equals(testInLang) && currOutLang.equals(testOutLang))) {
					/* Send the message in a new task, to avoid delaying the chat message for others */
					BukkitRunnable chatHover = new BukkitRunnable() {
						@Override
						public void run() {
							String translation = refs.translateText(event.getMessage() + " (Translated)", eaRecipient);
							String outMessageWithoutHover = String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation);
							
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
						}
					};
					refs.runAsync(chatHover);
				} else {
					unmodifiedMessageRecipients.add(eaRecipient);
				}
			}
			event.getRecipients().clear();
			event.getRecipients().addAll(unmodifiedMessageRecipients);
		} catch (Exception e) {
			if (!refs.serverIsStopping()) {
				throw e;
			}
		}
	}
}