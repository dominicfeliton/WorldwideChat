package com.dominicfeliton.worldwidechat.inventory.configuration;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.conversations.configuration.AISettingsConvos;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class AILangGui implements InventoryProvider {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private WWCInventoryManager invManager = main.getInventoryManager();

    private Player inPlayer;
    private Set<String> langs;

    public AILangGui(Player inPlayer) {
        this.inPlayer = inPlayer;
        langs = new TreeSet<>();
    }

    public SmartInventory getAILangs() {
        return SmartInventory.builder().id("aiLangMenu")
                .provider(this).size(6, 9)
                .manager(invManager)
                .title(refs.getPlainMsg("wwcConfigGUIModifyAILangsSettings", "", "&9", inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        try {
            // Refresh langs
            for (SupportedLang eaLang : main.getSupportedInputLangs().values()) {
                langs.add(eaLang.getLangCode());
            }

            // Fix this on override messages as well.
            /* Green stained glass borders */
            invManager.setBorders(contents, XMaterial.GREEN_STAINED_GLASS_PANE);

            /* Pagination */
            Pagination pagination = contents.pagination();
            ClickableItem[] currentLangs = new ClickableItem[langs.size()];

            if (!langs.isEmpty()) {
                refs.debugMsg("Adding existing AI langs to inventory! Amount of langs: " + currentLangs.length);
                int currSpot = 0;
                for (String lang : langs) {
                    ItemStack currentEntry = XMaterial.WRITABLE_BOOK.parseItem();
                    ItemMeta currentEntryMeta = currentEntry.getItemMeta();

                    currentEntryMeta.setDisplayName(lang);
                    currentEntry.setItemMeta(currentEntryMeta);
                    currentLangs[currSpot] = ClickableItem.of(currentEntry, e -> {
                        // Open Specific Override GUI
                        new AILangModifyGui(lang, inPlayer).modifyAILang().open(player);
                    });
                    currSpot++;
                }
            }

            /* 28 messages per page, start at 1, 1 */
            pagination.setItems(currentLangs);
            pagination.setItemsPerPage(28);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1).allowOverride(false));

            /* Save Button */
            invManager.setCommonButton(5, 0, player, contents,"Quit");

            /* Bottom Left Option: Previous Page */
            if (!pagination.isFirst()) {
                invManager.setCommonButton(5, 2, player, contents, "Previous");
            } else {
                invManager.setCommonButton(5, 2, player, contents, "Previous", new Object[]{MenuGui.CONFIG_GUI_TAGS.AI_SET.smartInv});
            }

            /* Bottom Middle Option: Add new AI lang */
            invManager.genericConversationButton(5 ,4, player, contents, new AISettingsConvos.AddLang(getAILangs()), XMaterial.WRITABLE_BOOK, "wwcConfigGUIAILangNewButton");

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
    public void update(Player player, InventoryContents contents) {}

}
