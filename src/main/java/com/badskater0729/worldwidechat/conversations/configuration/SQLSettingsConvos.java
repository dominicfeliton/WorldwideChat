package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;

public class SQLSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class Database extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLDatabaseNameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlDatabaseName")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLDatabaseNameSuccess", 
					new String[] {"Storage.sqlDatabaseName"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Hostname extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLHostnameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlHostname")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLHostnameSuccess", 
					new String[] {"Storage.sqlHostname"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class OptionalArgs extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLOptionalArgsInput", new String[] {main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs").toString() : "empty"});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("clear")) {
				main.getConfigManager().getMainConfig().set("Storage.sqlOptionalArgs", new String[0]);
				final TextComponent badChange = Component.text()
								.content(getMsg("wwcConfigConversationSQLOptionalArgsCleared"))
								.color(NamedTextColor.YELLOW)
						.build();
				sendMsg((Player)context.getForWhom(), badChange);
				return this;
			} else {
				return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLOptionalArgsSuccess", 
						new String[] {"Storage.sqlOptionalArgs"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
			}
		}
	}
	
	public static class Password extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLPasswordInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlPassword")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLPasswordSuccess", 
					new String[] {"Storage.sqlPassword"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Port extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLPortInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlPort")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationSQLPortSuccess", 
					new String[] {"Storage.sqlPort"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
		
	}
	
	public static class Username extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSQLUsernameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlUsername")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLUsernameSuccess", 
					new String[] {"Storage.sqlUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
}
