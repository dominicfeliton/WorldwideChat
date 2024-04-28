package com.badskater0729.worldwidechat.inventory.configuration;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.WWCInventoryManager;
import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.SupportedLang;
import com.cryptomorin.xseries.XMaterial;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MessagesOverridePickLangGui implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private WWCInventoryManager invManager = main.getInventoryManager();

    public SmartInventory getMessagesOverridePickLangGui() {
        return SmartInventory.builder().id("overrideMessagesMenuPicker")
                .provider(this).size(6, 9)
                .manager(invManager)
                .title(ChatColor.BLUE + refs.getMsg("wwcConfigGUIChatMessagesOverrideSettingsPicker", null))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            /* Yellow stained glass borders */
            invManager.setBorders(contents, XMaterial.YELLOW_STAINED_GLASS_PANE);

            /* Pagination */
            Pagination pagination = contents.pagination();
            ClickableItem[] langsFormatted = new ClickableItem[CommonRefs.supportedPluginLangCodes.size()];

            refs.debugMsg("Adding all supported langs! Amount of langs: " + CommonRefs.supportedPluginLangCodes.size());
            int currSpot = 0;
            for (SupportedLang eaLang : CommonRefs.supportedPluginLangCodes.values()) {
                String code = eaLang.getLangCode();
                ItemStack currentEntry = XMaterial.BOOKSHELF.parseItem();
                ItemMeta currentEntryMeta = currentEntry.getItemMeta();

                currentEntryMeta.setDisplayName("messages-" + code + ".yml");
                ArrayList<String> lore = new ArrayList<>();
                if (code.equalsIgnoreCase(main.getPlayerRecord(player, true).getLocalizationCode())) {
                    invManager.addGlowEffect(currentEntryMeta);
                    lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwcConfigGUICurrentPlayerLang", player));
                }
                if (refs.isSameLang(code, main.getConfigManager().getMainConfig().getString("General.pluginLang"), "local")) {
                    lore.add(ChatColor.YELLOW + "" + ChatColor.ITALIC + refs.getMsg("wwcConfigGUIMessagesServerLang", player));
                    invManager.addGlowEffect(currentEntryMeta);
                }
                if (!eaLang.getNativeLangName().isEmpty() && !eaLang.getNativeLangName().equalsIgnoreCase(eaLang.getLangName())) {
                    lore.add(eaLang.getNativeLangName());
                }
                lore.add(eaLang.getLangName());

                currentEntryMeta.setLore(lore);
                currentEntry.setItemMeta(currentEntryMeta);

                langsFormatted[currSpot] = ClickableItem.of(currentEntry, e -> {
                    // Open Specific Override GUI
                    new MessagesOverrideCurrentListGui(code, player).getOverrideMessagesSettings().open(player);
                });
                currSpot++;
            }

            /* 28 langs per page, start at 1, 1 */
            pagination.setItems(langsFormatted);
            pagination.setItemsPerPage(28);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

            /* Bottom Left Option: Previous Page */
            if (!pagination.isFirst()) {
                invManager.setCommonButton(5, 2, player, contents, "Previous");
            } else {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[] {MenuGui.CONFIG_GUI_TAGS.CHAT_SET.smartInv});
            }

            /* Bottom Right Option: Next Page */
            if (!pagination.isLast()) {
                invManager.setCommonButton(5, 6, player, contents, "Next");
            }

            /* Last Option: Current Page Number */
            invManager.setCommonButton(5, 8, player, contents, "Page Number", new String[] {pagination.getPage() + 1 + ""});
        } catch (Exception e) {
            invManager.inventoryError(player, e);
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {}
}
