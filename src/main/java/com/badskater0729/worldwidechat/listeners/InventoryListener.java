package com.badskater0729.worldwidechat.listeners;

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

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.runSync;

public class InventoryListener implements Listener {

	private WorldwideChat main = WorldwideChat.instance;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryCloseEvent(InventoryCloseEvent e) {
		/* Send the user a message if they quit a config GUI without saving properly */
		BukkitRunnable out = new BukkitRunnable() {
			@Override
			public void run() {
				HumanEntity currPlayer = e.getPlayer();
				if (main.isPlayerUsingGUI(currPlayer.getUniqueId().toString()) && currPlayer.getOpenInventory().getType() != InventoryType.CHEST
						&& !((Player) currPlayer).isConversing()) {
					final TextComponent reloadPlease = Component.text()
							.append(Component.text()
									.content(getMsg("wwcConfigGUIChangesNotSaved"))
									.color(NamedTextColor.YELLOW))
							.build();
					sendMsg(currPlayer, reloadPlease);
					main.removePlayerUsingConfigurationGUI((Player) currPlayer);
				}
			}
		};
		runSync(true, 10, out);
	}
}
