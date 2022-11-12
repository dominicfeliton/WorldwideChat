package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.commands.WWCGlobal;
import com.badskater0729.worldwidechat.commands.WWCTranslate;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonDefinitions;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class WWCTranslateGuiTargetLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;

	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = "";
	
	public WWCTranslateGuiTargetLanguage(String source, String targetPlayerUUID) {
		selectedSourceLanguage = source;
		this.targetPlayerUUID = targetPlayerUUID;
	}

	public static SmartInventory getTargetLanguageInventory(String source, String targetPlayerUUID) {
		return SmartInventory.builder().id("translateTargetLanguage")
				.provider(new WWCTranslateGuiTargetLanguage(source, targetPlayerUUID)).size(6, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwctGUINewTranslationTarget"))
				.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Default white stained glass borders for inactive, yellow if player has existing translation session */
			WWCInventoryManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
			if (!main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals("")) {
				WWCInventoryManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
			}
			
			/* Init current active translator */
			ActiveTranslator currTranslator = main.getActiveTranslator(targetPlayerUUID);
			
			/* Pagination: Lets you generate pages rather than set defined ones */
			Pagination pagination = contents.pagination();
			ClickableItem[] listOfAvailableLangs = new ClickableItem[main.getSupportedTranslatorLanguages().size()];

			/* Add each supported language from each respective translator */
			for (int i = 0; i < main.getSupportedTranslatorLanguages().size(); i++) {
				ItemStack currentLang = XMaterial.ARROW.parseItem();
				if (XMaterial.TARGET.parseItem() != null) {
					currentLang = XMaterial.TARGET.parseItem();
				}
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (currTranslator.getOutLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())) {
					WWCInventoryManager.addGlowEffect(currentLangMeta);
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

			/* 28 langs per page, start at 1, 1 */
			pagination.setItems(listOfAvailableLangs);
			pagination.setItemsPerPage(28);
			pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

			/* Bottom Left Option: Previous Page */
			if (!pagination.isFirst()) {
				WWCInventoryManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {getTargetLanguageInventory(selectedSourceLanguage, targetPlayerUUID)});
			} else {
				WWCInventoryManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {WWCTranslateGuiSourceLanguage.getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID)});
			}

			/* Bottom Right Option: Next Page */
			if (!pagination.isLast()) {
				WWCInventoryManager.setCommonButton(5, 6, player, contents, "Next", new Object[] {getTargetLanguageInventory(selectedSourceLanguage, targetPlayerUUID)});
			}
			
			/* Last Option: Page Number */
			WWCInventoryManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
		} catch (Exception e) {
			WWCInventoryManager.inventoryError(player, e);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		WWCInventoryManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
	}

}
