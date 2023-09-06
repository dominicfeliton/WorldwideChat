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
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;;

public class MongoSettingsConvos {

private static WorldwideChat main = WorldwideChat.instance;
	
	public static class Database extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoDatabaseNameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoDatabaseName"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoDatabaseNameSuccess", 
					new String[] {"Storage.mongoDatabaseName"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class Hostname extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoHostnameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoHostname"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoHostnameSuccess", 
					new String[] {"Storage.mongoHostname"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class OptionalArgs extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoOptionalArgsInput", main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs") != null ? main.getConfigManager().getMainConfig().getList("Storage.mongoOptionalArgs").toString() : "empty");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("clear")) {
				main.getConfigManager().getMainConfig().set("Storage.mongoOptionalArgs", new String[0]);
				final TextComponent badChange = Component.text()
								.content(getMsg("wwcConfigConversationMongoOptionalArgsCleared"))
								.color(NamedTextColor.YELLOW)
						.build();
				sendMsg((Player)context.getForWhom(), badChange);
				return this;
			} else {
				return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoOptionalArgsSuccess", 
						new String[] {"Storage.mongoOptionalArgs"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
			}
		}
	}
	
	public static class Password extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoPasswordInput", main.getConfigManager().getMainConfig().getString("Storage.mongoPassword"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoPasswordSuccess", 
					new String[] {"Storage.mongoPassword"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
	public static class Port extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoPortInput", main.getConfigManager().getMainConfig().getString("Storage.mongoPort"));
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationMongoPortSuccess", 
					new String[] {"Storage.mongoPort"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
		
	}
	
	public static class Username extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationMongoUsernameInput", main.getConfigManager().getMainConfig().getString("Storage.mongoUsername"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoUsernameSuccess", 
					new String[] {"Storage.mongoUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
}
