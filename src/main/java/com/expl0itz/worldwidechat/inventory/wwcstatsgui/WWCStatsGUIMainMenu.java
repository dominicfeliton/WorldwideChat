package com.expl0itz.worldwidechat.inventory.wwcstatsgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.WWCInventoryManager;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.PlayerRecord;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class WWCStatsGUIMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private String targetPlayerUUID = "";
	
	public WWCStatsGUIMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}
	
	public static SmartInventory getStatsMainMenu(String targetPlayerUUID) {
		return SmartInventory.builder().id("statsMainMenu")
				.provider(new WWCStatsGUIMainMenu(targetPlayerUUID)).size(5, 9)
				.manager(WorldwideChat.getInstance().getInventoryManager())
				.title(ChatColor.BLUE + CommonDefinitions.getMessage("wwcsTitle", new String[] {Bukkit.getPlayer(UUID.fromString(targetPlayerUUID)).getName()}))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Init current active record */
			PlayerRecord currRecord = main.getPlayerRecord(targetPlayerUUID, false);
			
			/* Default orange stained glass borders */
			ItemStack customDefaultBorders = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));
			
			/* Is active translator button */
			ItemStack isActiveTranslator;
			if (!main.getActiveTranslator(targetPlayerUUID).getUUID().equals("")) {
				isActiveTranslator = new ItemStack(Material.GREEN_CONCRETE);
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(CommonDefinitions.getMessage("wwcsIsActiveTranslator", new String[] {ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713"}));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			} else {
				isActiveTranslator = new ItemStack(Material.RED_CONCRETE);
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(CommonDefinitions.getMessage("wwcsIsActiveTranslator", new String[] {ChatColor.BOLD + "" + ChatColor.RED + "\u2717"}));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			}
			contents.set(2, 1, ClickableItem.empty(isActiveTranslator));
			
			/* Attempted translations button */
			ItemStack attemptedTranslations = new ItemStack(Material.WRITABLE_BOOK);
			ItemMeta attemptedTranslationsMeta = attemptedTranslations.getItemMeta();
			attemptedTranslationsMeta.setDisplayName(CommonDefinitions.getMessage("wwcsAttemptedTranslations", new String[] {ChatColor.AQUA + "" + currRecord.getAttemptedTranslations()}));
			attemptedTranslations.setItemMeta(attemptedTranslationsMeta);
			contents.set(2, 3, ClickableItem.empty(attemptedTranslations));
			
			/* Successful translations button */
			ItemStack successfulTranslations = new ItemStack(Material.WRITTEN_BOOK);
			ItemMeta successfulTranslationsMeta = successfulTranslations.getItemMeta();
			successfulTranslationsMeta.setDisplayName(CommonDefinitions.getMessage("wwcsSuccessfulTranslations", new String[] {ChatColor.AQUA + "" + currRecord.getSuccessfulTranslations()}));
			successfulTranslations.setItemMeta(successfulTranslationsMeta);
			contents.set(2, 5, ClickableItem.empty(successfulTranslations));
			
			/* Last translation time button */
			ItemStack lastTranslationTime = new ItemStack(Material.CLOCK);
			ItemMeta lastTranslationTimeMeta = lastTranslationTime.getItemMeta();
			lastTranslationTimeMeta.setDisplayName(CommonDefinitions.getMessage("wwcsLastTranslationTime", new String[] {ChatColor.AQUA + "" + currRecord.getLastTranslationTime()}));
			lastTranslationTime.setItemMeta(lastTranslationTimeMeta);
			contents.set(2, 7, ClickableItem.empty(lastTranslationTime));
	    } catch (Exception e) {
		    WWCInventoryManager.inventoryError(player, e);
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
