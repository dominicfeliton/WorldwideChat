package com.expl0itz.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCGlobal;
import com.expl0itz.worldwidechat.commands.WWCTranslate;
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

public class WWCTranslateGUITargetLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();

	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = "";
	
	public WWCTranslateGUITargetLanguage(String source, String targetPlayerUUID) {
		selectedSourceLanguage = source;
		this.targetPlayerUUID = targetPlayerUUID;
	}

	public static SmartInventory getTargetLanguageInventory(String source, String targetPlayerUUID) {
		return SmartInventory.builder().id("translateTargetLanguage")
				.provider(new WWCTranslateGUITargetLanguage(source, targetPlayerUUID)).size(6, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUINewTranslationTarget"))
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
				ItemStack currentLang = new ItemStack(Material.TARGET);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (currTranslator.getOutLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
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
				String outLang = main.getSupportedTranslatorLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, e -> {
					/* Send to /wwct */
					WWCTranslate translateCommand;
					if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						translateCommand = new WWCTranslate((CommandSender)player, null, null, new String[] {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(), selectedSourceLanguage, outLang});
					} else {
						translateCommand = new WWCGlobal((CommandSender)player, null, null, new String[] {selectedSourceLanguage, outLang});
					}
					player.closeInventory();
					translateCommand.processCommand();
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
				contents.set(5, 2,
						ClickableItem.of(previousPageButton,
								e -> getTargetLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
										pagination.previous().getPage())));
			} else {
				contents.set(5, 2,
						ClickableItem.of(previousPageButton,
								e -> WWCTranslateGUISourceLanguage.getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player)));
			}

			/* Bottom Right Option: Next Page */
			ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
			ItemMeta nextPageMeta = nextPageButton.getItemMeta();
			nextPageMeta.setDisplayName(ChatColor.GREEN
					+ CommonDefinitions.getMessage("wwcConfigGUINextPageButton"));
			nextPageButton.setItemMeta(nextPageMeta);
			if (!pagination.isLast()) {
				contents.set(5, 6,
						ClickableItem.of(nextPageButton,
								e -> getTargetLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player,
										pagination.next().getPage())));
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
