package com.dominicfeliton.worldwidechat.inventory.wwcstatsgui;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                .title(refs.getPlainMsg("wwcsTitle",
                        targetPlayerName,
                        "&9",
                        inPlayer))
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
                isActiveTranslatorMeta.setDisplayName(refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(true), inPlayer));
                SupportedLang inLang = refs.getSupportedLang(currTrans.getInLangCode(), CommonRefs.LangType.INPUT);
                SupportedLang outLang = refs.getSupportedLang(currTrans.getOutLangCode(), CommonRefs.LangType.OUTPUT);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransUUID", "&6" + currTrans.getUUID(), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransRateLimit", "&6" + currTrans.getRateLimit(), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransInLang", "&6" + inLang.toString(), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransOutLang", "&6" + outLang.toString(), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransOutgoing", refs.checkOrX(currTrans.getTranslatingChatOutgoing()), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransIncoming", refs.checkOrX(currTrans.getTranslatingChatIncoming()), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransBook", refs.checkOrX(currTrans.getTranslatingBook()), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransSign", refs.checkOrX(currTrans.getTranslatingSign()), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransItem", refs.checkOrX(currTrans.getTranslatingItem()), inPlayer));
                lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransEntity", refs.checkOrX(currTrans.getTranslatingEntity()), inPlayer));

                // If debug, append extra vars
                if (main.getConfigManager().getMainConfig().getBoolean("General.enableDebugMode")) {
                    lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransColorWarning", refs.checkOrX(currTrans.getCCWarning()), inPlayer));
                    lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransSignWarning", refs.checkOrX(currTrans.getSignWarning()), inPlayer));
                    lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransSaved", refs.checkOrX(currTrans.getHasBeenSaved()), inPlayer));
                    lore.add(ChatColor.LIGHT_PURPLE + "  - " + refs.getPlainMsg("wwcsActiveTransPrevRate", "&6" + currTrans.getRateLimitPreviousTime(), inPlayer));
                }
                isActiveTranslatorMeta.setLore(lore);

                isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
            } else {
                isActiveTranslator = XMaterial.RED_CONCRETE.parseItem();
                ItemMeta isActiveTranslatorMeta = isActiveTranslator.getItemMeta();
                isActiveTranslatorMeta.setDisplayName(refs.getPlainMsg("wwcsIsActiveTranslator", refs.checkOrX(false), inPlayer));
                isActiveTranslator.setItemMeta(isActiveTranslatorMeta);
            }
            contents.set(2, 1, ClickableItem.empty(isActiveTranslator));

            /* Attempted translations button */
            ItemStack attemptedTranslations = XMaterial.WRITABLE_BOOK.parseItem();
            ItemMeta attemptedTranslationsMeta = attemptedTranslations.getItemMeta();
            attemptedTranslationsMeta.setDisplayName(refs.getPlainMsg("wwcsAttemptedTranslations",
                    "&b" + currRecord.getAttemptedTranslations(), inPlayer));
            attemptedTranslations.setItemMeta(attemptedTranslationsMeta);
            contents.set(2, 3, ClickableItem.empty(attemptedTranslations));

            /* Successful translations button */
            ItemStack local = XMaterial.BOOKSHELF.parseItem();
            ItemMeta localMeta = local.getItemMeta();
            localMeta.setDisplayName(refs.getPlainMsg("wwcsLocalization",
                    "&b" + (currRecord.getLocalizationCode().isEmpty()
                            ? refs.checkOrX(false)
                            : refs.getSupportedLang(currRecord.getLocalizationCode(), CommonRefs.LangType.LOCAL)),
                    inPlayer));
            local.setItemMeta(localMeta);
            contents.set(3, 4, ClickableItem.empty(local));

            /* Successful translations button */
            ItemStack successfulTranslations = XMaterial.WRITTEN_BOOK.parseItem();
            ItemMeta successfulTranslationsMeta = successfulTranslations.getItemMeta();
            successfulTranslationsMeta.setDisplayName(refs.getPlainMsg("wwcsSuccessfulTranslations",
                    "&b" + currRecord.getSuccessfulTranslations(),
                    inPlayer));
            successfulTranslations.setItemMeta(successfulTranslationsMeta);
            contents.set(2, 5, ClickableItem.empty(successfulTranslations));

            /* Last translation time button */
            ItemStack lastTranslationTime = XMaterial.CLOCK.parseItem();
            ItemMeta lastTranslationTimeMeta = lastTranslationTime.getItemMeta();
            lastTranslationTimeMeta.setDisplayName(refs.getPlainMsg("wwcsLastTranslationTime",
                    "&b" + currRecord.getLastTranslationTime(),
                    inPlayer));
            lastTranslationTime.setItemMeta(lastTranslationTimeMeta);
            contents.set(2, 7, ClickableItem.empty(lastTranslationTime));

        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}
