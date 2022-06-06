package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class AmazonSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class AccessKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationAmazonTranslateAccessKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonAccessKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateAccessKeySuccess", 
					new String[] {"Translator.amazonAccessKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class Region extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationAmazonTranslateRegionInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonRegion")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateRegionSuccess", 
					new String[] {"Translator.amazonRegion", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class SecretKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationAmazonTranslateSecretKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateSecretKeySuccess", 
					new String[] {"Translator.amazonSecretKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	
	
}
