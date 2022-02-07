package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SQLUtils;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class StorageSettingsGUI implements InventoryProvider {

	public static final SmartInventory storageSettings = SmartInventory.builder().id("storageSettingsMenu")
			.provider(new StorageSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIStorageSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* White stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			
			/* First Button: SQL Settings */
			WWCInventoryManager.genericOpenSubmenuButton(1, 1, player, contents, SQLUtils.isConnected(), "wwcConfigGUISQLMenuButton", SQLSettingsGUI.sqlSettings);
			
			/* Bottom Left Option: Previous Page */
			WWCInventoryManager.setCommonButton(2, 2, player, contents, "Previous", new Object[] {GeneralSettingsGUI.generalSettings});

			/* Bottom Middle Option: Quit */
			WWCInventoryManager.setCommonButton(2, 4, player, contents, "Quit");
			
			/* Bottom Right Option: Next Page */
			WWCInventoryManager.setCommonButton(2, 6, player, contents, "Next", new Object[] {ChatSettingsGUI.chatSettings});
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(2, 8, player, contents, "Page Number", new String[] {"2"});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
