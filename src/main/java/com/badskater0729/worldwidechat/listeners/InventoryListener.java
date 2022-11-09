package com.badskater0729.worldwidechat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class InventoryListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		/* Send the user a message if they quit a config GUI without saving properly */
		BukkitRunnable out = new BukkitRunnable() {
			@Override
			public void run() {
				if (main.isPlayerUsingGUI(e.getPlayer().getUniqueId().toString()) && e.getPlayer().getOpenInventory().getType() != InventoryType.CHEST
						&& !((Player) e.getPlayer()).isConversing()) {
					final TextComponent reloadPlease = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwcConfigGUIChangesNotSaved"))
									.color(NamedTextColor.YELLOW))
							.build();
					CommonDefinitions.sendMessage(e.getPlayer(), reloadPlease);
					main.removePlayerUsingConfigurationGUI((Player) e.getPlayer());
				}
			}
		};
		CommonDefinitions.scheduleTask(true, 10, out);
	}
}
