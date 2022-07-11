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
			if ((main.isActiveTranslator(event.getPlayer()) && currTranslator.getTranslatingChatOutgoing())
					|| (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChatOutgoing())) {
				String outMsg = CommonDefinitions.translateText(event.getMessage(), event.getPlayer());
				event.setMessage(outMsg);
			}
			
			/* New WWC functionality/Incoming Messages */
			List<Player> unmodifiedMessageRecipients = new ArrayList<Player>();
			for (Player eaRecipient : event.getRecipients()) {
				ActiveTranslator testTranslator = main.getActiveTranslator(eaRecipient.getUniqueId());
				if ((   /* Check if this testTranslator wants their incoming messages to be translated */
						!currTranslator.getUUID().equals(testTranslator.getUUID()) && main.isActiveTranslator(eaRecipient) && testTranslator.getTranslatingChatIncoming())
						/* Check if this testTranslator doesn't already want the current chat message */
						&& !(currTranslator.getInLangCode().equals(testTranslator.getInLangCode())
								&& currTranslator.getOutLangCode().equals(testTranslator.getOutLangCode()))) {
					
					/* Send the message in a new task, to avoid delaying the chat message for others */
					BukkitRunnable chatHover = new BukkitRunnable() {
						@Override
						public void run() {
							String translation = CommonDefinitions.translateText(event.getMessage() + " (Translated)", eaRecipient);
							if (translation.contains(event.getMessage())) {
								translation = event.getMessage();
							}
							String outMessageWithoutHover = String.format(event.getFormat(), event.getPlayer().getDisplayName(), translation);
							
							TextComponent hoverOutMessage = Component.text()
									.content(outMessageWithoutHover)
									.build();
			                if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendIncomingHoverTextChat") && !(translation.equals(event.getMessage()))) {
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
					CommonDefinitions.scheduleTaskAsynchronously(chatHover);
				} else {
					unmodifiedMessageRecipients.add(eaRecipient);
				}
			}
			event.getRecipients().clear();
			event.getRecipients().addAll(unmodifiedMessageRecipients);
		} catch (Exception e) {
			if (!CommonDefinitions.serverIsStopping()) {
				throw e;
			}
			//CommonDefinitions.sendDebugMessage("We are reloading! Caught exception in ChatListener...");
			//CommonDefinitions.sendDebugMessage(ExceptionUtils.getStackTrace(e));
		}
	}
}