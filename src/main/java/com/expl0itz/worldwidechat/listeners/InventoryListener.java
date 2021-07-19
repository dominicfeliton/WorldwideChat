package com.expl0itz.worldwidechat.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class InventoryListener implements Listener {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		/* Send the user a message if they quit a config GUI without saving properly */
		if (!main.isReloading() && main.getPlayersUsingGUI().contains(e.getPlayer())) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(main, new Runnable() {
				@Override
				public void run() {
					if (e.getPlayer().getOpenInventory().getType() != InventoryType.CHEST && !((Player)e.getPlayer()).isConversing()) {
						final TextComponent reloadPlease = Component.text()
				                .append(main.getPluginPrefix().asComponent())
				                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIChangesNotSaved")).color(NamedTextColor.YELLOW))
				                .build();
				            Audience adventureSender = main.adventure().sender(e.getPlayer());
				        adventureSender.sendMessage(reloadPlease);
				        main.removePlayerUsingGUI((Player)e.getPlayer());
					}
				}
			}, 10);
		}
	}
}
