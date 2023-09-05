package com.badskater0729.worldwidechat.inventory.wwcstatsgui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import com.cryptomorin.xseries.XMaterial;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;

public class WWCStatsGuiMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	
	private String targetPlayerUUID = "";
	
	public WWCStatsGuiMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
	}
	
	public static SmartInventory getStatsMainMenu(String targetPlayerUUID, String targetPlayerName) {
		return SmartInventory.builder().id("statsMainMenu")
				.provider(new WWCStatsGuiMainMenu(targetPlayerUUID)).size(5, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + getMsg("wwcsTitle", targetPlayerName))
				.build();
	}
	
	@Override
	public void init(Player player, InventoryContents contents) {
		try {
			/* Init current active record */
			PlayerRecord currRecord = main.getPlayerRecord(targetPlayerUUID, false);
			
			/* Default orange stained glass borders */
			ItemStack customDefaultBorders = XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem();
			ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
			defaultBorderMeta.setDisplayName(" ");
			customDefaultBorders.setItemMeta(defaultBorderMeta);
			contents.fillBorders(ClickableItem.empty(customDefaultBorders));
			
			/* Is active translator button */
			ItemStack isActiveTranslator;
			if (main.isActiveTranslator(targetPlayerUUID)) {
				isActiveTranslator = XMaterial.GREEN_CONCRETE.parseItem();
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(getMsg("wwcsIsActiveTranslator", ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713"));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			} else {
				isActiveTranslator = XMaterial.RED_CONCRETE.parseItem();
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(getMsg("wwcsIsActiveTranslator", ChatColor.BOLD + "" + ChatColor.RED + "\u2717"));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			}
			contents.set(2, 1, ClickableItem.empty(isActiveTranslator));
			
			/* Attempted translations button */
			ItemStack attemptedTranslations = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta attemptedTranslationsMeta = attemptedTranslations.getItemMeta();
			attemptedTranslationsMeta.setDisplayName(getMsg("wwcsAttemptedTranslations", ChatColor.AQUA + "" + currRecord.getAttemptedTranslations()));
			attemptedTranslations.setItemMeta(attemptedTranslationsMeta);
			contents.set(2, 3, ClickableItem.empty(attemptedTranslations));
			
			/* Successful translations button */
			ItemStack successfulTranslations = XMaterial.WRITTEN_BOOK.parseItem();
			ItemMeta successfulTranslationsMeta = successfulTranslations.getItemMeta();
			successfulTranslationsMeta.setDisplayName(getMsg("wwcsSuccessfulTranslations", ChatColor.AQUA + "" + currRecord.getSuccessfulTranslations()));
			successfulTranslations.setItemMeta(successfulTranslationsMeta);
			contents.set(2, 5, ClickableItem.empty(successfulTranslations));
			
			/* Last translation time button */
			ItemStack lastTranslationTime = XMaterial.CLOCK.parseItem();
			ItemMeta lastTranslationTimeMeta = lastTranslationTime.getItemMeta();
			lastTranslationTimeMeta.setDisplayName(getMsg("wwcsLastTranslationTime", ChatColor.AQUA + "" + currRecord.getLastTranslationTime()));
			lastTranslationTime.setItemMeta(lastTranslationTimeMeta);
			contents.set(2, 7, ClickableItem.empty(lastTranslationTime));

			/* Current translator stats button */
			ItemStack currentTranslatorStats = XMaterial.PAPER.parseItem();
			ItemMeta currentTranslatorStatsMeta = currentTranslatorStats.getItemMeta();
			//currentTranslatorStatsMeta.setDisplayName(getMsg("wwcsCurrentTranslatorStats", ));
			// TODO: Open submenu for stats, if active translator

	    } catch (Exception e) {
		    WWCInventoryManager.inventoryError(player, e);
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
