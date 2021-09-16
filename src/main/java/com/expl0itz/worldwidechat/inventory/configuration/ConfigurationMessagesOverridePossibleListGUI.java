package com.expl0itz.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.ChatSettingsModifyOverrideTextConversation;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class ConfigurationMessagesOverridePossibleListGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public static final SmartInventory overrideNewMessageSettings = SmartInventory.builder().id("overridePossibilitiesMenu")
			.provider(new ConfigurationMessagesOverridePossibleListGUI()).size(6, 9)
			.manager(WorldwideChat.getInstance().getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatMessagesPossibleOverrides"))
	        .build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Pagination */
			Pagination pagination = contents.pagination();
			HashMap<String, String> messagesFromConfig = new HashMap<String, String>();
			ClickableItem[] currentMessages = new ClickableItem[0];
			FileConfiguration messagesConfig = main.getConfigManager().getMessagesConfig();
			
			for (String eaKey : messagesConfig.getConfigurationSection("Messages").getKeys(true)) {
				messagesFromConfig.put(eaKey, messagesConfig.getString("Messages." + eaKey));
			}
			currentMessages = new ClickableItem[messagesFromConfig.size()];
			
			int currSpot = 0;
			CommonDefinitions.sendDebugMessage("Adding all possible messages to inventory! Amount of messages: " + currentMessages.length);
			for (Map.Entry<String, String> entry : messagesFromConfig.entrySet()) {
				/* Init item, ensure pre-1.14 compatibility */
				ItemStack currentEntry = XMaterial.OAK_SIGN.parseItem();
				ItemMeta currentEntryMeta = currentEntry.getItemMeta();
				
				currentEntryMeta.setDisplayName(entry.getKey());
				ArrayList<String> lore = new ArrayList<>();
				if (messagesConfig.getString("Overrides." + entry.getKey()) != null) {
					EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
					currentEntryMeta.addEnchant(glow, 1, true);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwcConfigGUIMessagesAlreadyOverriden"));
				}
				lore.add(CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideOriginalLabel") + ": " + messagesConfig.getString("Messages." + entry.getKey()));
				currentEntryMeta.setLore(lore);
				currentEntry.setItemMeta(currentEntryMeta);
				currentMessages[currSpot] = ClickableItem.of(currentEntry, e -> {
					// Start conversation
					ConversationFactory textConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new ChatSettingsModifyOverrideTextConversation(ConfigurationMessagesOverridePossibleListGUI.overrideNewMessageSettings, entry.getKey()));
				    textConvo.buildConversation(player).begin();
				});
				currSpot++;
			}
			
			/* 45 messages per page, start at 0, 0 */
			pagination.setItems(currentMessages);
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
					overrideNewMessageSettings.open(player,
							pagination.previous().getPage());
				}));
			} else {
				contents.set(5, 2, ClickableItem.of(previousPageButton, e -> {
					ConfigurationMessagesOverrideCurrentListGUI.overrideMessagesSettings.open(player);
				}));
			}
			
			/* Bottom Right Option: Next Page */
			ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta nextPageMeta = nextPageButton.getItemMeta();
			nextPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
			nextPageButton.setItemMeta(nextPageMeta);
			if (!pagination.isLast()) {
				contents.set(5, 6, ClickableItem.of(nextPageButton, e -> {
					overrideNewMessageSettings.open(player,
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
