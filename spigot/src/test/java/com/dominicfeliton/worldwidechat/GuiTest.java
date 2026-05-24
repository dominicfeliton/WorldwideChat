package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.PlayerRecord;
import com.dominicfeliton.worldwidechat.input.InputContext;
import com.dominicfeliton.worldwidechat.input.InputResult;
import com.dominicfeliton.worldwidechat.input.configuration.GeneralSettingsConvos;
import com.dominicfeliton.worldwidechat.inventory.wwcstatsgui.WWCStatsGuiMainMenu;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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

    @Test
    void objectTranslationConcurrencyPromptAcceptsOnlyOneThroughFour() {
        PlayerMock player = WWCTestSupport.addOpPlayer("ConfigConcurrencyUser");
        player.performCommand("wwcc");

        GeneralSettingsConvos.ObjectTranslationConcurrency prompt = new GeneralSettingsConvos.ObjectTranslationConcurrency();
        InputContext context = new InputContext(player);

        assertEquals(InputResult.Action.REPEAT, prompt.acceptInput(context, "0").getAction());
        assertEquals(InputResult.Action.REPEAT, prompt.acceptInput(context, "5").getAction());
        assertEquals(InputResult.Action.COMPLETE, prompt.acceptInput(context, "3").getAction());
        assertEquals(3, plugin().getConfigManager().getMainConfig().getInt("General.objectTranslationConcurrencyLimit"));
    }

    @Test
    void storageProviderMenusOpenDistinctProviderLayouts() {
        PlayerMock player = WWCTestSupport.addOpPlayer("StorageGuiUser");

        player.performCommand("wwcc");
        clickOpenItem(player, 3, 6, "Expected configuration next-page button.");

        assertEquals("storageSettingsMenu", openedSmartInventory(player).getId());
        ClickableItem sqlButton = clickableItem(player, 1, 1, "Expected SQL storage button.");
        ClickableItem mongoButton = clickableItem(player, 1, 2, "Expected MongoDB storage button.");
        ClickableItem postgresButton = clickableItem(player, 1, 3, "Expected PostgreSQL storage button.");

        sqlButton.run((org.bukkit.event.inventory.InventoryClickEvent) null);
        assertStorageProviderMenu(player, "sqlSettingsMenu", 4, "SQL Configuration",
                "Toggle SQL", "Change SQL Hostname", 29, 31);

        mongoButton.run((org.bukkit.event.inventory.InventoryClickEvent) null);
        assertStorageProviderMenu(player, "mongoSettingsMenu", 3, "MongoDB Configuration",
                "Toggle MongoDB", "Change MongoDB Hostname", 20, 22);

        postgresButton.run((org.bukkit.event.inventory.InventoryClickEvent) null);
        assertStorageProviderMenu(player, "postgresSettingsMenu", 4, "PostgreSQL Configuration",
                "Toggle PostgreSQL", "Change PostgreSQL Hostname", 29, 31);
    }

    private SmartInventory openedSmartInventory(PlayerMock player) {
        return plugin().getInventoryManager().getInventory(player)
                .orElseThrow(() -> new AssertionError("Expected a SmartInventory to be open."));
    }

    private ClickableItem clickableItem(PlayerMock player, int row, int column, String failureMessage) {
        return plugin().getInventoryManager()
                .getContents(player)
                .orElseThrow(() -> new AssertionError("Expected GUI contents to be available."))
                .get(row, column)
                .orElseThrow(() -> new AssertionError(failureMessage));
    }

    private void clickOpenItem(PlayerMock player, int row, int column, String failureMessage) {
        clickableItem(player, row, column, failureMessage)
                .run((org.bukkit.event.inventory.InventoryClickEvent) null);
    }

    private void assertStorageProviderMenu(PlayerMock player, String expectedId, int expectedRows,
                                           String expectedTitle, String expectedToggleLabel,
                                           String expectedHostnameLabel, int previousSlot, int quitSlot) {
        SmartInventory inventory = openedSmartInventory(player);
        assertEquals(expectedId, inventory.getId());
        assertEquals(expectedRows, inventory.getRows());
        assertEquals(9, inventory.getColumns());
        assertEquals(expectedTitle, ChatColor.stripColor(inventory.getTitle()));

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        assertEquals(expectedRows * 9, topInventory.getSize());
        assertDisplayName(expectedToggleLabel, topInventory.getItem(10));
        assertDisplayName(expectedHostnameLabel, topInventory.getItem(11));
        assertEquals(Material.RED_STAINED_GLASS, topInventory.getItem(previousSlot).getType());
        assertEquals(Material.BARRIER, topInventory.getItem(quitSlot).getType());
    }

    private void assertDisplayName(String expected, ItemStack item) {
        assertNotNull(item);
        assertTrue(item.hasItemMeta());
        assertEquals(expected, ChatColor.stripColor(item.getItemMeta().getDisplayName()));
    }
}
