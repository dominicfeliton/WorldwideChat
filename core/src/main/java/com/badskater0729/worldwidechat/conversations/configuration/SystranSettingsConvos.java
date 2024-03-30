package com.badskater0729.worldwidechat.conversations.configuration;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui;
import com.badskater0729.worldwidechat.util.CommonRefs;
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationSystranTranslateApiKeyInput", main.getConfigManager().getMainConfig().getString("Translator.systranAPIKey"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSystranTranslateApiKeySuccess",
                    new String[] {"Translator.systranAPIKey", "Translator.useSystranTranslate"}, new Object[] {input, false}, MenuGui.CONFIG_GUI_TAGS.SYSTRAN_TRANS_SET.smartInv);
        }
    }

}
