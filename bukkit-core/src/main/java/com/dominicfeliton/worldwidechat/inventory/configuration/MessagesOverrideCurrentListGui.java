package com.dominicfeliton.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

public class MessagesOverrideCurrentListGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private String inLang;
	private Player inPlayer;

	public MessagesOverrideCurrentListGui(String inLang, Player inPlayer) {
		this.inLang = inLang;
		this.inPlayer = inPlayer;
	}

	public SmartInventory getOverrideMessagesSettings() {
		return SmartInventory.builder().id("overrideMessagesMenu")
				.provider(this).size(6, 9)
				.manager(invManager)
				.title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIChatMessagesOverrideSettings", new String[] {inLang}, inPlayer))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Green stained glass borders */
			invManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);
			
			/* Pagination */
			Pagination pagination = contents.pagination();
			TreeMap<String, String> overridesFromConfig = new TreeMap<>();
			ClickableItem[] currentOverrides = new ClickableItem[0];
			FileConfiguration messagesConfig = main.getConfigManager().getCustomMessagesConfig(inLang);
			
			if (messagesConfig.getConfigurationSection("Overrides") != null) {
				for (String eaKey : messagesConfig.getConfigurationSection("Overrides").getKeys(true)) {
					overridesFromConfig.put(eaKey, messagesConfig.getString("Overrides." + eaKey));
				}
				currentOverrides = new ClickableItem[overridesFromConfig.size()];
			}
			
			if (!overridesFromConfig.isEmpty()) {
				refs.debugMsg("Adding existing overrides to inventory! Amount of overrides: " + currentOverrides.length);
				int currSpot = 0;
				for (Map.Entry<String, String> entry : overridesFromConfig.entrySet()) {
					ItemStack currentEntry = XMaterial.WRITABLE_BOOK.parseItem();
					ItemMeta currentEntryMeta = currentEntry.getItemMeta();
					
					currentEntryMeta.setDisplayName(entry.getKey());
					ArrayList<String> lore = new ArrayList<>();
					lore.add(refs.getMsg("wwcConfigGUIMessagesOverrideOriginalLabel", inPlayer) + ": " + (messagesConfig.getString("Messages." + entry.getKey()) != null ? messagesConfig.getString("Messages." + entry.getKey()) : refs.getMsg("wwcConfigGUIChatMessagesDeadOverride", inPlayer)));
					lore.add(refs.getMsg("wwcConfigGUIMessagesOverrideCustomLabel", inPlayer) + ": " + entry.getValue());
					currentEntryMeta.setLore(lore);
					currentEntry.setItemMeta(currentEntryMeta);
					currentOverrides[currSpot] = ClickableItem.of(currentEntry, e -> {
						// Open Specific Override GUI
						new MessagesOverrideModifyGui(entry.getKey(), inLang, inPlayer).getModifyCurrentOverride().open(player);
					});
					currSpot++;
				}
			}
			
			/* 28 messages per page, start at 1, 1 */
			pagination.setItems(currentOverrides);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));
			
			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				invManager.setCommonButton(5, 2, player, contents, "Previous");
			} else {
				invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{new MessagesOverridePickLangGui().getMessagesOverridePickLangGui()});
			}
			
			/* Bottom Middle Option: Add new override */
			invManager.genericOpenSubmenuButton(5, 4, player, contents, "wwcConfigGUIChatMessagesOverrideNewButton", new MessagesOverridePossibleListGui(inLang, inPlayer).getOverrideNewMessageSettings());
			
			/* Bottom Right Option: Next Page */
			if (!pagination.isLast()) {
				invManager.setCommonButton(5, 6, player, contents, "Next");
			}
			
			/* Last Option: Page Number */
			invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
