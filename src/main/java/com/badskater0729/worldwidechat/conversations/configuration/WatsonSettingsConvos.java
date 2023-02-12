package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;;

public class WatsonSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationWatsonAPIKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationWatsonAPIKeySuccess", 
					new String[] {"Translator.watsonAPIKey", "Translator.useWatsonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv);
		}
	}
	
	public static class ServiceUrl extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationWatsonURLInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.watsonURL")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationWatsonURLSuccess", 
					new String[] {"Translator.watsonURL", "Translator.useWatsonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv);
		}
	}
	
}
