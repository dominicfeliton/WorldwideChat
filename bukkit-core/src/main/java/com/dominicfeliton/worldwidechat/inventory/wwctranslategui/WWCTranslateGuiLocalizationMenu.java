package com.dominicfeliton.worldwidechat.inventory.wwctranslategui;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.commands.WWCLocalize;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;

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
                .title(refs.getPlainMsg("wwctGUILocalizeTitle",
                        "",
                        "&9",
                        inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetPlayerUUID));

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
            TreeSet<SupportedLang> filteredLocalLangs = new TreeSet<>(CommonRefs.supportedPluginLangCodes.values());
            ClickableItem[] listOfAvailableLangs = new ClickableItem[filteredLocalLangs.size()];

            /* Add each supported language from each respective translator */
            int i = 0;
            for (SupportedLang eaLang : filteredLocalLangs) {
                String currLang = eaLang.getLangCode();
                ItemStack itemForLang = XMaterial.BOOK.parseItem();
                ItemMeta itemForLangMeta = itemForLang.getItemMeta();

                /* Add Glow Effects */
                ArrayList<String> lore = new ArrayList<>();
                refs.debugMsg(eaLang.toString());
                if (userLang.equalsIgnoreCase(currLang)) {
                    invManager.addGlowEffect(itemForLangMeta);
                    lore.add(refs.getPlainMsg("wwcConfigGUICurrentPlayerLang",
                            "",
                            "&e&o",
                            inPlayer));
                }
                if (refs.isSameLang(currLang, main.getConfigManager().getMainConfig().getString("General.pluginLang"), CommonRefs.LangType.LOCAL)) {
                    lore.add(refs.getPlainMsg("wwctGUILocalizationSameAsServer",
                            "",
                            "&e&o",
                            player));
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
                    WWCLocalize localize = new WWCLocalize((CommandSender) player, null, null, new String[]{targetPlayer.getName(), currLang});
                    localize.processCommand();
                    new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu().open(player);
                });

                // Iterate
                i++;
            }

            /* 28 langs per page, start at 1, 1 */
            pagination.setItems(listOfAvailableLangs);
            pagination.setItemsPerPage(28);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

            /* Bottom Left Option: Previous Page */
            if (!pagination.isFirst()) {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{getLocalizationInventory()});
            } else {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu()});
            }

            /* Bottom Middle Option: Stop */
            if (!currRecord.getLocalizationCode().isEmpty()) {
                ItemStack stopButton = XMaterial.BARRIER.parseItem();
                ItemMeta stopMeta = stopButton.getItemMeta();
                stopMeta.setDisplayName(refs.getPlainMsg("wwctGUILocalizeStopButton",
                        "",
                        "&c",
                        inPlayer));
                stopButton.setItemMeta(stopMeta);
                contents.set(5, 4, ClickableItem.of(stopButton, e -> {
                    WWCLocalize localize = new WWCLocalize((CommandSender) player, null, null, new String[]{targetPlayer.getName(), "stop"});
                    localize.processCommand();
                    new WWCTranslateGuiMainMenu(targetPlayerUUID, inPlayer).getTranslateMainMenu().open(player);
                }));
            }

            /* Bottom Right Option: Next Page */
            if (!pagination.isLast()) {
                invManager.setCommonButton(5, 6, player, contents, "Next");
            }

            /* Last Option: Page Number */
            invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[]{pagination.getPage() + 1 + ""});
        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        invManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
    }
}
