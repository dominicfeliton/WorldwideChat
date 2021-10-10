package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatListener implements Listener {

	private WorldwideChat main = WorldwideChat.getInstance();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
		if ((!currTranslator.getUUID().equals("") && currTranslator.getTranslatingChat())
				|| (!main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getTranslatingChat())) {
			String outMsg = CommonDefinitions.translateText(event.getMessage(), event.getPlayer());
			if (!event.getMessage().equals(outMsg)) {
				event.setMessage(outMsg);
			} else if (main.getConfigManager().getMainConfig().getBoolean("Chat.sendFailedTranslationChat")) {
				final TextComponent chatTranslationFail = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcChatTranslationFail"))
								.color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), chatTranslationFail);
			}
		}
	}
}