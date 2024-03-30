package com.badskater0729.worldwidechat.conversations.configuration;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui;
import com.badskater0729.worldwidechat.util.CommonRefs;
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresDatabaseNameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresDatabaseName"));
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresHostnameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresHostname"));
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresOptionalArgsInput", main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.postgresOptionalArgs").toString() : "empty");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            if (input.equalsIgnoreCase("clear")) {
                main.getConfigManager().getMainConfig().set("Storage.postgresOptionalArgs", new String[0]);
                final TextComponent badChange = Component.text()
                        .content(refs.getMsg("wwcConfigConversationPostgresOptionalArgsCleared"))
                        .color(NamedTextColor.YELLOW)
                        .build();
                refs.sendMsg((Player)context.getForWhom(), badChange);
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresPasswordInput", main.getConfigManager().getMainConfig().getString("Storage.postgresPassword"));
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresPortInput", main.getConfigManager().getMainConfig().getString("Storage.postgresPort"));
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
            ((Player) context.getForWhom()).closeInventory();
            return ChatColor.AQUA + refs.getMsg("wwcConfigConversationPostgresUsernameInput", main.getConfigManager().getMainConfig().getString("Storage.postgresUsername"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            CommonRefs refs = main.getServerFactory().getCommonRefs();
            return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPostgresUsernameSuccess",
                    new String[] {"Storage.postgresUsername"}, new Object[] {input}, MenuGui.CONFIG_GUI_TAGS.POSTGRES_SET.smartInv);
        }
    }
}
