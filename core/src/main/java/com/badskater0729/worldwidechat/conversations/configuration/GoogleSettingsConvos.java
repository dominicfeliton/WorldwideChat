package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;

import net.md_5.bungee.api.ChatColor;

public class GoogleSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationGoogleTranslateAPIKeyInput", main.getConfigManager().getMainConfig().getString("Translator.googleTranslateAPIKey"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationGoogleTranslateAPIKeySuccess", 
					new String[] {"Translator.googleTranslateAPIKey", "Translator.useGoogleTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.smartInv);
		}
	}
	
}
