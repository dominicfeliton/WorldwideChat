package com.dominicfeliton.worldwidechat.inventory.wwctranslategui;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.commands.WWCGlobal;
import com.dominicfeliton.worldwidechat.commands.WWCTranslate;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;

public class WWCTranslateGuiTargetLanguage implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private WWCInventoryManager invManager = main.getInventoryManager();

    private String selectedSourceLanguage = "";
    private String targetPlayerUUID = "";

    private Player inPlayer;

    public WWCTranslateGuiTargetLanguage(String selectedSourceLanguage, String targetPlayerUUID, Player inPlayer) {
        this.selectedSourceLanguage = selectedSourceLanguage;
        this.targetPlayerUUID = targetPlayerUUID;
        this.inPlayer = inPlayer;
    }

    public SmartInventory getTargetLanguageInventory() {
        return SmartInventory.builder().id("translateTargetLanguage")
                .provider(this).size(6, 9)
                .manager(WorldwideChat.instance.getInventoryManager())
                .title(refs.getPlainMsg("wwctGUINewTranslationTarget",
                        "",
                        "&9",
                        inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            /* Default white stained glass borders for inactive, yellow if player has existing translation session */
            invManager.setBorders(contents, XMaterial.WHITE_STAINED_GLASS_PANE);
            if (!main.getActiveTranslator(targetPlayerUUID).getInLangCode().equals("")) {
                invManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);
            }

            /* Init current active translator */
            ActiveTranslator currTranslator = main.getActiveTranslator(targetPlayerUUID);

            /* Pagination: Lets you generate pages rather than set defined ones */
            Pagination pagination = contents.pagination();
            TreeSet<SupportedLang> cleanedOutLangs = new TreeSet<>(main.getSupportedOutputLangs().values());
            ClickableItem[] listOfAvailableLangs = new ClickableItem[cleanedOutLangs.size()];

            /* Add each supported language from each respective translator */
            int i = 0;
            SupportedLang userLang = refs.getSupportedLang(currTranslator.getOutLangCode(), "out");

            for (SupportedLang currLang : cleanedOutLangs) {
                boolean unsupported;

                // Change item depending on version + condition
                ItemStack itemForLang = XMaterial.ARROW.parseItem();
                if (XMaterial.TARGET.parseItem() != null) {
                    itemForLang = XMaterial.TARGET.parseItem();
                }
                if (selectedSourceLanguage.equalsIgnoreCase(currLang.getLangCode())) {
                    refs.debugMsg("Skipping " + currLang.getLangCode() + " as it is source lang...");
                    itemForLang = XMaterial.BARRIER.parseItem();
                    unsupported = true;
                } else {
                    unsupported = false;
                }
                ItemMeta itemForLangMeta = itemForLang.getItemMeta();
                ArrayList<String> lore = new ArrayList<>();

                /* Add Glow Effect */
                if (unsupported) {
                    lore.add(refs.getPlainMsg("wwctGUISourceSameLang",
                            "",
                            "&c&o",
                            inPlayer));
                }
                if (!unsupported && (userLang.getLangCode().equals(currLang.getLangCode()) || userLang.getLangName().equals(currLang.getLangName()))) {
                    invManager.addGlowEffect(itemForLangMeta);
                    lore.add(refs.getPlainMsg("wwctGUISourceOrTargetTranslationAlreadyActive",
                            "",
                            "&e&o",
                            inPlayer));
                } else if (!unsupported && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getOutLangCode().equalsIgnoreCase(currLang.getLangCode())) {
                    // If global translate is already using this as an outLang...
                    invManager.addGlowEffect(itemForLangMeta);
                    lore.add(refs.getPlainMsg("wwctGUISourceOrTargetTranslationAlreadyGlobal",
                            "",
                            "&e&o",
                            inPlayer));
                }
                itemForLangMeta.setDisplayName(currLang.getLangName());
                if (!currLang.getNativeLangName().isEmpty() && !currLang.getNativeLangName().equalsIgnoreCase(currLang.getLangName())) {
                    lore.add(currLang.getNativeLangName());
                }
                lore.add(currLang.getLangCode());
                itemForLangMeta.setLore(lore);
                itemForLang.setItemMeta(itemForLangMeta);
                String outLang = currLang.getLangCode();

                listOfAvailableLangs[i] = ClickableItem.of(itemForLang, e -> {
                    /* Send to /wwct */
                    if (unsupported) {
                        return;
                    }

                    WWCTranslate translateCommand;
                    if (!targetPlayerUUID.equals("GLOBAL-TRANSLATE-ENABLED")) {
                        translateCommand = new WWCTranslate((CommandSender) player, null, null, new String[]{main.getServer().getPlayer(UUID.fromString(targetPlayerUUID)).getName(), selectedSourceLanguage, outLang});
                    } else {
                        translateCommand = new WWCGlobal((CommandSender) player, null, null, new String[]{selectedSourceLanguage, outLang});
                    }
                    player.closeInventory();
                    translateCommand.processCommand();
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
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{getTargetLanguageInventory()});
            } else {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{new WWCTranslateGuiSourceLanguage(selectedSourceLanguage, targetPlayerUUID, inPlayer).getSourceLanguageInventory()});
            }

            /* Bottom Right Option: Next Page */
            if (!pagination.isLast()) {
                invManager.setCommonButton(5, 6, player, contents, "Next", new Object[]{getTargetLanguageInventory()});
            }

            /* Last Option: Page Number */
            invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[]{pagination.getPage() + 1 + ""});
        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        invManager.checkIfPlayerIsMissing(player, targetPlayerUUID);
    }

}
