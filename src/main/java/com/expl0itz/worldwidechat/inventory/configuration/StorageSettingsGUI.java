package com.expl0itz.worldwidechat.inventory.configuration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class StorageSettingsGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	public static final SmartInventory storageSettings = SmartInventory.builder().id("storageSettingsMenu")
			.provider(new StorageSettingsGUI()).size(3, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIStorageSettings"))
			.build();

	@Override
	public void init(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub
		
	}

}
