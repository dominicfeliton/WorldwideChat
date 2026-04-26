package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PostgresSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class Database extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationPostgresDatabaseNameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.postgresDatabaseName"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPostgresDatabaseNameSuccess",
                    new String[]{"Storage.postgresDatabaseName"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
        }
    }

    public static class Hostname extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationPostgresHostnameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.postgresHostname"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPostgresHostnameSuccess",
                    new String[]{"Storage.postgresHostname"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
        }
    }

    public static class OptionalArgs extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationPostgresOptionalArgsInput",
                    "&6" + (main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs").toString() : "empty"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Storage.postgresOptionalArgs", new String[0]);
                refs.sendMsg("wwcConfigConversationPostgresOptionalArgsCleared",
                        "",
                        "&e",
                        currPlayer);
                return InputResult.repeat();
            } else {
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPostgresOptionalArgsSuccess",
                        new String[]{"Storage.postgresOptionalArgs"}, new Object[]{input.split(",")}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
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
            return refs.getPlainMsg("wwcConfigConversationPostgresPasswordInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPostgresPasswordSuccess",
                    new String[]{"Storage.postgresPassword"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
        }
    }

    public static class Port extends NumericInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationPostgresPortInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() != 0, context, "wwcConfigConversationPostgresPortSuccess",
                    new String[]{"Storage.postgresPort"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
        }

    }

    public static class Username extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationPostgresUsernameInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationPostgresUsernameSuccess",
                    new String[]{"Storage.postgresUsername"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.inv.get());
        }
    }
}
