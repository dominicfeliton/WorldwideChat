package com.expl0itz.worldwidechat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class InventoryListener implements Listener {

	private WorldwideChat main = WorldwideChat.getInstance();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		/* Send the user a message if they quit a config GUI without saving properly */
		if (main.getPlayersUsingGUI().contains(e.getPlayer())) {
			Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					if (e.getPlayer().getOpenInventory().getType() != InventoryType.CHEST
							&& !((Player) e.getPlayer()).isConversing()) {
						final TextComponent reloadPlease = Component.text().append(main.getPluginPrefix().asComponent())
								.append(Component.text()
										.content(main.getConfigManager().getMessagesConfig()
												.getString("Messages.wwcConfigGUIChangesNotSaved"))
										.color(NamedTextColor.YELLOW))
								.build();
						Audience adventureSender = main.adventure().sender(e.getPlayer());
						adventureSender.sendMessage(reloadPlease);
						main.removePlayerUsingConfigurationGUI((Player) e.getPlayer());
					}
				}
			}, 10);
		}
	}
}
