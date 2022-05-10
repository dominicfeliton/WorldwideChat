package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class TranslatorSettingsGlobalRateConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationRateLimitInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("Translator.rateLimit")});
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		return CommonDefinitions.genericConfigConversation(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess", 
				"Translator.rateLimit", input.intValue(), TAGS.TRANS_SET.smartInv);
	}

}
