package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;

public class AmazonSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class AccessKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationAmazonTranslateAccessKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonAccessKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateAccessKeySuccess", 
					new String[] {"Translator.amazonAccessKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class Region extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationAmazonTranslateRegionInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonRegion")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateRegionSuccess", 
					new String[] {"Translator.amazonRegion", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class SecretKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationAmazonTranslateSecretKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey")});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateSecretKeySuccess", 
					new String[] {"Translator.amazonSecretKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	
	
}
