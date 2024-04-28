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

	private Player inPlayer;
	
	public WWCStatsGuiMainMenu(String targetPlayerUUID, Player inPlayer) {
		this.targetPlayerUUID = targetPlayerUUID;
		this.targetPlayerName = main.getServer()
				.getOfflinePlayer(UUID.fromString(targetPlayerUUID)).getName();
		this.inPlayer = inPlayer;
	}

	public SmartInventory getStatsMainMenu() {
		return SmartInventory.builder().id("statsMainMenu")
				.provider(this).size(5, 9)
				.manager(WorldwideChat.instance.getInventoryManager())
				.title(ChatColor.BLUE + refs.getMsg("wwcsTitle", targetPlayerName, inPlayer))
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
				isActiveTranslatorMeta.setDisplayName(refs.getMsg("wwcsIsActiveTranslator", refs.checkOrX(true), inPlayer));

				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransUUID", ChatColor.GOLD + currTrans.getUUID(), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransRateLimit", ChatColor.GOLD + "" + currTrans.getRateLimit(), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransInLang", ChatColor.GOLD + currTrans.getInLangCode(), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransOutLang", ChatColor.GOLD + currTrans.getOutLangCode(), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransOutgoing", refs.checkOrX(currTrans.getTranslatingChatOutgoing()), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransIncoming", refs.checkOrX(currTrans.getTranslatingChatIncoming()), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransBook", refs.checkOrX(currTrans.getTranslatingBook()), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSign", refs.checkOrX(currTrans.getTranslatingSign()), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransItem", refs.checkOrX(currTrans.getTranslatingItem()), inPlayer));
				lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransEntity", refs.checkOrX(currTrans.getTranslatingEntity()), inPlayer));

				// If debug, append extra vars
				if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransColorWarning", refs.checkOrX(currTrans.getCCWarning()), inPlayer));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSignWarning", refs.checkOrX(currTrans.getSignWarning()), inPlayer));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransSaved", refs.checkOrX(currTrans.getHasBeenSaved()), inPlayer));
					lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getMsg("wwcsActiveTransPrevRate", ChatColor.GOLD + currTrans.getRateLimitPreviousTime(), inPlayer));
				}
				isActiveTranslatorMeta.setLore(lore);

				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			} else {
				isActiveTranslator = XMaterial.RED_CONCRETE.parseItem();
				ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
				isActiveTranslatorMeta.setDisplayName(refs.getMsg("wwcsIsActiveTranslator", refs.checkOrX(false), inPlayer));
				isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
			}
			contents.set(2, 1, ClickableItem.empty(isActiveTranslator));
			
			/* Attempted translations button */
			ItemStack attemptedTranslations = XMaterial.WRITABLE_BOOK.parseItem();
			ItemMeta attemptedTranslationsMeta = attemptedTranslations.getItemMeta();
			attemptedTranslationsMeta.setDisplayName(refs.getMsg("wwcsAttemptedTranslations", ChatColor.AQUA + "" + currRecord.getAttemptedTranslations(), inPlayer));
			attemptedTranslations.setItemMeta(attemptedTranslationsMeta);
			contents.set(2, 3, ClickableItem.empty(attemptedTranslations));

			/* Successful translations button */
			ItemStack local = XMaterial.BOOKSHELF.parseItem();
			ItemMeta localMeta = local.getItemMeta();
			localMeta.setDisplayName(refs.getMsg("wwcsLocalization", ChatColor.AQUA + "" + (currRecord.getLocalizationCode().isEmpty() ? refs.checkOrX(false) : currRecord.getLocalizationCode()), inPlayer));
			local.setItemMeta(localMeta);
			contents.set(3, 4, ClickableItem.empty(local));

			/* Successful translations button */
			ItemStack successfulTranslations = XMaterial.WRITTEN_BOOK.parseItem();
			ItemMeta successfulTranslationsMeta = successfulTranslations.getItemMeta();
			successfulTranslationsMeta.setDisplayName(refs.getMsg("wwcsSuccessfulTranslations", ChatColor.AQUA + "" + currRecord.getSuccessfulTranslations(), inPlayer));
			successfulTranslations.setItemMeta(successfulTranslationsMeta);
			contents.set(2, 5, ClickableItem.empty(successfulTranslations));
			
			/* Last translation time button */
			ItemStack lastTranslationTime = XMaterial.CLOCK.parseItem();
			ItemMeta lastTranslationTimeMeta = lastTranslationTime.getItemMeta();
			lastTranslationTimeMeta.setDisplayName(refs.getMsg("wwcsLastTranslationTime", ChatColor.AQUA + "" + currRecord.getLastTranslationTime(), inPlayer));
			lastTranslationTime.setItemMeta(lastTranslationTimeMeta);
			contents.set(2, 7, ClickableItem.empty(lastTranslationTime));

	    } catch (Exception e) {
		    invManager.inventoryError(player, e);
	    }
	}

	@Override
	public void update(Player player, InventoryContents contents) {}

}
