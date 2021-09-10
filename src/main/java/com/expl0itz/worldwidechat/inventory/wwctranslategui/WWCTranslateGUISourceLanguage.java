package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
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

	private WorldwideChat main = WorldwideChat.getInstance();

	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = "";

	public WWCTranslateGUISourceLanguage(String s, String targetPlayerUUID) {
		selectedSourceLanguage = s;
		this.targetPlayerUUID = targetPlayerUUID;
	}

	public static SmartInventory getSourceLanguageInventory(String s, String targetPlayerUUID) {
		return SmartInventory.builder().id("translateSourceLanguage")
				.provider(new WWCTranslateGUISourceLanguage(s, targetPlayerUUID)).size(6, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUINewTranslationSource"))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Init current active translator */
			ActiveTranslator currTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			/* Pagination: Lets you generate pages rather than set defined ones */
			Pagination pagination = contents.pagination();
			ClickableItem[] listOfAvailableLangs = new ClickableItem[main.getSupportedTranslatorLanguages().size()];

			/* Add each supported language from each respective translator */
			for (int i = 0; i < main.getSupportedTranslatorLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.BOOK);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				/* Add Glow Effect */
				ArrayList<String> lore = new ArrayList<>();
				EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
				if (selectedSourceLanguage.equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					currentLangMeta.addEnchant(glow, 1, true);
					lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceTranslationSelected"));
				} else if (currTranslator.getInLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					currentLangMeta.addEnchant(glow, 1, true);
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

			/* 45 langs per page, start at 0, 0 */
			pagination.setItems(listOfAvailableLangs);
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
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
							pagination.previous().getPage());
				}));
			} else {
				contents.set(5, 2, ClickableItem.of(previousPageButton, e -> {
					WWCTranslateGUIMainMenu.getTranslateMainMenu(targetPlayerUUID).open(player);
				}));
			}

			/* Bottom Middle Option: Auto-detect Source Language */
			ItemStack skipSourceButton = new ItemStack(Material.BOOKSHELF);
			ItemMeta skipSourceMeta = skipSourceButton.getItemMeta();
			skipSourceMeta.setDisplayName(ChatColor.YELLOW
					+ CommonDefinitions.getMessage("wwctGUIAutoDetectButton"));
			
			/* Add Glow Effect */
			EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
			ArrayList<String> lore = new ArrayList<>();
			if ((currTranslator.getInLangCode().equals("None"))) {
				skipSourceMeta.addEnchant(glow, 1, true);
				lore.add(ChatColor.GREEN + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceTranslationSelected"));
			} else if (selectedSourceLanguage.equalsIgnoreCase("None")) {
				skipSourceMeta.addEnchant(glow, 1, true);
				lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + CommonDefinitions.getMessage("wwctGUISourceOrTargetTranslationAlreadyActive"));
			}
			skipSourceButton.setItemMeta(skipSourceMeta);
			contents.set(5, 4, ClickableItem.of(skipSourceButton, e -> WWCTranslateGUITargetLanguage
					.getTargetLanguageInventory("None", targetPlayerUUID).open(player)));

			/* Bottom Right Option: Next Page */
			ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta nextPageMeta = nextPageButton.getItemMeta();
			nextPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
			nextPageButton.setItemMeta(nextPageMeta);
			if (!pagination.isLast()) {
				contents.set(5, 6, ClickableItem.of(nextPageButton, e -> {
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
							pagination.next().getPage());
				}));
				;
			}
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		WWCInventoryManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}

}
