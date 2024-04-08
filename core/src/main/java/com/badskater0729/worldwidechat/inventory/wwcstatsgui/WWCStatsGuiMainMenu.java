package com.badskater0729.worldwidechat.inventory.wwcstatsgui;

import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
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

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class WWCStatsGuiMainMenu implements InventoryProvider {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	private WWCInventoryManager invManager = main.getInventoryManager();
	
	private String targetPlayerUUID = "";
	private String targetPlayerName = "";
	
	public WWCStatsGuiMainMenu(String targetPlayerUUID) {
		this.targetPlayerUUID = targetPlayerUUID;
		this.targetPlayerName = main.getServer()
				.getOfflinePlayer(UUID.fromString(targetPlayerUUID)).getName();
	}

	// TODO: Rewrite for being able to switch between localizations??
	// TODO: Also fix getMsg calls to respect user's localization
	public SmartInventory getStatsMainMenu() {
		return SmartInventory.builder().id("statsMainMenu")
				.provider(new WWCStatsGuiMainMenu(targetPlayerUUID)).size(5, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + refs.getMsg("wwcsTitle", targetPlayerName, null))
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
				ActiveTranslator currTrans = main.getActiveTranslator(targetPlayerUUID);
				isActiveTranslator = XMaterial.GREEN_CONCRETE.parseItem();
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(refs.getMsg("wwcsIsActiveTranslator", ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713", player));

				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransUUID", ChatColor.GOLD + currTrans.getUUID(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransRateLimit", ChatColor.GOLD + "" + currTrans.getRateLimit(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransInLang", ChatColor.GOLD + currTrans.getInLangCode(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransOutLang", ChatColor.GOLD + currTrans.getOutLangCode(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransOutgoing", ChatColor.GOLD + "" + currTrans.getTranslatingChatOutgoing(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransIncoming", ChatColor.GOLD + "" + currTrans.getTranslatingChatIncoming(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransBook", ChatColor.GOLD + "" + currTrans.getTranslatingBook(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSign", ChatColor.GOLD + "" + currTrans.getTranslatingSign(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransItem", ChatColor.GOLD + "" + currTrans.getTranslatingItem(), player));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransEntity", ChatColor.GOLD + "" + currTrans.getTranslatingEntity(), player));

				// If debug, append extra vars
				if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransColorWarning", ChatColor.GOLD + "" + currTrans.getCCWarning(), player));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSignWarning", ChatColor.GOLD + "" + currTrans.getSignWarning(), player));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSaved", ChatColor.GOLD + "" + currTrans.getHasBeenSaved(), player));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransPrevRate", ChatColor.GOLD + currTrans.getRateLimitPreviousTime(), player));
				}
				isActiveTranslatorMeta.setLore(lore);

				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			} else {
				isActiveTranslator = XMaterial.RED_CONCRETE.parseItem();
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(refs.getMsg("wwcsIsActiveTranslator", ChatColor.BOLD + "" + ChatColor.RED + "\u2717", player));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			}
			contents.set(2, 1, ClickableItem.empty(isActiveTranslator));
			
			/* Attempted translations button */
			ItemStack attemptedTranslations = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta attemptedTranslationsMeta = attemptedTranslations.getItemMeta();
			attemptedTranslationsMeta.setDisplayName(refs.getMsg("wwcsAttemptedTranslations", ChatColor.AQUA + "" + currRecord.getAttemptedTranslations(), player));
			attemptedTranslations.setItemMeta(attemptedTranslationsMeta);
			contents.set(2, 3, ClickableItem.empty(attemptedTranslations));
			
			/* Successful translations button */
			ItemStack successfulTranslations = XMaterial.WRITTEN_BOOK.parseItem();
			ItemMeta successfulTranslationsMeta = successfulTranslations.getItemMeta();
			successfulTranslationsMeta.setDisplayName(refs.getMsg("wwcsSuccessfulTranslations", ChatColor.AQUA + "" + currRecord.getSuccessfulTranslations(), player));
			successfulTranslations.setItemMeta(successfulTranslationsMeta);
			contents.set(2, 5, ClickableItem.empty(successfulTranslations));
			
			/* Last translation time button */
			ItemStack lastTranslationTime = XMaterial.CLOCK.parseItem();
			ItemMeta lastTranslationTimeMeta = lastTranslationTime.getItemMeta();
			lastTranslationTimeMeta.setDisplayName(refs.getMsg("wwcsLastTranslationTime", ChatColor.AQUA + "" + currRecord.getLastTranslationTime(), player));
			lastTranslationTime.setItemMeta(lastTranslationTimeMeta);
			contents.set(2, 7, ClickableItem.empty(lastTranslationTime));

	    } catch (Exception e) {
		    invManager.inventoryError(player, e);
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
