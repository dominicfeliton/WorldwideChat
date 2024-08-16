package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.conversations.configuration.ChatSettingsConvos;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlacklistGui implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private Player inPlayer;
	private Set<String> blacklist;

	public BlacklistGui(Player inPlayer) {
		this.inPlayer = inPlayer;
		this.blacklist = main.getBlacklistTerms();
	}

	public SmartInventory getBlacklist() {
		return SmartInventory.builder().id("blacklistMenu")
				.provider(this).size(6, 9)
				.manager(invManager)
				.title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIModifyBlacklistSettings", new String[] {}, inPlayer))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			// TODO: Make sure new terms are immediately added.
			// Fix this on override messages as well.
			// Also TODO: Make sure that save operations are safe on both overrides/blacklist. Schedule an async>sync task if necessary.
			// TODO: Add save button on override main page + modify main page.
			/* Green stained glass borders */
			invManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);
			
			/* Pagination */
			Pagination pagination = contents.pagination();
			ClickableItem[] currentTerms = new ClickableItem[blacklist.size()];
			
			if (!blacklist.isEmpty()) {
				refs.debugMsg("Adding existing blacklist terms to inventory! Amount of terms: " + currentTerms.length);
				int currSpot = 0;
				for (String term : blacklist) {
					ItemStack currentEntry = XMaterial.WRITABLE_BOOK.parseItem();
					ItemMeta currentEntryMeta = currentEntry.getItemMeta();
					
					currentEntryMeta.setDisplayName(term);
					currentEntry.setItemMeta(currentEntryMeta);
					currentTerms[currSpot] = ClickableItem.of(currentEntry, e -> {
						// Open Specific Override GUI
						new BlacklistModifyGui(term, inPlayer).modifyTerm().open(player);
					});
					currSpot++;
				}
			}
			
			/* 28 messages per page, start at 1, 1 */
			pagination.setItems(currentTerms);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

			/* Save Button */
			invManager.setCommonButton(5, 0, player, contents,"Quit");

			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				invManager.setCommonButton(5, 2, player, contents, "Previous");
			} else {
				invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{MenuGui.CONFIG_GUI_TAGS.CHAT_SET.smartInv});
			}
			
			/* Bottom Middle Option: Add new override */
			invManager.genericConversationButton(5 ,4, player, contents, new ChatSettingsConvos.AddBlacklistTerm(getBlacklist()), XMaterial.WRITABLE_BOOK, "wwcConfigGUIChatMessagesBlacklistNewButton");
			
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
