package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GeneralSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class FatalAsyncAbort extends NumericInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationFatalAsyncAbort",
                    "&6" + main.getConfigManager().getMainConfig().getString("General.fatalAsyncTaskTimeout"),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, @NotNull Number input) {
            return invMan.genericConfigInput(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, CONFIG_GUI_TAGS.GEN_SET.inv.get());
        }

    }

    public static class ObjectTranslationConcurrency extends NumericInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationObjectTranslationConcurrencyMinimumInput",
                    "&6" + main.getObjectTranslationConcurrencyLimit(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, @NotNull Number input) {
            int limit = input.intValue();
            if (limit < 1) {
                CommonRefs refs = main.getServerFactory().getCommonRefs();
                refs.sendMsg("wwcConfigConversationObjectTranslationConcurrencyMinimumInvalid", "", "&c", context.getForWhom());
                return InputResult.repeat();
            }

            return invMan.genericConfigInput(true, context,
                    "wwcConfigConversationObjectTranslationConcurrencySuccess",
                    "General.objectTranslationConcurrencyLimit",
                    limit,
                    CONFIG_GUI_TAGS.GEN_SET.inv.get());
        }

    }

    public static class Lang extends StringInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationLangInput",
                    new String[]{"&6", main.getConfigManager().getMainConfig().getString("General.pluginLang"), "&6" + refs.getFormattedLangCodes("local")},
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (refs.isSupportedLang(input, CommonRefs.LangType.LOCAL) || input.equals("0")) {
                input = !input.equals("0") ? refs.getSupportedLang(input, CommonRefs.LangType.LOCAL).getLangCode() : "0";
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationLangSuccess", "General.pluginLang", input, CONFIG_GUI_TAGS.GEN_SET.inv.get());
            }
            Player currPlayer = ((Player) context.getForWhom());
            refs.sendMsg("wwcConfigConversationLangInvalid", "", "&c", currPlayer);
            return InputResult.repeat();
        }

    }

    public static class Prefix extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            // TODO: Show color codes
            return refs.getPlainMsg("wwcConfigConversationPrefixInput",
                    "&r" + LegacyComponentSerializer.legacyAmpersand().serialize(main.getPluginPrefix()),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPrefixSuccess", "General.prefixName", input, CONFIG_GUI_TAGS.GEN_SET.inv.get());
        }
    }

    public static class SyncUserData extends NumericInputPrompt {
        @Override
        public String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSyncUserDataDelayInput",
                    "&6" + main.getSyncUserDataDelay(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return invMan.genericConfigInput(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.inv.get());
        }
    }

    public static class UpdateChecker extends NumericInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationUpdateCheckerInput",
                    "&6" + main.getUpdateCheckerDelay(),
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return invMan.genericConfigInput(input.intValue() > 10, context, "wwcConfigConversationUpdateCheckerSuccess", "General.updateCheckerDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.inv.get());
        }
    }

}
