package com.expl0itz.worldwidechat.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.EnchantGlowEffect;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class WWCTranslateGUIMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private String targetPlayerUUID = null;
	
	public WWCTranslateGUIMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}
	
	/* Get translation info */
	public static SmartInventory getTranslateMainMenu(String targetPlayerUUID) { 
		if (targetPlayerUUID == null) {
			return SmartInventory.builder()
				    .id("translateMainMenu")
				    .provider(new WWCTranslateGUIMainMenu(null))
				    .size(6, 9)
				    .manager(WorldwideChat.getInstance().getInventoryManager())
				    .title(ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenu"))
				    .build();
		}
		String playerTitle = "";
		if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
			playerTitle = ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenuGlobal");
		} else {
			playerTitle = ChatColor.BLUE + WorldwideChat.getInstance().getConfigManager().getMessagesConfig().getString("Messages.wwctGUIMainMenuPlayer").replace("%i", WorldwideChat.getInstance().getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName());
		}
		return SmartInventory.builder()
		    .id("translateMainMenu")
		    .provider(new WWCTranslateGUIMainMenu(targetPlayerUUID))
		    .size(6, 9)
		    .manager(WorldwideChat.getInstance().getInventoryManager())
		    .title(playerTitle)
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
					if (targetPlayerUUID == null) {
						WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", null).open(player);
					} else {
						WWCTranslateGUISourceLanguage.getSourceLanguageInventory("", targetPlayerUUID).open(player);
					}
				}));
		
		/* If target player exists, is active player */
		if (targetPlayerUUID != null && main.getActiveTranslator(targetPlayerUUID) != null) {
			ActiveTranslator targetTranslator = main.getActiveTranslator(targetPlayerUUID);
			
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
			contents.set(2, 4, ClickableItem.of(stopButton,
				e -> {
					String[] args;
					if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						args = new String[]{main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(), "stop"};
					} else {
						args = new String[]{"stop"};
					}
					WWCTranslate translate = new WWCTranslate((CommandSender)player, null, null, args);
					if (targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
						translate.processCommand(true);
				    } else {
				    	translate.processCommand(false);
				    }
					getTranslateMainMenu(targetPlayerUUID).open(player);
				}));
			
			/* Book Translation Button */
			if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
				ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
				ItemMeta bookMeta = bookButton.getItemMeta();
				bookMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBookButton"));
				if (targetTranslator.getTranslatingBook()) {				
					bookMeta.addEnchant(glow, 1, true);
				}
				bookButton.setItemMeta(bookMeta);
				contents.set(3, 1, ClickableItem.of(bookButton, 
						e -> {
							String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
							WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender)player, null, null, args);
							translateBook.processCommand();
							getTranslateMainMenu(targetPlayerUUID).open(player);
						}));
			}
			
			/* Sign Translation Button */
			if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
				ItemStack signButton = new ItemStack(Material.OAK_SIGN);
				ItemMeta signMeta = signButton.getItemMeta();
				signMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUISignButton"));
				if (targetTranslator.getTranslatingSign()) {				
					signMeta.addEnchant(glow, 1, true);
				}
				signButton.setItemMeta(signMeta);
				contents.set(3, 7, ClickableItem.of(signButton, 
						e -> {
							String[] args = {main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName()};
							WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender)player, null, null, args);
							translateSign.processCommand();
							getTranslateMainMenu(targetPlayerUUID).open(player);
						}));
			}
		} else if (targetPlayerUUID == null && main.getActiveTranslator(player.getUniqueId().toString()) != null) { /* Current player is active translator */
			ActiveTranslator currTranslator = main.getActiveTranslator(player.getUniqueId().toString());
			
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
			contents.set(2, 4, ClickableItem.of(stopButton,
				e -> {
					String[] args = {"stop"};
					WWCTranslate translate = new WWCTranslate((CommandSender)player, null, null, args);
					translate.processCommand(false);
					getTranslateMainMenu(targetPlayerUUID).open(player);
				}));
			
			/* Book Translation Button */
			ItemStack bookButton = new ItemStack(Material.WRITABLE_BOOK);
			ItemMeta bookMeta = bookButton.getItemMeta();
			bookMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIBookButton"));
			if (currTranslator.getTranslatingBook()) {				
				bookMeta.addEnchant(glow, 1, true);
			}
			bookButton.setItemMeta(bookMeta);
			contents.set(3, 1, ClickableItem.of(bookButton, 
					e -> {
						String[] args = {};
						WWCTranslateBook translateBook = new WWCTranslateBook((CommandSender)player, null, null, args);
						translateBook.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
			
			/* Sign Translation Button */
			ItemStack signButton = new ItemStack(Material.OAK_SIGN);
			ItemMeta signMeta = signButton.getItemMeta();
			signMeta.setDisplayName(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUISignButton"));
			if (currTranslator.getTranslatingSign()) {				
				signMeta.addEnchant(glow, 1, true);
			}
			signButton.setItemMeta(signMeta);
			contents.set(3, 7, ClickableItem.of(signButton, 
					e -> {
						String[] args = {};
						WWCTranslateSign translateSign = new WWCTranslateSign((CommandSender)player, null, null, args);
						translateSign.processCommand();
						getTranslateMainMenu(targetPlayerUUID).open(player);
					}));
		} else { /* Current player exists */
			return;
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		
	}

}