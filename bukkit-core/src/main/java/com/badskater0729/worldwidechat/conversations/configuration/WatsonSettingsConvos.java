package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class WatsonSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationWatsonAPIKeyInput", main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationWatsonAPIKeySuccess",
					new String[] {"Translator.watsonAPIKey", "Translator.useWatsonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv);
		}
	}
	
	public static class ServiceUrl extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationWatsonURLInput", main.getConfigManager().getMainConfig().getString("Translator.watsonURL"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationWatsonURLSuccess",
					new String[] {"Translator.watsonURL", "Translator.useWatsonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.WATSON_TRANS_SET.smartInv);
		}
	}
	
}
