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

public class LibreSettingsConvos {

    private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationLibreTranslateApiKeyInput", main.getConfigManager().getMainConfig().getString("Translator.libreAPIKey"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationLibreTranslateApiKeySuccess", 
					new String[] {"Translator.libreAPIKey", "Translator.useLibreTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.LIBRE_TRANS_SET.smartInv);
		}
	}
	
	public static class Url extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationLibreURLInput", main.getConfigManager().getMainConfig().getString("Translator.libreURL"));
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationLibreURLSuccess", 
					new String[] {"Translator.libreURL", "Translator.useLibreTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.LIBRE_TRANS_SET.smartInv);
		}
	}
	
}
