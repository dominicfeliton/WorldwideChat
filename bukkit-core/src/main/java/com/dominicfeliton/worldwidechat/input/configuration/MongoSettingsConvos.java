package com.dominicfeliton.worldwidechat.input.configuration;

import com.dominicfeliton.worldwidechat.input.*;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MongoSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    private static WWCInventoryManager invMan = main.getInventoryManager();

    public static class Database extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationMongoDatabaseNameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationMongoDatabaseNameSuccess",
                    new String[]{"Storage.mongoDatabaseName"}, new Object[]{input}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
        }
    }

    public static class Hostname extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationMongoHostnameInput",
                    "&6" + main.getConfigManager().getMainConfig().getString("Storage.mongoHostname"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationMongoHostnameSuccess",
                    new String[]{"Storage.mongoHostname"}, new Object[]{input}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
        }
    }

    public static class OptionalArgs extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationMongoOptionalArgsInput",
                    "&6" + (main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs").toString() : "empty"),
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Storage.mongoOptionalArgs", new String[0]);
                refs.sendMsg("wwcConfigConversationMongoOptionalArgsCleared",
                        "",
                        "&e",
                        currPlayer);
                return InputResult.repeat();
            } else {
                return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationMongoOptionalArgsSuccess",
                        new String[]{"Storage.mongoOptionalArgs"}, new Object[]{input.split(",")}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
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
            return refs.getPlainMsg("wwcConfigConversationMongoPasswordInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationMongoPasswordSuccess",
                    new String[]{"Storage.mongoPassword"}, new Object[]{input}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
        }
    }

    public static class Port extends NumericInputPrompt {

        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationMongoPortInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        protected InputResult acceptValidatedInput(@NotNull InputContext context, Number input) {
            return invMan.genericConfigInput(input.intValue() != 0, context, "wwcConfigConversationMongoPortSuccess",
                    new String[]{"Storage.mongoPort"}, new Object[]{input}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
        }

    }

    public static class Username extends StringInputPrompt {
        @Override
        public @NotNull String getPromptText(InputContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return refs.getPlainMsg("wwcConfigConversationMongoUsernameInput",
                    "&cREDACTED",
                    "&b",
                    currPlayer);
        }

        @Override
        public InputResult acceptInput(@NotNull InputContext context, String input) {
            return invMan.genericConfigInput(!input.equals("0"), context, "wwcConfigConversationMongoUsernameSuccess",
                    new String[]{"Storage.mongoUsername"}, new Object[]{input}, CONFIG_GUI_TAGS.MONGO_SET.inv.get());
        }
    }

}
