package com.expl0itz.worldwidechat.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class WWCTranslateGUIMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private Player targetPlayer = null;
	
	public WWCTranslateGUIMainMenu(Player player) {
		targetPlayer = player;
	}
	
	/* Get translation info */
	public static SmartInventory getTranslateMainMenu(Player targetPlayer) { 
		if (targetPlayer == null) {
			return SmartInventory.builder()
				    .id("translateMainMenu")
				    .provider(new WWCTranslateGUIMainMenu(null))
				    .size(6, 9)
				    .manager(WorldwideChat.getInstance().getInventoryManager())
				    .title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenu"))
				    .build();
		}
		return SmartInventory.builder()
		    .id("translateMainMenu")
		    .provider(new WWCTranslateGUIMainMenu(targetPlayer))
		    .size(6, 9)
		    .manager(WorldwideChat.getInstance().getInventoryManager())
		    .title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenuPlayer").replace("%i", targetPlayer.getName()))
		    .build();	
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		/* Default white stained glass borders for inactive */
		ItemStack customDefaultBorders = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
		defaultBorderMeta.setDisplayName(" ");
		customDefaultBorders.setItemMeta(defaultBorderMeta);
		contents.fillBorders(ClickableItem.empty(customDefaultBorders));
		
		/* New translation button */
		ItemStack translationButton = new ItemStack(Material.COMPASS);
		ItemMeta translationMeta = translationButton.getItemMeta();
		translationMeta.setDisplayName(main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUITranslationButton"));
		translationButton.setItemMeta(translationMeta);
		contents.set(3, 4, ClickableItem.of(translationButton, 
				e -> {
					if (targetPlayer == null) {
						WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", null).open(player);
					} else {
						WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", targetPlayer).open(player);
					}
				}));
		
		/* If target player exists, is active player */
		if (targetPlayer != null && main.getActiveTranslator(targetPlayer.getUniqueId().toString()) != null) {
			/* Make compass enchanted */
			EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
			translationMeta.addEnchant(glow, 1, true);
			translationMeta.setDisplayName(main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIExistingTranslationButton"));
			translationMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			translationButton.setItemMeta(translationMeta);
			
			/* Green stained glass borders for active */
			ItemStack customBorders = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
			
			/* Stop Button: Stop translation if active */
			ItemStack stopButton = new ItemStack(Material.BARRIER);
			ItemMeta stopMeta = stopButton.getItemMeta();
			stopMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIStopButton"));
			stopButton.setItemMeta(stopMeta);
			contents.set(3, 6, ClickableItem.of(stopButton,
				e -> {
					main.removeActiveTranslator(main.getActiveTranslator(targetPlayer.getUniqueId().toString()));
					getTranslateMainMenu(targetPlayer).open(player);
				}));
			
			/* Book Translation Button */
			
			/* Sign Translation Button */
			
		} else if (targetPlayer != null) { /* Target player exists */
			
			
		} else if (main.getActiveTranslator(player.getUniqueId().toString()) != null) { /* Current player is active translator */
			/* Make compass enchanted */
			EnchantGlowEffect glow = new EnchantGlowEffect(new NamespacedKey(main, "wwc_glow"));
			translationMeta.addEnchant(glow, 1, true);
			translationMeta.setDisplayName(main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIExistingTranslationButton"));
			translationMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			translationButton.setItemMeta(translationMeta);
			
			/* Green stained glass borders for active */
			ItemStack customBorders = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
			ItemMeta borderMeta = customBorders.getItemMeta();
			borderMeta.setDisplayName(" ");
			customBorders.setItemMeta(borderMeta);
			contents.fillBorders(ClickableItem.empty(customBorders));
			
			/* Stop Button: Stop translation if active */
			ItemStack stopButton = new ItemStack(Material.BARRIER);
			ItemMeta stopMeta = stopButton.getItemMeta();
			stopMeta.setDisplayName(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIStopButton"));
			stopButton.setItemMeta(stopMeta);
			contents.set(3, 6, ClickableItem.of(stopButton,
				e -> {
					main.removeActiveTranslator(main.getActiveTranslator(player.getUniqueId().toString()));
					getTranslateMainMenu(null).open(player);
				}));
			
			/* Book Translation Button */
			
			/* Sign Translation Button */
			
		} else { /* Current player exists */
			
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}

}
