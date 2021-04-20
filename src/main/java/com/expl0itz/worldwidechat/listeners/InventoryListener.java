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
		for (Player eaPlayer : main.getPlayersUsingGUI()) {
			if (eaPlayer.equals(e.getPlayer())) {
				Bukkit.getScheduler().runTaskLaterAsynchronously(main, new Runnable() {
					@Override
					public void run() {
						if (eaPlayer.getOpenInventory().getType() != InventoryType.CHEST && !eaPlayer.isConversing()) {
							final TextComponent reloadPlease = Component.text()
					                .append(main.getPluginPrefix().asComponent())
					                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIChangesNotSaved")).color(NamedTextColor.YELLOW))
					                .build();
					            Audience adventureSender = main.adventure().sender(e.getPlayer());
					        adventureSender.sendMessage(reloadPlease);
					        main.removePlayerUsingGUI(eaPlayer);
						}
					}
				}, 20);
		        break;
			}
		}
	}
	
}
