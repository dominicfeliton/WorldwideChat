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

public class OllamaSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class Url extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOllamaURLInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.ollamaURL"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationOllamaURLSuccess",
                    new String[]{"Translator.ollamaURL", "Translator.useOllama"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.OLLAMA_TRANS_SET.smartInv);
        }
    }

    public static class Model extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOllamaModelInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.ollamaModel"),
                    "&b",
                    currPlayer);
        }

        @Override
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationOllamaModelSuccess",
                    new String[]{"Translator.ollamaModel", "Translator.useOllama"}, new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.OLLAMA_TRANS_SET.smartInv);
        }
    }

}
