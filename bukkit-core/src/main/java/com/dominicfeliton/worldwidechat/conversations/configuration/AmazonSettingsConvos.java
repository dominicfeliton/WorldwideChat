package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

public class AmazonSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = new WWCInventoryManager();
	
	public static class AccessKey extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAmazonTranslateAccessKeyInput", main.getConfigManager().getMainConfig().getString("Translator.amazonAccessKey"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateAccessKeySuccess",
					new String[] {"Translator.amazonAccessKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class Region extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAmazonTranslateRegionInput", main.getConfigManager().getMainConfig().getString("Translator.amazonRegion"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateRegionSuccess",
					new String[] {"Translator.amazonRegion", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	public static class SecretKey extends StringPrompt {
		private CommonRefs refs = main.getServerFactory().getCommonRefs();
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationAmazonTranslateSecretKeyInput", main.getConfigManager().getMainConfig().getString("Translator.amazonSecretKey"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationAmazonTranslateSecretKeySuccess",
					new String[] {"Translator.amazonSecretKey", "Translator.useAmazonTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.AMAZON_TRANS_SET.smartInv);
		}
	}
	
	
	
}
