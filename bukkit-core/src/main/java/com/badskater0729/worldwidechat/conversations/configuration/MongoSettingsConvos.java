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

import com.badskater0729.worldwidechat.util.CommonRefs;

public class MongoSettingsConvos {

private static WorldwideChat main = WorldwideChat.instance;
	
	public static class Database extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoDatabaseNameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoDatabaseNameSuccess",
					new String[] {"Storage.mongoDatabaseName"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class Hostname extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoHostnameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoHostname"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoHostnameSuccess",
					new String[] {"Storage.mongoHostname"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class OptionalArgs extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoOptionalArgsInput", (main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs").toString() : "empty"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			if (input.equalsIgnoreCase("clear")) {
				Player currPlayer = ((Player) context.getForWhom());
				main.getConfigManager().getMainConfig().set("Storage.mongoOptionalArgs", new String[0]);
				final TextComponent badChange = Component.text()
								.content(refs.getMsg("wwcConfigConversationMongoOptionalArgsCleared", currPlayer))
								.color(NamedTextColor.YELLOW)
						.build();
				refs.sendMsg(currPlayer, badChange);
				return this;
			} else {
				return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoOptionalArgsSuccess",
						new String[] {"Storage.mongoOptionalArgs"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
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
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoPasswordInput", main.getConfigManager().getMainConfig().getString("Storage.mongoPassword"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoPasswordSuccess",
					new String[] {"Storage.mongoPassword"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class Port extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoPortInput", main.getConfigManager().getMainConfig().getString("Storage.mongoPort"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationMongoPortSuccess",
					new String[] {"Storage.mongoPort"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
		
	}
	
	public static class Username extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationMongoUsernameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoUsername"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoUsernameSuccess",
					new String[] {"Storage.mongoUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
}
