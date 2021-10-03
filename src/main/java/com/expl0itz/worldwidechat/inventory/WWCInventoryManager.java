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
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.InventoryManager;
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

}
