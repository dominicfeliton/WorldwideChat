package com.badskater0729.worldwidechat.inventory.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.conversations.configuration.ChatSettingsConvos;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class MessagesOverridePossibleListGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	String inLang = "";

	Player inPlayer;

	public MessagesOverridePossibleListGui(String inLang, Player inPlayer) {
		this.inLang = inLang;
		this.inPlayer = inPlayer;
	}

	public SmartInventory getOverrideNewMessageSettings() {
		return SmartInventory.builder().id("overridePossibilitiesMenu")
				.provider(this).size(6, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIChatMessagesPossibleOverrides", inPlayer))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Yellow stained glass borders */
			invManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
			
			/* Pagination */
			Pagination pagination = contents.pagination();
			HashMap<String, String> messagesFromConfig = new HashMap<String, String>();
			ClickableItem[] currentMessages = new ClickableItem[0];
			FileConfiguration messagesConfig = main.getConfigManager().getCustomMessagesConfig(inLang);
			
			for (String eaKey : messagesConfig.getConfigurationSection("Messages").getKeys(true)) {
				messagesFromConfig.put(eaKey, messagesConfig.getString("Messages." + eaKey));
			}
			currentMessages = new ClickableItem[messagesFromConfig.size()];
			
			int currSpot = 0;
			refs.debugMsg("Adding all possible messages to inventory! Amount of messages: " + currentMessages.length);
			for (Map.Entry<String, String> entry : messagesFromConfig.entrySet()) {
				/* Init item, ensure pre-1.14 compatibility */
				ItemStack currentEntry = XMaterial.OAK_SIGN.parseItem();
				ItemMeta currentEntryMeta = currentEntry.getItemMeta();
				
				currentEntryMeta.setDisplayName(entry.getKey());
				ArrayList<String> lore = new ArrayList<>();
				if (messagesConfig.getString("Overrides." + entry.getKey()) != null) {
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwcConfigGUIMessagesAlreadyOverriden", null));
					invManager.addGlowEffect(currentEntryMeta);
				}
				lore.add(refs.getMsg("wwcConfigGUIMessagesOverrideOriginalLabel", null) + ": " + messagesConfig.getString("Messages." + entry.getKey()));
				currentEntryMeta.setLore(lore);
				currentEntry.setItemMeta(currentEntryMeta);
				currentMessages[currSpot] = ClickableItem.of(currentEntry, e -> {
					// Start conversation
					ConversationFactory textConvo = new ConversationFactory(main).withModality(true)
							.withFirstPrompt(new ChatSettingsConvos.ModifyOverrideText(new MessagesOverridePossibleListGui(inLang, inPlayer).getOverrideNewMessageSettings(), entry.getKey(), inLang));
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
				invManager.setCommonButton(5, 2, player, contents, "Previous");
			} else {
				invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {new MessagesOverrideCurrentListGui(inLang, inPlayer).getOverrideMessagesSettings()});
			}
			
			/* Bottom Right Option: Next Page */
			if (!pagination.isLast()) {
				invManager.setCommonButton(5, 6, player, contents, "Next");
			}
			
			/* Last Option: Current Page Number */
			invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
			
		} catch (Exception e) {
			invManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
