package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class PostgresSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;

    public static class Database extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresDatabaseNameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresDatabaseName"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresDatabaseNameSuccess",
                    new String[] {"Storage.postgresDatabaseName"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class Hostname extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresHostnameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresHostname"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresHostnameSuccess",
                    new String[] {"Storage.postgresHostname"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class OptionalArgs extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresOptionalArgsInput", (main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs").toString() : "empty"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                Player currPlayer = ((Player) context.getForWhom());
                main.getConfigManager().getMainConfig().set("Storage.postgresOptionalArgs", new String[0]);
                final TextComponent badChange = Component.text()
                        .content(refs.getMsg("wwcConfigConversationPostgresOptionalArgsCleared", currPlayer))
                        .color(NamedTextColor.YELLOW)
                        .build();
                refs.sendMsg(currPlayer, badChange);
                return this;
            } else {
                return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresOptionalArgsSuccess",
                        new String[] {"Storage.postgresOptionalArgs"}, new Object[] {input.split(",")}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
            }
        }
    }

    public static class Password extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresPasswordInput", main.getConfigManager().getMainConfig().getString("Storage.postgresPassword"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresPasswordSuccess",
                    new String[] {"Storage.postgresPassword"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }

    public static class Port extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresPortInput", main.getConfigManager().getMainConfig().getString("Storage.postgresPort"), currPlayer);
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationPostgresPortSuccess",
                    new String[] {"Storage.postgresPort"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }

    }

    public static class Username extends StringPrompt {
        @Override
        public String getPromptText(ConversationContext context) {
            /* Close any open inventories */
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            Player currPlayer = ((Player) context.getForWhom());
            currPlayer.closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresUsernameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresUsername"), currPlayer);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresUsernameSuccess",
                    new String[] {"Storage.postgresUsername"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }
}
