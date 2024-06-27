package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

import net.md_5.bungee.api.ChatColor;

public class GoogleSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = new WWCInventoryManager();

	public static class ApiKey extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationGoogleTranslateAPIKeyInput", main.getConfigManager().getMainConfig().getString("Translator.googleTranslateAPIKey"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationGoogleTranslateAPIKeySuccess",
					new String[] {"Translator.googleTranslateAPIKey", "Translator.useGoogleTranslate"}, new Object[] {input, false}, CONFIG_GUI_TAGS.GOOGLE_TRANS_SET.smartInv);
		}
	}
	
}
