package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class TranslatorSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class CharacterLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationCharacterLimitInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("Translator.messageCharLimit")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > 0, context, "wwcConfigConversationCharacterLimitSuccess", 
					"Translator.messageCharLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class ErrorLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationErrorLimitInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("Translator.errorLimit")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess", 
					"Translator.errorLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class GlobalRateLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationRateLimitInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("Translator.rateLimit")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess", 
					"Translator.rateLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class TranslationCache extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationTranslationCacheInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess", 
					"Translator.translatorCacheSize", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
}
