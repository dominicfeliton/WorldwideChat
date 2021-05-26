package com.expl0itz.worldwidechat.commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
		ClickableItem[] listOfAvailableLangs = new ClickableItem[main.getSupportedTranslatorLanguages().size()];
		
		/* Add each supported language from each respective translator */
		if (!main.getTranslatorName().equals("Invalid")) {
			for (int i = 0; i < main.getSupportedTranslatorLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.BOOK);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				/* Add Glow Effect */
				if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedTranslatorLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (targetPlayerUUID == null && main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedTranslatorLanguages().get(i).getLangCode())
							 ) || (selectedSourceLanguage.equals(main.getSupportedTranslatorLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				currentLangMeta.setDisplayName(main.getSupportedTranslatorLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				if (!main.getSupportedTranslatorLanguages().get(i).getNativeLangName().equals("")) {
					lore.add(main.getSupportedTranslatorLanguages().get(i).getNativeLangName());
				}
				lore.add(main.getSupportedTranslatorLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String thisLangCode = main.getSupportedTranslatorLanguages().get(i).getLangCode();
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
				WWCTranslateGUIMainMenu.getTranslateMainMenu(targetPlayerUUID).open(player);
			}));
		
		/* Bottom Left Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(5, 2, ClickableItem.of(previousPageButton, 
				e -> {
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player, pagination.previous().getPage());
				}));
		
	    /* Bottom Middle Option: Auto-detect Source Language */
	    ItemStack skipSourceButton = new ItemStack(Material.BOOKSHELF);
	    ItemMeta skipSourceMeta = skipSourceButton.getItemMeta();
	    skipSourceMeta.setDisplayName(ChatColor.YELLOW + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIAutoDetectButton"));
        if ((targetPlayerUUID != null && Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) != null && main.getActiveTranslator(Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)).getUniqueId().toString()) != null && main.getActiveTranslator(Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)).getUniqueId().toString()).getInLangCode().equals("None"))
        	|| (targetPlayerUUID == null && main.getActiveTranslator(player.getUniqueId().toString()) != null && main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals("None"))
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
					getSourceLanguageInventory(selectedSourceLanguage, targetPlayerUUID).open(player, pagination.next().getPage());
				}));;
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		if (targetPlayerUUID != null && !targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
	    	if (Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)) == null) {
	    		//Target player no longer online
				player.closeInventory();
				final TextComponent targetPlayerDC = Component.text()
	                    .append(main.getPluginPrefix().asComponent())
	                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUITargetPlayerNull")).color(NamedTextColor.RED).decorate(TextDecoration.ITALIC))
	                    .build();
	                main.adventure().sender(player).sendMessage(targetPlayerDC);
	    	}
	    }
	}

}
