package com.expl0itz.worldwidechat.inventory;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import org.bukkit.scheduler.BukkitRunnable;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.content.InventoryContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WWCInventoryManager extends InventoryManager {
	
	public WWCInventoryManager(JavaPlugin plugin) {
		super(plugin);
	}
	
	public static void checkIfPlayerIsMissing(Player player, String targetPlayerUUID) {
		if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			if (Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
				// Target player no longer online
				player.closeInventory();
				final TextComponent targetPlayerDC = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwctGUITargetPlayerNull"))
								.color(NamedTextColor.RED).decorate(TextDecoration.ITALIC))
						.build();
				CommonDefinitions.sendMessage(player, targetPlayerDC);
			}
		}
	}
	
	public static void inventoryError(Player player, Exception e) {
		final TextComponent inventoryError = Component.text()
				.append(Component.text().content(
						CommonDefinitions.getMessage("wwcInventoryErrorPlayer"))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(player, inventoryError);
		WorldwideChat.getInstance().getLogger().severe(CommonDefinitions.getMessage("wwcInventoryError", new String[] {player.getName()}));
		e.printStackTrace();
	}
	
	public static ItemStack getCommonButton(String buttonType) {
		ItemStack pageButton = XMaterial.WHITE_STAINED_GLASS.parseItem();
		ItemMeta pageMeta = pageButton.getItemMeta();
		if (buttonType.equalsIgnoreCase("Previous")) {
			pageButton = XMaterial.RED_STAINED_GLASS.parseItem();
			pageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUIPreviousPageButton"));
		} else if (buttonType.equalsIgnoreCase("Next")) {
			pageButton = XMaterial.GREEN_STAINED_GLASS.parseItem();
			pageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
		} else {
			pageMeta.setDisplayName(ChatColor.RED + "Not a valid button!");
		}
		
		pageButton.setItemMeta(pageMeta);
		return pageButton;
	}
	
	public static ItemStack getSaveMainConfigButton() {
		ItemStack quitButton = XMaterial.BARRIER.parseItem();
		ItemMeta quitMeta = quitButton.getItemMeta();
		quitMeta.setDisplayName(ChatColor.RED
				+ CommonDefinitions.getMessage("wwcConfigGUIQuitButton"));
		quitButton.setItemMeta(quitMeta);
		return quitButton;
	}

	public static void saveMainConfigAndReload(Player player, InventoryContents content) {
		WorldwideChat.getInstance().removePlayerUsingConfigurationGUI(player);
		player.closeInventory();
		new BukkitRunnable() {
			@Override
			public void run() {
				// Save config sync/in the same thread because we are already in another thread thanks to Bukkit Scheduler
				WorldwideChat.getInstance().getConfigManager().saveMainConfig(false);
				
				// Reload
				WorldwideChat.getInstance().reload(player);
			}
		}.runTaskAsynchronously(WorldwideChat.getInstance());
	}
	
}
