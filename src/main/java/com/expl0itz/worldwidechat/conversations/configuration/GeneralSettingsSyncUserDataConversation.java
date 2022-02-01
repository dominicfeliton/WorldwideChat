package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.ConfigurationGeneralSettingsGUI;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsSyncUserDataConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSyncUserDataDelayInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("General.syncUserDataDelay")});
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		return CommonDefinitions.genericConfigConversation(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), ConfigurationGeneralSettingsGUI.generalSettings);
	}

}
