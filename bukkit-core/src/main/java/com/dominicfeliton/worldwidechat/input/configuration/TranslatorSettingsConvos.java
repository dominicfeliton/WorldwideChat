package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TranslatorSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class CharacterLimit extends NumericInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationCharacterLimitInput",
                    "&6" + main.getMessageCharLimit(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() > 0 && input.intValue() <= 255, context, "wwcConfigConversationCharacterLimitSuccess",
                    "Translator.messageCharLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.inv.get());
        }
    }

    public static class ErrorLimit extends NumericInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationErrorLimitInput",
                    "&6" + main.getErrorLimit(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess",
                    "Translator.errorLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.inv.get());
        }
    }

    public static class GlobalRateLimit extends NumericInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationRateLimitInput",
                    "&6" + main.getGlobalRateLimit(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess",
                    "Translator.rateLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.inv.get());
        }
    }

    public static class TranslationCache extends NumericInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationTranslationCacheInput",
                    "&6" + main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize"),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess",
                    "Translator.translatorCacheSize", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.inv.get());
        }
    }

    public static class IgnoreErrors extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationIgnoreErrorsInput",
                    "&6" + (main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore") != null ? main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore").toString() : "empty"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Translator.errorsToIgnore", new String[0]);
                refs.sendMsg("wwcConfigConversationIgnoreErrorsCleared",
                        "",
                        "&e",
                        currPlayer);
                return InputResult.repeat();
            } else {
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationIgnoreErrorsSuccess",
                        new String[]{"Translator.errorsToIgnore"}, new Object[]{input.split(",")}, CONFIG_GUI_TAGS.TRANS_SET.inv.get());
            }
        }
    }
}
