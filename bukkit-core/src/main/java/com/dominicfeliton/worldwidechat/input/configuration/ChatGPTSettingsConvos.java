package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatGPTSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
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
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatGPTApiKeySuccess",
                    new String[]{"Translator.chatGPTAPIKey", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.inv.get());
        }
    }

    public static class Url extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
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
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatGPTURLSuccess",
                    new String[]{"Translator.chatGPTURL", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.inv.get());
        }
    }

    public static class Model extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
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
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationChatGPTModelSuccess",
                    new String[]{"Translator.chatGPTModel", "Translator.useChatGPT"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.CHATGPT_TRANS_SET.inv.get());
        }
    }

}
