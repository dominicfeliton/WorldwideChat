package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;

import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.CommonRefs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class WWCTranslateGuiSourceLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();

	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = "";

	private Player inPlayer;

	// TODO: Rewrite for being able to switch between localizations??
	// TODO: Also fix getMsg calls to respect user's localization
	public WWCTranslateGuiSourceLanguage(String selectedSourceLanguage, String targetPlayerUUID, Player inPlayer) {
		this.selectedSourceLanguage = selectedSourceLanguage;
		this.targetPlayerUUID = targetPlayerUUID;
		this.inPlayer = inPlayer;
	}

	public SmartInventory getSourceLanguageInventory() {
		return SmartInventory.builder().id("translateSourceLanguage")
				.provider(this).size(6, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + refs.getMsg("wwctGUINewTranslationSource", inPlayer))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive, yellow if player has existing translation session */
			invManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			if (main.isActiveTranslator(targetPlayerUUID)) {
				invManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
			}
			
			/* Init current active translator */
			ActiveTranslator currTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			/* Pagination: Lets you generate pages rather than set defined ones */
			Pagination pagination = contents.pagination();
			ClickableItem[] listOfAvailableLangs = new ClickableItem[main.getSupportedInputLangs().size()];
			
			/* Add each supported language from each respective translator */
			for (int i = 0; i < main.getSupportedInputLangs().size(); i++) {
				ItemStack itemForLang = XMaterial.BOOK.parseItem();
				ItemMeta itemForLangMeta = itemForLang.getItemMeta();
				SupportedLang currLang = main.getSupportedInputLangs().get(i);
				SupportedLang userLang = refs.getSupportedLang(currTranslator.getInLangCode(), "in");
				
				/* Add Glow Effect */
				ArrayList<String> lore = new ArrayList<>();
				if (selectedSourceLanguage.equalsIgnoreCase(currLang.getLangCode()) || selectedSourceLanguage.equalsIgnoreCase(currLang.getLangName())) {
					invManager.addGlowEffect(itemForLangMeta);
					lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + refs.getMsg("wwctGUISourceTranslationSelected", inPlayer));
				} else if (userLang.getLangCode().equalsIgnoreCase(currLang.getLangCode()) || userLang.getLangName().equalsIgnoreCase(currLang.getLangName())) {
					invManager.addGlowEffect(itemForLangMeta);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwctGUISourceOrTargetTranslationAlreadyActive", inPlayer));
				} else if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getInLangCode().equalsIgnoreCase(currLang.getLangCode())) {
					// If global translate is already using this as an inLang...
					invManager.addGlowEffect(itemForLangMeta);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwctGUISourceOrTargetTranslationAlreadyGlobal", inPlayer));
				}
				itemForLangMeta.setDisplayName(currLang.getLangName());
				if (!currLang.getNativeLangName().isEmpty() && !currLang.getNativeLangName().equalsIgnoreCase(currLang.getLangName())) {
					lore.add(currLang.getNativeLangName());
				}
				lore.add(currLang.getLangCode());
				itemForLangMeta.setLore(lore);
				itemForLang.setItemMeta(itemForLangMeta);
				String thisLangCode = currLang.getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(itemForLang, e -> {
					new WWCTranslateGuiTargetLanguage(thisLangCode, targetPlayerUUID, inPlayer).getTargetLanguageInventory()
							.open(player);
				});
			}

			/* 28 langs per page, start at 1, 1 */
			pagination.setItems(listOfAvailableLangs);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

			/* Bottom Middle Option: Auto-detect Source Language */
			/* Disabled for Amazon Translate */
			if (!main.getTranslatorName().equalsIgnoreCase("Amazon Translate")) {
				ItemStack skipSourceButton = XMaterial.BOOKSHELF.parseItem();
				ItemMeta skipSourceMeta = skipSourceButton.getItemMeta();
				skipSourceMeta.setDisplayName(ChatColor.YELLOW
						+ refs.getMsg("wwctGUIAutoDetectButton", inPlayer));
				
				/* Add Glow Effect */
				ArrayList<String> lore = new ArrayList<>();
				if ((currTranslator.getInLangCode().equals("None"))) {
					invManager.addGlowEffect(skipSourceMeta);
					lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + refs.getMsg("wwctGUISourceTranslationSelected", inPlayer));
				} else if (selectedSourceLanguage.equalsIgnoreCase("None")) {
					invManager.addGlowEffect(skipSourceMeta);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwctGUISourceOrTargetTranslationAlreadyActive", inPlayer));
				}
				skipSourceButton.setItemMeta(skipSourceMeta);
				contents.set(5, 4, ClickableItem.of(skipSourceButton, e -> new WWCTranslateGuiTargetLanguage("None", targetPlayerUUID, inPlayer)
						.getTargetLanguageInventory().open(player)));
			}

			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {getSourceLanguageInventory()});
			} else {
				invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu()});
			}
			
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
	public void update(Player player, InventoryContents contents) {
		invManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}

}
