package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PostgresSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class Database extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresDatabaseNameSuccess",
                    new String[]{"Storage.postgresDatabaseName"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class Hostname extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresHostnameSuccess",
                    new String[]{"Storage.postgresHostname"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class OptionalArgs extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Storage.postgresOptionalArgs", new String[0]);
                refs.sendMsg("wwcConfigConversationPostgresOptionalArgsCleared",
                        "",
                        "&e",
                        currPlayer);
                return this;
            } else {
                return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresOptionalArgsSuccess",
                        new String[]{"Storage.postgresOptionalArgs"}, new Object[]{input.split(",")}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
            }
        }
    }

    public static class Password extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresPasswordSuccess",
                    new String[]{"Storage.postgresPassword"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class Port extends NumericPrompt {

        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
            return invMan.genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationPostgresPortSuccess",
                    new String[]{"Storage.postgresPort"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }

    }

    public static class Username extends StringPrompt {
        @Override
        public @NotNull String getPromptText(ConversationContext context) {
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
        public Prompt acceptInput(@NotNull ConversationContext context, String input) {
            return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresUsernameSuccess",
                    new String[]{"Storage.postgresUsername"}, new Object[]{input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }
}
