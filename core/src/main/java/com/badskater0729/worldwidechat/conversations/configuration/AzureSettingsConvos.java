package com.badskater0729.worldwidechat.conversations.configuration;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui;
import com.badskater0729.worldwidechat.util.CommonRefs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class AzureSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    public static class ApiKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAzureTranslateApiKeyInput", main.getConfigManager().getMainConfig().getString("Translator.azureAPIKey"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAzureTranslateApiKeySuccess",
                    new String[] {"Translator.azureAPIKey", "Translator.useAzureTranslate"}, new Object[] {input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv);
        }
    }

    public static class Region extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAzureTranslateRegionInput", main.getConfigManager().getMainConfig().getString("Translator.azureRegion"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAzureTranslateRegionSuccess",
                    new String[] {"Translator.azureRegion", "Translator.useAzureTranslate"}, new Object[] {input, false}, MenuGui.CONFIG_GUI_TAGS.AZURE_TRANS_SET.smartInv);
        }
    }
}