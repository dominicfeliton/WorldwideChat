package com.expl0itz.worldwidechat.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;

public class WWCTranslateGUISourceLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private String selectedSourceLanguage = "";
	private String targetPlayerUUID = null;
	
	public WWCTranslateGUISourceLanguage(String s, String targetPlayerUUID) {
		selectedSourceLanguage = s;
		this.targetPlayerUUID = targetPlayerUUID;
	}
	
	public static SmartInventory getSourceLanguageInventory(String s, String targetPlayerUUID) {
		return SmartInventory.builder()
				.id("translateSourceLanguage")
				.provider(new WWCTranslateGUISourceLanguage(s, targetPlayerUUID))
				.size(6, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUINewTranslationSource"))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* Pagination: Lets you generate pages rather than set defined ones */
		Pagination pagination = contents.pagination();
		ClickableItem[] listOfAvailableLangs;
		
		/* Add each supported language from each respective translator */
		if (main.getTranslatorName().equals("Watson")) {
			listOfAvailableLangs = new ClickableItem[main.getSupportedWatsonLanguages().size()];
			for (int i = 0; i < main.getSupportedWatsonLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.BOOK);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				/* Add Glow Effect */
				if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals(main.getSupportedWatsonLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedWatsonLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedWatsonLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedWatsonLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				currentLangMeta.setDisplayName(main.getSupportedWatsonLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				lore.add(main.getSupportedWatsonLanguages().get(i).getNativeLangName());
				lore.add(main.getSupportedWatsonLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String thisLangCode = main.getSupportedWatsonLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							WWCTranslateGUITargetLanguage.getTargetLanguageInventory(thisLangCode, targetPlayerUUID).open(player);
						});
			}
		} else if (main.getTranslatorName().equals("Google Translate")) {
			listOfAvailableLangs = new ClickableItem[main.getSupportedGoogleTranslateLanguages().size()];
			for (int i = 0; i < main.getSupportedGoogleTranslateLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.BOOK);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				currentLangMeta.setDisplayName(main.getSupportedGoogleTranslateLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				//lore.add(main.getSupportedGoogleTranslateLanguages().get(i).getNativeLangName());
				lore.add(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String thisLangCode = main.getSupportedGoogleTranslateLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							WWCTranslateGUITargetLanguage.getTargetLanguageInventory(thisLangCode, targetPlayerUUID).open(player);
						});
			}
		} else if (main.getTranslatorName().equals("Amazon Translate")) {
			listOfAvailableLangs = new ClickableItem[main.getSupportedAmazonTranslateLanguages().size()];
			for (int i = 0; i < main.getSupportedAmazonTranslateLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.BOOK);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				currentLangMeta.setDisplayName(main.getSupportedAmazonTranslateLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				//lore.add(main.getSupportedAmazonTranslateLanguages().get(i).getNativeLangName());
				lore.add(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String thisLangCode = main.getSupportedAmazonTranslateLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							WWCTranslateGUITargetLanguage.getTargetLanguageInventory(thisLangCode, targetPlayerUUID).open(player);
						});
			}
		} else {
			listOfAvailableLangs = new ClickableItem[1];
			listOfAvailableLangs[0] = ClickableItem.empty(new ItemStack(Material.STONE));
		}
		
		/* 45 langs per page, start at 0, 0 */
		pagination.setItems(listOfAvailableLangs);
		pagination.setItemsPerPage(45);
		pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
		
		/* Deep Bottom Left Option: Back to main translation menu */
		ItemStack stopButton = new ItemStack(Material.BARRIER);
		ItemMeta stopMeta = stopButton.getItemMeta();
		stopMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBackToMainMenuButton"));
		stopButton.setItemMeta(stopMeta);
		contents.set(5, 0, ClickableItem.of(stopButton,
			e -> {
				if (targetPlayerUUID == null) {
					WWCTranslateGUIMainMenu.getTranslateMainMenu(null).open(player);
				} else {
					WWCTranslateGUIMainMenu.getTranslateMainMenu(targetPlayerUUID).open(player);
				}
			}));
		
		/* Bottom Left Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(5, 2, ClickableItem.of(previousPageButton, 
				e -> {
					if (targetPlayerUUID == null) {
						getSourceLanguageInventory(selectedSourceLanguage, null).open(player, pagination.previous().getPage());
					} else {
						getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player, pagination.previous().getPage());
					}
				}));
		
	    /* Bottom Middle Option: Auto-detect Source Language */
	    ItemStack skipSourceButton = new ItemStack(Material.BOOKSHELF);
	    ItemMeta skipSourceMeta = skipSourceButton.getItemMeta();
	    skipSourceMeta.setDisplayName(ChatColor.YELLOW + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIAutoDetectButton"));
        if (main.getActiveTranslator(player.getUniqueId().toString()) != null && main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals("None")
        		|| selectedSourceLanguage.equals("None")) {
        	EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
			skipSourceMeta.addEnchant(glow, 1, true);
		}
	    skipSourceButton.setItemMeta(skipSourceMeta);
	    contents.set(5, 4, ClickableItem.of(skipSourceButton, 
	    		e -> WWCTranslateGUITargetLanguage.getTargetLanguageInventory("None", targetPlayerUUID).open(player)));
	    
	    /* Bottom Right Option: Next Page */
		ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta nextPageMeta = nextPageButton.getItemMeta();
		nextPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUINextPageButton"));
	    nextPageButton.setItemMeta(nextPageMeta);
	    contents.set(5, 6, ClickableItem.of(nextPageButton,
	    		e -> {
					if (targetPlayerUUID == null) {
						getSourceLanguageInventory(selectedSourceLanguage, null).open(player, pagination.next().getPage());
					} else {
						getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player, pagination.next().getPage());
					}
				}));;
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}

}
