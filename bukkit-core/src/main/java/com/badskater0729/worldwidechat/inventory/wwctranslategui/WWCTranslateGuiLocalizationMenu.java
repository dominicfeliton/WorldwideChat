package com.badskater0729.worldwidechat.inventory.wwctranslategui;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.commands.WWCLocalize;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.PlayerRecord;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class WWCTranslateGuiLocalizationMenu implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private WWCInventoryManager invManager = main.getInventoryManager();

    private String targetPlayerUUID = "";

    private Player inPlayer;

    public WWCTranslateGuiLocalizationMenu(String targetPlayerUUID, Player inPlayer) {
        this.targetPlayerUUID = targetPlayerUUID;
        this.inPlayer = inPlayer;
    }

    public SmartInventory getLocalizationInventory() {
        return SmartInventory.builder().id("translateLocalization")
                .provider(this).size(6, 9)
                .manager(WorldwideChat.instance.getInventoryManager())
                .title(ChatColor.BLUE + refs.getMsg("wwctGUILocalizeTitle", inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            /* Default white stained glass borders for inactive, yellow if player has existing translation session */
            invManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
            if (main.isPlayerRecord(targetPlayerUUID) && !main.getPlayerRecord(targetPlayerUUID, true).getLocalizationCode().isEmpty()) {
                invManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
            }

            /* Init current active translator */
            PlayerRecord currRecord = main.getPlayerRecord(targetPlayerUUID, true);
            String userLang = currRecord.getLocalizationCode();

            /* Pagination: Lets you generate pages rather than set defined ones */
            Pagination pagination = contents.pagination();
            ClickableItem[] listOfAvailableLangs = new ClickableItem[CommonRefs.supportedPluginLangCodes.size()];

            /* Add each supported language from each respective translator */
            int i = 0;
            for (SupportedLang eaLang : CommonRefs.supportedPluginLangCodes.values()) {
                String currLang = eaLang.getLangCode();
                ItemStack itemForLang = XMaterial.BOOK.parseItem();
                ItemMeta itemForLangMeta = itemForLang.getItemMeta();

                /* Add Glow Effects */
                ArrayList<String> lore = new ArrayList<>();
                if (userLang.equalsIgnoreCase(currLang)) {
                    invManager.addGlowEffect(itemForLangMeta);
                    lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwcConfigGUICurrentPlayerLang", inPlayer));
                }
                if (refs.isSameLang(currLang, main.getConfigManager().getMainConfig().getString("General.pluginLang"), "local")) {
                    lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwctGUILocalizationSameAsServer", player));
                    invManager.addGlowEffect(itemForLangMeta);
                }
                if (!eaLang.getNativeLangName().isEmpty() && !eaLang.getNativeLangName().equalsIgnoreCase(eaLang.getLangName())) {
                    lore.add(eaLang.getNativeLangName());
                }
                lore.add(eaLang.getLangName());
                itemForLangMeta.setDisplayName(currLang);
                itemForLangMeta.setLore(lore);
                itemForLang.setItemMeta(itemForLangMeta);
                listOfAvailableLangs[i] = ClickableItem.of(itemForLang, e -> {
                    WWCLocalize localize = new WWCLocalize((CommandSender) player, null, null, new String[] {player.getName(), currLang});
                    localize.processCommand();
                    new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu().open(player);
                });
                i += 1;
            }

            /* 28 langs per page, start at 1, 1 */
            pagination.setItems(listOfAvailableLangs);
            pagination.setItemsPerPage(28);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

            /* Bottom Left Option: Previous Page */
            if (!pagination.isFirst()) {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {getLocalizationInventory()});
            } else {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu()});
            }

            /* Bottom Middle Option: Stop */
            if (!currRecord.getLocalizationCode().isEmpty()) {
                ItemStack stopButton = XMaterial.BARRIER.parseItem();
                ItemMeta stopMeta = stopButton.getItemMeta();
                stopMeta.setDisplayName(ChatColor.RED
                        + refs.getMsg("wwctGUILocalizeStopButton", inPlayer));
                stopButton.setItemMeta(stopMeta);
                contents.set(5, 4, ClickableItem.of(stopButton, e -> {
                    WWCLocalize localize = new WWCLocalize((CommandSender) player, null, null, new String[] {player.getName(), "stop"});
                    localize.processCommand();
                    new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu().open(player);
                }));
            }

            /* Bottom Right Option: Next Page */
            if (!pagination.isLast()) {
                invManager.setCommonButton(5, 6, player, contents, "Next");
            }

            /* Last Option: Page Number */
            invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }
}