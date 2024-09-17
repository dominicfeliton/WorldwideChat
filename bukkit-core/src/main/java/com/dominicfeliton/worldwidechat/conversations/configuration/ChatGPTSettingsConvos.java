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

public class ChatGPTSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChatGPTApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChatGPTApiKeySuccess",
                    new String[]{"Translator.chatGPTAPIKey", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.smartInv);
        }
    }

    public static class Url extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChatGPTURLInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.chatGPTURL"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChatGPTURLSuccess",
                    new String[]{"Translator.chatGPTURL", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.smartInv);
        }
    }

    public static class Model extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationChatGPTModelInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.chatGPTModel"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationChatGPTModelSuccess",
                    new String[]{"Translator.chatGPTModel", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.smartInv);
        }
    }

}
