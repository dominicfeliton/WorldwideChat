package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.GeneralSettingsGUI;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsFatalAsyncAbortConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.instance;
	
	@Override
	public String getPromptText(ConversationContext context) {
        /* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationFatalAsyncAbort", new String[] {main.getConfigManager().getMainConfig().getString("General.fatalAsyncTaskTimeout")});
		
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		return CommonDefinitions.genericConfigConversation(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, GeneralSettingsGUI.generalSettings);
	}

}
