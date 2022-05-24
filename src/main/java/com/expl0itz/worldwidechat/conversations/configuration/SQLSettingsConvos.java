package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class SQLSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class Database extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLDatabaseNameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlDatabaseName")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLDatabaseNameSuccess", 
					new String[] {"Storage.sqlDatabaseName"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Hostname extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLHostnameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlHostname")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLHostnameSuccess", 
					new String[] {"Storage.sqlHostname"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class OptionalArgs extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLOptionalArgsInput", new String[] {main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs").toString() : "empty"});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("clear")) {
				main.getConfigManager().getMainConfig().set("Storage.sqlOptionalArgs", new String[0]);
				final TextComponent badChange = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcConfigConversationSQLOptionalArgsCleared"))
								.color(NamedTextColor.YELLOW))
						.build();
				CommonDefinitions.sendMessage((Player)context.getForWhom(), badChange);
				return this;
			} else {
				return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLOptionalArgsSuccess", 
						new String[] {"Storage.sqlOptionalArgs"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
			}
		}
	}
	
	public static class Password extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLPasswordInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlPassword")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLPasswordSuccess", 
					new String[] {"Storage.sqlPassword"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Port extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLPortInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlPort")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() != 0, context, "wwcConfigConversationSQLPortSuccess", 
					new String[] {"Storage.sqlPort"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
		
	}
	
	public static class Username extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLUsernameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlUsername")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLUsernameSuccess", 
					new String[] {"Storage.sqlUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
}