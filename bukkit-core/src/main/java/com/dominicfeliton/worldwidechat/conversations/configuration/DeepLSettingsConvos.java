package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeepLSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationDeepLTranslateApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationDeepLTranslateApiKeySuccess",
                    new String[]{"Translator.deepLAPIKey", "Translator.useDeepLTranslate"}, new Object[]{input, false}, CONFIG_GUI_TAGS.DEEP_TRANS_SET.smartInv);
        }
    }

}
