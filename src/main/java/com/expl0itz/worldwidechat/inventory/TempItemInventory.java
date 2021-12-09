package com.expl0itz.worldwidechat.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class TempItemInventory implements InventoryProvider {

	private ItemStack displayedItem;

	public TempItemInventory(ItemStack displayedItem) {
		this.displayedItem = displayedItem;
	}

	public static SmartInventory getTempItemInventory(ItemStack displayedItem) {
		return SmartInventory.builder().id("tempItemMenu").provider(new TempItemInventory(displayedItem)).size(5, 9)
				.manager(WorldwideChat.instance.getInventoryManager()).title(ChatColor.DARK_BLUE + CommonDefinitions.getMessage("wwcGUITempItem"))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		/* Set borders to green */
		ItemStack customDefaultBorders = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
		ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
		defaultBorderMeta.setDisplayName(" ");
		customDefaultBorders.setItemMeta(defaultBorderMeta);
		contents.fillBorders(ClickableItem.empty(customDefaultBorders));

		/* Display item in center */
		contents.set(2, 4, ClickableItem.empty(displayedItem));
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
