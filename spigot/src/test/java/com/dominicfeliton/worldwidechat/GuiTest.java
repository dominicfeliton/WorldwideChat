package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class GuiTest extends WWCIntegrationTest {

    @Test
    void translateCommandWithoutArgumentsOpensTranslateGui() {
        PlayerMock player = WWCTestSupport.addOpPlayer("GuiUser");

        player.performCommand("wwct");

        SmartInventory inventory = openedSmartInventory(player);
        assertEquals("translateMainMenu", inventory.getId());
        assertEquals(5, inventory.getRows());
        assertEquals(9, inventory.getColumns());

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        assertEquals(45, topInventory.getSize());
        assertEquals(Material.COMPASS, topInventory.getItem(22).getType());
    }

    @Test
    void translateGuiStopButtonStopsExistingTranslationSession() {
        PlayerMock player = WWCTestSupport.addOpPlayer("GuiStopUser");
        player.performCommand("wwct en es");

        player.performCommand("wwct");
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        assertEquals(Material.BARRIER, topInventory.getItem(13).getType());

        plugin().getInventoryManager()
                .getContents(player)
                .orElseThrow(() -> new AssertionError("Expected translate GUI contents to exist."))
                .get(1, 4)
                .orElseThrow(() -> new AssertionError("Expected translate GUI stop button to exist."))
                .run((org.bukkit.event.inventory.InventoryClickEvent) null);

        assertFalse(plugin().isActiveTranslator(player));
        assertEquals("translateMainMenu", openedSmartInventory(player).getId());
    }

    @Test
    void statsCommandOpensStatsGuiForExistingRecord() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StatsGuiUser");
        plugin().addPlayerRecord(new PlayerRecord("None", player.getUniqueId().toString(), 7, 6));

        new WWCStatsGuiMainMenu(player.getUniqueId().toString(), player)
                .getStatsMainMenu()
                .open(player);

        SmartInventory inventory = openedSmartInventory(player);
        assertEquals("statsMainMenu", inventory.getId());
        assertEquals(5, inventory.getRows());
        assertEquals(9, inventory.getColumns());
        assertEquals(Material.WRITABLE_BOOK, player.getOpenInventory().getTopInventory().getItem(21).getType());
        assertEquals(Material.WRITTEN_BOOK, player.getOpenInventory().getTopInventory().getItem(23).getType());
    }

    @Test
    void configurationCommandOpensGeneralSettingsGui() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ConfigGuiUser");

        player.performCommand("wwcc");

        SmartInventory inventory = openedSmartInventory(player);
        assertEquals("generalSettingsMenu", inventory.getId());
        assertEquals(4, inventory.getRows());
        assertEquals(9, inventory.getColumns());
        assertEquals(Material.NAME_TAG, player.getOpenInventory().getTopInventory().getItem(10).getType());
    }

    private SmartInventory openedSmartInventory(PlayerMock player) {
        return plugin().getInventoryManager().getInventory(player)
                .orElseThrow(() -> new AssertionError("Expected a SmartInventory to be open."));
    }
}
