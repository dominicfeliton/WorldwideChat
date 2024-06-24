package com.badskater0729.worldwidechat.listeners;

import com.badskater0729.worldwidechat.WorldwideChatHelper;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import com.badskater0729.worldwidechat.WorldwideChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static com.badskater0729.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class InventoryListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;

	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	private WorldwideChatHelper wwcHelper = main.getServerFactory().getWWCHelper();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		/* Send the user a message if they quit a config GUI without saving properly */
		BukkitRunnable out = new BukkitRunnable() {
			@Override
			public void run() {
				HumanEntity currPlayer = e.getPlayer();
				if (main.isPlayerUsingGUI((Player)currPlayer) && currPlayer.getOpenInventory().getType() != InventoryType.CHEST
						&& !((Player) currPlayer).isConversing()) {
					refs.sendFancyMsg("wwcConfigGUIChangesNotSaved", "", "&e", currPlayer);
					main.removePlayerUsingConfigurationGUI((Player) currPlayer);
				}
			}
		};
		wwcHelper.runSync(true, 10, out, ENTITY, (Player)e.getPlayer());
	}
}
