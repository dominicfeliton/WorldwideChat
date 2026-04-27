package com.dominicfeliton.worldwidechat.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WWCInventoryManagerInputMethodTest {

    @Test
    void inputMethodMaterialMapsConfiguredState() {
        assertEquals(Material.GOLD_BLOCK, WWCInventoryManager.getInputMethodButtonMaterial("auto"));
        assertEquals(Material.EMERALD_BLOCK, WWCInventoryManager.getInputMethodButtonMaterial("paper-dialog"));
        assertEquals(Material.REDSTONE_BLOCK, WWCInventoryManager.getInputMethodButtonMaterial("conversation"));
    }

    @Test
    void inputMethodCycleUsesAutoPaperDialogConversationOrder() {
        assertEquals("paper-dialog", WWCInventoryManager.getNextInputMethodValue("auto"));
        assertEquals("conversation", WWCInventoryManager.getNextInputMethodValue("paper-dialog"));
        assertEquals("auto", WWCInventoryManager.getNextInputMethodValue("conversation"));
    }

    @Test
    void invalidInputMethodFallsBackToAutoState() {
        assertEquals(Material.GOLD_BLOCK, WWCInventoryManager.getInputMethodButtonMaterial("invalid"));
        assertEquals("paper-dialog", WWCInventoryManager.getNextInputMethodValue("invalid"));
    }
}
