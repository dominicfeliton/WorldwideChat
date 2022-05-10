package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class SQLSettingsHostnameConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSQLHostnameInput", new String[] {main.getConfigManager().getMainConfig().getString("Storage.sqlHostname")});
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationSQLHostnameSuccess", 
				new String[] {"Storage.sqlHostname"}, new Object[] {input}, TAGS.SQL_SET.smartInv);
	}

}
