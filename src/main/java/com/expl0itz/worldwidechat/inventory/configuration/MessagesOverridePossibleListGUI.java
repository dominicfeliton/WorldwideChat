package com.expl0itz.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.conversations.configuration.ChatSettingsModifyOverrideTextConversation;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class MessagesOverridePossibleListGUI implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	
	public static final SmartInventory overrideNewMessageSettings = SmartInventory.builder().id("overridePossibilitiesMenu")
			.provider(new MessagesOverridePossibleListGUI()).size(6, 9)
			.manager(WorldwideChat.instance.getInventoryManager())
			.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcConfigGUIChatMessagesPossibleOverrides"))
	        .build();
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Yellow stained glass borders */
			WWCInventoryManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
			
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
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwcConfigGUIMessagesAlreadyOverriden"));
					currentEntryMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
					currentEntryMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
				}
				lore.add(CommonDefinitions.getMessage("wwcConfigGUIMessagesOverrideOriginalLabel") + ": " + messagesConfig.getString("Messages." + entry.getKey()));
				currentEntryMeta.setLore(lore);
				currentEntry.setItemMeta(currentEntryMeta);
				currentMessages[currSpot] = ClickableItem.of(currentEntry, e -> {
					// Start conversation
					ConversationFactory textConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new ChatSettingsModifyOverrideTextConversation(MessagesOverridePossibleListGUI.overrideNewMessageSettings, entry.getKey()));
				    textConvo.buildConversation(player).begin();
				});
				currSpot++;
			}
			
			/* 28 messages per page, start at 1, 1 */
			pagination.setItems(currentMessages);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));
			
			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				WWCInventoryManager.setCommonButton(5, 2, player, contents, "Previous");
			} else {
				WWCInventoryManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {MessagesOverrideCurrentListGUI.overrideMessagesSettings});
			}
			
			/* Bottom Right Option: Next Page */
			if (!pagination.isLast()) {
				WWCInventoryManager.setCommonButton(5, 6, player, contents, "Next");
			}
			
			/* Last Option: Current Page Number */
			WWCInventoryManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
			
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
