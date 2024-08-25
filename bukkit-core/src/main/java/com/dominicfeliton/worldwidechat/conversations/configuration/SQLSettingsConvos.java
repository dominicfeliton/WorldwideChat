package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
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
import org.jetbrains.annotations.NotNull;

public class SQLSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = main.getInventoryManager();

	public static class Database extends StringPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationSQLDatabaseNameInput",
					"&6"+main.getConfigManager().getMainConfig().getString("Storage.sqlDatabaseName"),
					"&b",
					currPlayer);
		}

		@Override
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLDatabaseNameSuccess",
					new String[] {"Storage.sqlDatabaseName"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Hostname extends StringPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationSQLHostnameInput",
					"&6"+main.getConfigManager().getMainConfig().getString("Storage.sqlHostname"),
					"&b",
					currPlayer);
		}

		@Override
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLHostnameSuccess",
					new String[] {"Storage.sqlHostname"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class OptionalArgs extends StringPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationSQLOptionalArgsInput",
					"&6"+(main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.sqlOptionalArgs").toString() : "empty"),
					"&b",
					currPlayer);
		}

		@Override
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			if (input.equalsIgnoreCase("clear")) {
				Player currPlayer = ((Player) context.getForWhom());
				main.getConfigManager().getMainConfig().set("Storage.sqlOptionalArgs", new String[0]);
				refs.sendMsg("wwcConfigConversationSQLOptionalArgsCleared",
						"",
						"&e",
						currPlayer);
				return this;
			} else {
				return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLOptionalArgsSuccess",
						new String[] {"Storage.sqlOptionalArgs"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
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
			return refs.getPlainMsg("wwcConfigConversationSQLPasswordInput",
					"&cREDACTED",
					"&b",
					currPlayer);
		}

		@Override
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLPasswordSuccess",
					new String[] {"Storage.sqlPassword"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
	public static class Port extends NumericPrompt {

		@Override
		public @NotNull String getPromptText(ConversationContext context) {
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
		protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationSQLPortSuccess",
					new String[] {"Storage.sqlPort"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
		
	}
	
	public static class Username extends StringPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
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
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationSQLUsernameSuccess",
					new String[] {"Storage.sqlUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.SQL_SET.smartInv);
		}
	}
	
}
