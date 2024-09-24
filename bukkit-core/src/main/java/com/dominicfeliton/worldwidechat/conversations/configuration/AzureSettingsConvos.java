package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AzureSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAzureTranslateApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAzureTranslateApiKeySuccess",
                    new String[]{"Translator.azureAPIKey", "Translator.useAzureTranslate"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv);
        }
    }

    public static class Region extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationAzureTranslateRegionInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.azureRegion"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAzureTranslateRegionSuccess",
                    new String[]{"Translator.azureRegion", "Translator.useAzureTranslate"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv);
        }
    }
}