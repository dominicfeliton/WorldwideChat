package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class SystranSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    public static class ApiKey extends StringPrompt {
        private CommonRefs refs = main.getServerFactory().getCommonRefs();

        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationSystranTranslateApiKeyInput", main.getConfigManager().getMainConfig().getString("Translator.systranAPIKey"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSystranTranslateApiKeySuccess",
                    new String[] {"Translator.systranAPIKey", "Translator.useSystranTranslate"}, new Object[] {input, false}, MenuGui.CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.smartInv);
        }
    }

}
