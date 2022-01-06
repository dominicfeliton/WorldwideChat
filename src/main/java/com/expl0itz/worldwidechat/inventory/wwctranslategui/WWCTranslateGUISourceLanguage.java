package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class WWCTranslateGUISourceLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = "";

	public WWCTranslateGUISourceLanguage(String s, String targetPlayerUUID) {
		selectedSourceLanguage = s;
		this.targetPlayerUUID = targetPlayerUUID;
	}

	public static SmartInventory getSourceLanguageInventory(String s, String targetPlayerUUID) {
		return SmartInventory.builder().id("translateSourceLanguage")
				.provider(new WWCTranslateGUISourceLanguage(s, targetPlayerUUID)).size(6, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUINewTranslationSource"))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive, yellow if player has existing translation session */
			ItemStack customDefaultBorders = XMaterial.WHITE_STAINED_GLASS_PANE.parseItem();
			if (!main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals("")) {
				customDefaultBorders = XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem();
			}
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));
			
			/* Init current active translator */
			ActiveTranslator currTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			/* Pagination: Lets you generate pages rather than set defined ones */
			Pagination pagination = contents.pagination();
			ClickableItem[] listOfAvailableLangs = new ClickableItem[main.getSupportedTranslatorLanguages().size()];

			/* Add each supported language from each respective translator */
			for (int i = 0; i < main.getSupportedTranslatorLanguages().size(); i++) {
				ItemStack currentLang = XMaterial.BOOK.parseItem();
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				/* Add Glow Effect */
				ArrayList<String> lore = new ArrayList<>();
				currentLangMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				if (selectedSourceLanguage.equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					currentLangMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
					lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceTranslationSelected"));
				} else if (currTranslator.getInLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					currentLangMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceOrTargetTranslationAlreadyActive"));
				}
				currentLangMeta.setDisplayName(main.getSupportedTranslatorLanguages().get(i).getLangName());
				if (!main.getSupportedTranslatorLanguages().get(i).getNativeLangName().equals("")) {
					lore.add(main.getSupportedTranslatorLanguages().get(i).getNativeLangName());
				}
				lore.add(main.getSupportedTranslatorLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String thisLangCode = main.getSupportedTranslatorLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, e -> {
					WWCTranslateGUITargetLanguage.getTargetLanguageInventory(thisLangCode, targetPlayerUUID)
							.open(player);
				});
			}

			/* 28 langs per page, start at 1, 1 */
			pagination.setItems(listOfAvailableLangs);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				contents.set(5, 2, ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"), e -> {
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
							pagination.previous().getPage());
				}));
			} else {
				contents.set(5, 2, ClickableItem.of(WWCInventoryManager.getCommonButton("Previous"), e -> {
					WWCTranslateGUIMainMenu.getTranslateMainMenu(targetPlayerUUID).open(player);
				}));
			}

			/* Bottom Middle Option: Auto-detect Source Language */
			/* Disabled for Amazon Translate */
			if (!main.getTranslatorName().equalsIgnoreCase("Amazon Translate")) {
				ItemStack skipSourceButton = XMaterial.BOOKSHELF.parseItem();
				ItemMeta skipSourceMeta = skipSourceButton.getItemMeta();
				skipSourceMeta.setDisplayName(ChatColor.YELLOW
						+ CommonDefinitions.getMessage("wwctGUIAutoDetectButton"));
				
				/* Add Glow Effect */
				skipSourceMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				ArrayList<String> lore = new ArrayList<>();
				if ((currTranslator.getInLangCode().equals("None"))) {
					skipSourceMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
					lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceTranslationSelected"));
				} else if (selectedSourceLanguage.equalsIgnoreCase("None")) {
					skipSourceMeta.addEnchant(XEnchantment.matchXEnchantment("power").get().getEnchant(), 1, false);
					lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceOrTargetTranslationAlreadyActive"));
				}
				skipSourceButton.setItemMeta(skipSourceMeta);
				contents.set(5, 4, ClickableItem.of(skipSourceButton, e -> WWCTranslateGUITargetLanguage
						.getTargetLanguageInventory("None", targetPlayerUUID).open(player)));
			}

			/* Bottom Right Option: Next Page */
			if (!pagination.isLast()) {
				contents.set(5, 6, ClickableItem.of(WWCInventoryManager.getCommonButton("Next"), e -> {
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
							pagination.next().getPage());
				}));
				;
			}
			
			/* Last Option: Page Number */
			contents.set(5, 8, ClickableItem.of(WWCInventoryManager.getCommonButton("Page Number", new String[] {pagination.getPage() + 1 + ""}), e -> {}));
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		WWCInventoryManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}

}
