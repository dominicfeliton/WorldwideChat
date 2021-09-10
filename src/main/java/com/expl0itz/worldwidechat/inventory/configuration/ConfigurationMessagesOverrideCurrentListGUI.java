package com.expl0itz.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class ConfigurationMessagesOverrideCurrentListGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory overrideMessagesSettings = SmartInventory.builder().id("overrideMessagesMenu")
			.provider(new ConfigurationMessagesOverrideCurrentListGUI()).size(6, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatMessagesOverrideSettings"))
	        .build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Pagination */
			Pagination pagination = contents.pagination();
			HashMap<String, String> overridesFromConfig = new HashMap<String, String>();
			ClickableItem[] currentOverrides = new ClickableItem[0];
			FileConfiguration messagesConfig = main.getConfigManager().getMessagesConfig();
			
			if (messagesConfig.getConfigurationSection("Overrides") != null) {
				for (String eaKey : messagesConfig.getConfigurationSection("Overrides").getKeys(true)) {
					overridesFromConfig.put(eaKey, messagesConfig.getString("Overrides." + eaKey));
				}
				currentOverrides = new ClickableItem[overridesFromConfig.size()];
			}
			
			if (!overridesFromConfig.isEmpty()) {
				CommonDefinitions.sendDebugMessage("Adding existing overrides to inventory! Amount of overrides: " + currentOverrides.length);
				int currSpot = 0;
				for (Map.Entry<String, String> entry : overridesFromConfig.entrySet()) {
					ItemStack currentEntry = new ItemStack(Material.WRITABLE_BOOK);
					ItemMeta currentEntryMeta = currentEntry.getItemMeta();
					
					currentEntryMeta.setDisplayName(entry.getKey());
					ArrayList<String> lore = new ArrayList<>();
					lore.add(CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideOriginalLabel") + ": " + (messagesConfig.getString("Messages." + entry.getKey()) != null ? messagesConfig.getString("Messages." + entry.getKey()) : CommonDefinitions.getMessage("wwcConfigGUIChatMessagesDeadOverride")));
					lore.add(CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideCustomLabel") + ": " + entry.getValue());
					currentEntryMeta.setLore(lore);
					currentEntry.setItemMeta(currentEntryMeta);
					currentOverrides[currSpot] = ClickableItem.of(currentEntry, e -> {
						// Open Specific Override GUI
						ConfigurationMessagesOverrideModifyGUI.getModifyCurrentOverride(entry.getKey()).open(player);
					});
					currSpot++;
				}
			}
			
			/* 45 messages per page, start at 0, 0 */
			pagination.setItems(currentOverrides);
			pagination.setItemsPerPage(45);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
			
			/* Bottom Left Option: Previous Page */
			ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta previousPageMeta = previousPageButton.getItemMeta();
			previousPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUIPreviousPageButton"));
			previousPageButton.setItemMeta(previousPageMeta);
			if (!pagination.isFirst()) {
				contents.set(5, 2, ClickableItem.of(previousPageButton, e -> {
					overrideMessagesSettings.open(player,
							pagination.previous().getPage());
				}));
			} else {
				contents.set(5, 2, ClickableItem.of(previousPageButton, e -> {
					ConfigurationChatSettingsGUI.chatSettings.open(player);
				}));
			}
			
			/* Bottom Middle Option: Add new override */
			ItemStack addNewOverrideButton = new ItemStack(Material.GREEN_GLAZED_TERRACOTTA);
			ItemMeta addNewOverrideMeta = addNewOverrideButton.getItemMeta();
			addNewOverrideMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUIChatMessagesOverrideNewButton"));
			addNewOverrideButton.setItemMeta(addNewOverrideMeta);
			contents.set(5, 4, ClickableItem.of(addNewOverrideButton, e -> {
				ConfigurationMessagesOverridePossibleListGUI.overrideNewMessageSettings.open(player);
			}));
			
			/* Bottom Right Option: Next Page */
			ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta nextPageMeta = nextPageButton.getItemMeta();
			nextPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
			nextPageButton.setItemMeta(nextPageMeta);
			if (!pagination.isLast()) {
				contents.set(5, 6, ClickableItem.of(nextPageButton, e -> {
					overrideMessagesSettings.open(player,
							pagination.next().getPage());
				}));
				;
			}
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
