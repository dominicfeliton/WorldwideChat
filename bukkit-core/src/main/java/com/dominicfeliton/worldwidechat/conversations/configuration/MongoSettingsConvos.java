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

public class MongoSettingsConvos {

private static WorldwideChat main = WorldwideChat.instance;

private static WWCInventoryManager invMan = new WWCInventoryManager();
	
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
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoDatabaseNameSuccess",
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
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoHostnameSuccess",
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
				return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoOptionalArgsSuccess",
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
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoPasswordSuccess",
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
			return invMan.genericConfigConvo(input.intValue() != 0, context, "wwcConfigConversationMongoPortSuccess",
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
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationMongoUsernameSuccess",
					new String[] {"Storage.mongoUsername"}, new Object[] {input}, CONFIG_GUI_TAGS.MONGO_SET.smartInv);
		}
	}
	
}
