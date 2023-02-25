package com.badskater0729.worldwidechat.conversations.configuration;

import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;
import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

public class DeepLSettingsConvos {

private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationDeepLTranslateApiKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.deepLAPIKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationDeepLTranslateApiKeySuccess", 
					new String[] {"Translator.deepLAPIKey", "Translator.useDeepLTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.DEEP_TRANS_SET.smartInv);
		}
	}
	
}
