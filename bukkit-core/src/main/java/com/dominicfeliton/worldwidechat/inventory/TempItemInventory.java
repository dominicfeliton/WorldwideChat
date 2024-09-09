package com.dominicfeliton.worldwidechat.inventory;

import com.cryptomorin.xseries.XMaterial;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TempItemInventory implements InventoryProvider {

    private ItemStack displayedItem;
    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    private Player inPlayer;

    public TempItemInventory(ItemStack displayedItem, Player inPlayer) {
        this.displayedItem = displayedItem;
        this.inPlayer = inPlayer;
    }

    public SmartInventory getTempItemInventory() {
        return SmartInventory.builder().id("tempItemMenu").provider(this).size(5, 9)
                .manager(main.getInventoryManager()).title(refs.getPlainMsg("wwcGUITempItem",
                        "",
                        "&1",
                        inPlayer))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        /* Set borders to green */
        ItemStack customDefaultBorders = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
        ItemMeta defaultBorderMeta = customDefaultBorders.getItemMeta();
        defaultBorderMeta.setDisplayName(" ");
        customDefaultBorders.setItemMeta(defaultBorderMeta);
        contents.fillBorders(ClickableItem.empty(customDefaultBorders));

        /* Display item in center */
        contents.set(2, 4, ClickableItem.empty(displayedItem));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

}
