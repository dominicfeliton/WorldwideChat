package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SQLSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class Database extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLDatabaseNameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.sqlDatabaseName"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationSQLDatabaseNameSuccess",
                    new String[]{"Storage.sqlDatabaseName"}, new Object[]{input}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
        }
    }

    public static class Hostname extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLHostnameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.sqlHostname"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationSQLHostnameSuccess",
                    new String[]{"Storage.sqlHostname"}, new Object[]{input}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
        }
    }

    public static class OptionalArgs extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLOptionalArgsInput",
                    "&6" + (main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs").toString() : "empty"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Storage.sqlOptionalArgs", new String[0]);
                refs.sendMsg("wwcConfigConversationSQLOptionalArgsCleared",
                        "",
                        "&e",
                        currPlayer);
                return InputResult.repeat();
            } else {
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationSQLOptionalArgsSuccess",
                        new String[]{"Storage.sqlOptionalArgs"}, new Object[]{input.split(",")}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
            }
        }
    }

    public static class Password extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLPasswordInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationSQLPasswordSuccess",
                    new String[]{"Storage.sqlPassword"}, new Object[]{input}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
        }
    }

    public static class Port extends NumericInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLPortInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() != 0, context, "wwcConfigConversationSQLPortSuccess",
                    new String[]{"Storage.sqlPort"}, new Object[]{input}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
        }

    }

    public static class Username extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationSQLUsernameInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationSQLUsernameSuccess",
                    new String[]{"Storage.sqlUsername"}, new Object[]{input}, CONFIG_GUI_TAGS.SQL_SET.inv.get());
        }
    }

}
