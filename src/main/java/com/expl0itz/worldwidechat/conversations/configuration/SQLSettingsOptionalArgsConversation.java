package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class SQLSettingsOptionalArgsConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.instance;

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
					new String[] {"Storage.sqlOptionalArgs"}, new Object[] {input.split(",")}, TAGS.SQL_SET.smartInv);
		}
	}

}

