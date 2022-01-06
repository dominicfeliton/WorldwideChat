package com.expl0itz.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;

public class ChatListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		try {
			/* Original WWC functionality/Outgoing Messages */
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
			if ((!currTranslator.getUUID().equals("") && currTranslator.getTranslatingChatOutgoing())
					|| (!main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				String outMsg = CommonDefinitions.translateText(event.getMessage(), event.getPlayer());
				if (!event.getMessage().equals(outMsg)) {
					event.setMessage(outMsg);
				} else if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendFailedTranslationChat")) {
					TextComponent chatTranslationFail = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwcChatTranslationFail"))
									.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
							.build();
					CommonDefinitions.sendMessage(event.getPlayer(), chatTranslationFail);
				}
			}
			
			/* New WWC functionality/Incoming Messages */
			CommonDefinitions.sendDebugMessage("Message format: " + event.getFormat());
			List<Player> unmodifiedMessageRecipients = new ArrayList<Player>();
			for (Player eaRecipient : event.getRecipients()) {
				ActiveTranslator testTranslator = main.getActiveTranslator(eaRecipient.getUniqueId());
				if ((   !CommonDefinitions.serverIsStopping()
						/* Check if this testTranslator wants their incoming messages to be translated */
						&& !currTranslator.getUUID().equals(testTranslator.getUUID()) && !testTranslator.getUUID().equals("") && testTranslator.getTranslatingChatIncoming())
						/* Check if this testTranslator doesn't already want the current chat message */
						&& !(!currTranslator.getUUID().equals("") && currTranslator.getInLangCode().equals(testTranslator.getInLangCode())
								&& currTranslator.getOutLangCode().equals(testTranslator.getOutLangCode()))) {
					
					/* Send the message in a new task, to avoid delaying the chat message for others */
					new BukkitRunnable() {
						@Override
						public void run() {
							String translation = CommonDefinitions.translateText(event.getMessage() + " (Translated)", eaRecipient);
							if (translation.contains(event.getMessage())) {
								translation = event.getMessage();
							}
							String outMessageWithoutHover = String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation);
							
							TextComponent hoverOutMessage;
			                if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat") && !(translation.equals(event.getMessage()))) {
								hoverOutMessage = Component.text()
										.content(outMessageWithoutHover)
										.hoverEvent(HoverEvent.showText(Component.text(event.getMessage()).decorate(TextDecoration.ITALIC)))
										.build();
							} else {
								hoverOutMessage = Component.text()
										.content(outMessageWithoutHover)
										.build();
							}
							try {
							    main.adventure().sender(eaRecipient).sendMessage(hoverOutMessage);
							} catch (IllegalStateException e) {
								return;
							}
							
						}
					}.runTaskAsynchronously(main);
				} else {
					unmodifiedMessageRecipients.add(eaRecipient);
				}
			}
			event.getRecipients().clear();
			event.getRecipients().addAll(unmodifiedMessageRecipients);
		} catch (NullPointerException e) {
			//CommonDefinitions.sendDebugMessage("Ran into a NullPointerException while translating chat, server is probably shutting down.");
			//CommonDefinitions.sendDebugMessage(ExceptionUtils.getStackTrace(e));
		}
	}
}