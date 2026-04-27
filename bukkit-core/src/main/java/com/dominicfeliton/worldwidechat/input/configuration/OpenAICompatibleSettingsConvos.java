package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.input.InputContext;
import com.dominicfeliton.worldwidechat.input.InputResult;
import com.dominicfeliton.worldwidechat.input.StringInputPrompt;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpenAICompatibleSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class ApiKey extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOpenAICompatibleApiKeyInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationOpenAICompatibleApiKeySuccess",
                    new String[]{"Translator.openAICompatibleAPIKey", "Translator.useOpenAICompatible"},
                    new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.OPENAI_COMPATIBLE_TRANS_SET.inv.get());
        }
    }

    public static class Url extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOpenAICompatibleURLInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.openAICompatibleURL"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationOpenAICompatibleURLSuccess",
                    new String[]{"Translator.openAICompatibleURL", "Translator.useOpenAICompatible"},
                    new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.OPENAI_COMPATIBLE_TRANS_SET.inv.get());
        }
    }

    public static class Model extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationOpenAICompatibleModelInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Translator.openAICompatibleModel"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationOpenAICompatibleModelSuccess",
                    new String[]{"Translator.openAICompatibleModel", "Translator.useOpenAICompatible"},
                    new Object[]{input, false}, MenuGui.CONFIG_GUI_TAGS.OPENAI_COMPATIBLE_TRANS_SET.inv.get());
        }
    }
}
