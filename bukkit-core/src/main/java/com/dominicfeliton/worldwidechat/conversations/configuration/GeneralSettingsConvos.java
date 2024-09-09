package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GeneralSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class FatalAsyncAbort extends NumericPrompt {

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull Number input) {
            return invMan.genericConfigConvo(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
        }

    }

    public static class Lang extends StringPrompt {

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (refs.isSupportedLang(input, "local") || input.equals("0")) {
                input = !input.equals("0") ? refs.getSupportedLang(input, "local").getLangCode() : "0";
                return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationLangSuccess", "General.pluginLang", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
            }
            Player currPlayer = ((Player) context.getForWhom());
            refs.sendMsg("wwcConfigConversationLangInvalid", "", "&c", currPlayer);
            return this;
        }

    }

    public static class Prefix extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPrefixSuccess", "General.prefixName", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
        }
    }

    public static class SyncUserData extends NumericPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
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
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return invMan.genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
        }
    }

    public static class UpdateChecker extends NumericPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return invMan.genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationUpdateCheckerSuccess", "General.updateCheckerDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
        }
    }

}
