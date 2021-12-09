package com.expl0itz.worldwidechat.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class OnPlayerJoinListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoinListener(PlayerJoinEvent event) {
		// Check if plugin has updates
		if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendPluginUpdateChat")) && (main.getOutOfDate())
				&& (event.getPlayer().hasPermission("worldwidechat.chatupdate"))) {
			final TextComponent outOfDate = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcUpdaterOutOfDateChat"))
							.color(NamedTextColor.YELLOW))
					.append(Component.text().content(" (").color(NamedTextColor.GOLD))
					.append(Component.text().content("https://github.com/3xpl0itz/WorldwideChat/releases")
							.color(NamedTextColor.GOLD)
							.clickEvent(ClickEvent.openUrl("https://github.com/3xpl0itz/WorldwideChat/releases"))
							.decoration(TextDecoration.UNDERLINED, true))
					.append(Component.text().content(")").color(NamedTextColor.GOLD)).build();
			CommonDefinitions.sendMessage(event.getPlayer(), outOfDate);
		}

		/* Global translate is disabled, and user has a translation config */
		if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
				&& main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
				&& !main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("")) {
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
			if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
				final TextComponent langToLang = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcOnJoinTranslationNotificationSourceLang", new String[] {currTranslator.getInLangCode(), currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), langToLang);
			} else {
				final TextComponent noSource = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcOnJoinTranslationNotificationNoSourceLang", new String[] {currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), noSource);
			}
		/* Global translate is enabled, and user does not have a translation config */
		} else if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
				&& !main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
				&& main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("")) {
			ActiveTranslator currTranslator = main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED");
			if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
				final TextComponent langToLang = Component.text().append(main.getPluginPrefix().asComponent())
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcGlobalOnJoinTranslationNotificationSourceLang", new String[] {currTranslator.getInLangCode(), currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), langToLang);
			} else {
				final TextComponent noSource = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcGlobalOnJoinTranslationNotificationNoSourceLang", new String[] {currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), noSource);
			}
		/* Global translate is enabled, but user has a translation config */
		} else if ((main.getConfigManager().getMainConfig().getBoolean("Chat.sendTranslationChat"))
				&& !main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")
				&& !main.getActiveTranslator(event.getPlayer().getUniqueId().toString()).getUUID().equals("")) {
			ActiveTranslator currTranslator = main.getActiveTranslator(event.getPlayer().getUniqueId().toString());
			if (!currTranslator.getInLangCode().equalsIgnoreCase("None")) {
				final TextComponent langToLang = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcOverrideGlobalOnJoinTranslationNotificationSourceLang", new String[] {currTranslator.getInLangCode(), currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), langToLang);
			} else {
				final TextComponent noSource = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcOverrideGlobalOnJoinTranslationNotificationNoSourceLang", new String[] {currTranslator.getOutLangCode()}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(event.getPlayer(), noSource);
			}
		}
	}
}
