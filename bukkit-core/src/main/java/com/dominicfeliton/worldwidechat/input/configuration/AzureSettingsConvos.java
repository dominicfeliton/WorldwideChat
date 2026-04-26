package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AzureSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAzureTranslateApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationAzureTranslateApiKeySuccess",
                    new String[]{"Translator.azureAPIKey", "Translator.useAzureTranslate"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.inv.get());
        }
    }

    public static class Region extends StringInputPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAzureTranslateRegionInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.azureRegion"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationAzureTranslateRegionSuccess",
                    new String[]{"Translator.azureRegion", "Translator.useAzureTranslate"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.inv.get());
        }
    }
}