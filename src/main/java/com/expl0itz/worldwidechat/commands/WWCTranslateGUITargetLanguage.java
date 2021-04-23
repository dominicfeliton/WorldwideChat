package com.expl0itz.worldwidechat.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
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

public class WWCTranslateGUITargetLanguage implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private String selectedSourceLanguage = "";
	
	private Player targetPlayer = null;
	
	public WWCTranslateGUITargetLanguage(String source, Player p) {
		selectedSourceLanguage = source;
		targetPlayer = p;
	}
	
	public static SmartInventory getTargetLanguageInventory(String source, Player player) {
		return SmartInventory.builder()
				.id("translateTargetLanguage")
				.provider(new WWCTranslateGUITargetLanguage(source, player))
				.size(6, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUINewTranslationTarget"))
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
				ItemStack currentLang = new ItemStack(Material.TARGET);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				/* Add Glow Effect */
				if (targetPlayer != null && main.getActiveTranslator(targetPlayer.getUniqueId().toString()) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayer.getUniqueId().toString()).getOutLangCode().equals(main.getSupportedWatsonLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getOutLangCode().equals(main.getSupportedWatsonLanguages().get(i).getLangCode()))) {
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
				String outLang = main.getSupportedWatsonLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							/* Send to /wwct */
							String[] args;
							if (targetPlayer == null && selectedSourceLanguage.equals("None")) { //If no target, no input lang
								args = new String[]{outLang};
							} else if (targetPlayer == null) { //No target, yes input lang
								args = new String[]{selectedSourceLanguage, outLang};
							} else if (targetPlayer != null && selectedSourceLanguage.equals("None")) { //Yes target, no input lang
								args = new String[] {targetPlayer.getName(), outLang};
							} else { //Yes target, yes input lang
								args = new String[]{targetPlayer.getName(), selectedSourceLanguage, outLang};
							}
							
							WWCTranslate newTranslation = new WWCTranslate((CommandSender)player, null, null, args);
							player.closeInventory();
							newTranslation.processCommand(false);
						});
			}
		} else if (main.getTranslatorName().equals("Google Translate")) {
			listOfAvailableLangs = new ClickableItem[main.getSupportedGoogleTranslateLanguages().size()];
			for (int i = 0; i < main.getSupportedGoogleTranslateLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.TARGET);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				currentLangMeta.setDisplayName(main.getSupportedGoogleTranslateLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (targetPlayer != null && main.getActiveTranslator(targetPlayer.getUniqueId().toString()) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayer.getUniqueId().toString()).getInLangCode().equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				//lore.add(main.getSupportedGoogleTranslateLanguages().get(i).getNativeLangName());
				lore.add(main.getSupportedGoogleTranslateLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String outLang = main.getSupportedGoogleTranslateLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							/* Send to /wwct */
							String[] args;
							if (targetPlayer == null && selectedSourceLanguage.equals("None")) { //If no target, no input lang
								args = new String[]{outLang};
							} else if (targetPlayer == null) { //No target, yes input lang
								args = new String[]{selectedSourceLanguage, outLang};
							} else if (targetPlayer != null && selectedSourceLanguage.equals("None")) { //Yes target, no input lang
								args = new String[] {targetPlayer.getName(), outLang};
							} else { //Yes target, yes input lang
								args = new String[]{targetPlayer.getName(), selectedSourceLanguage, outLang};
							}
							
							WWCTranslate newTranslation = new WWCTranslate((CommandSender)player, null, null, args);
							player.closeInventory();
							newTranslation.processCommand(false);
						});
			}
		} else if (main.getTranslatorName().equals("Amazon Translate")) {
			listOfAvailableLangs = new ClickableItem[main.getSupportedAmazonTranslateLanguages().size()];
			for (int i = 0; i < main.getSupportedAmazonTranslateLanguages().size(); i++) {
				ItemStack currentLang = new ItemStack(Material.TARGET);
				ItemMeta currentLangMeta = currentLang.getItemMeta();
				currentLangMeta.setDisplayName(main.getSupportedAmazonTranslateLanguages().get(i).getLangName());
				ArrayList<String> lore = new ArrayList<>();
				/* Add Glow Effect */
				if (targetPlayer != null && main.getActiveTranslator(targetPlayer.getUniqueId().toString()) != null) { //If target player is active translator
					if ((main.getActiveTranslator(targetPlayer.getUniqueId().toString()).getInLangCode().equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { //If this player is an active translator
					if ((main.getActiveTranslator(player.getUniqueId().toString()).getInLangCode().equals(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode()))) {
						EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
						currentLangMeta.addEnchant(glow, 1, true);
					}
				}
				//lore.add(main.getSupportedAmazonTranslateLanguages().get(i).getNativeLangName());
				lore.add(main.getSupportedAmazonTranslateLanguages().get(i).getLangCode());
				currentLangMeta.setLore(lore);
				currentLang.setItemMeta(currentLangMeta);
				String outLang = main.getSupportedAmazonTranslateLanguages().get(i).getLangCode();
				listOfAvailableLangs[i] = ClickableItem.of(currentLang, 
						e -> {
							/* Send to /wwct */
							String[] args;
							if (targetPlayer == null && selectedSourceLanguage.equals("None")) { //If no target, no input lang
								args = new String[]{outLang};
							} else if (targetPlayer == null) { //No target, yes input lang
								args = new String[]{selectedSourceLanguage, outLang};
							} else if (targetPlayer != null && selectedSourceLanguage.equals("None")) { //Yes target, no input lang
								args = new String[] {targetPlayer.getName(), outLang};
							} else { //Yes target, yes input lang
								args = new String[]{targetPlayer.getName(), selectedSourceLanguage, outLang};
							}
							
							WWCTranslate newTranslation = new WWCTranslate((CommandSender)player, null, null, args);
							player.closeInventory();
							newTranslation.processCommand(false);
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
		
		/* Deep Bottom Left Option: Back to source translation menu */
		ItemStack stopButton = new ItemStack(Material.BARRIER);
		ItemMeta stopMeta = stopButton.getItemMeta();
		stopMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBackToSourceLanguageButton"));
		stopButton.setItemMeta(stopMeta);
		contents.set(5, 0, ClickableItem.of(stopButton,
			e -> {
				WWCTranslateGUISourceLanguage.getSourceLanguageInventory(selectedSourceLanguage, targetPlayer).open(player);
			}));
		
		/* Bottom Left Option: Previous Page */
		ItemStack previousPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta previousPageMeta = previousPageButton.getItemMeta();
		previousPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUIPreviousPageButton"));
	    previousPageButton.setItemMeta(previousPageMeta);
	    contents.set(5, 2, ClickableItem.of(previousPageButton, 
				e -> getTargetLanguageInventory(selectedSourceLanguage, targetPlayer).open(player, pagination.previous().getPage())));
	    
	    /* Bottom Right Option: Next Page */
		ItemStack nextPageButton = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
		ItemMeta nextPageMeta = nextPageButton.getItemMeta();
		nextPageMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigGUINextPageButton"));
	    nextPageButton.setItemMeta(nextPageMeta);
	    contents.set(5, 6, ClickableItem.of(nextPageButton,
                e -> getTargetLanguageInventory(selectedSourceLanguage, targetPlayer).open(player, pagination.next().getPage())));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}

}
