package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SystranSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSystranTranslateApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSystranTranslateApiKeySuccess",
                    new String[] {"Translator.systranAPIKey", "Translator.useSystranTranslate"}, new Object[] {input, false}, MenuGui.CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.smartInv);
        }
    }

}
