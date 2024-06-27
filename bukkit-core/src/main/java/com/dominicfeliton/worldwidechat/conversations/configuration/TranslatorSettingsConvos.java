package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

public class TranslatorSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = new WWCInventoryManager();
	
	public static class CharacterLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationCharacterLimitInput", "" + main.getMessageCharLimit(), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > 0 && input.intValue() <= 255, context, "wwcConfigConversationCharacterLimitSuccess",
					"Translator.messageCharLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class ErrorLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationErrorLimitInput", "" + main.getErrorLimit(), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess",
					"Translator.errorLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class GlobalRateLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationRateLimitInput", "" + main.getGlobalRateLimit(), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess",
					"Translator.rateLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class TranslationCache extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationTranslationCacheInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess",
					"Translator.translatorCacheSize", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}

	public static class IgnoreErrors extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationIgnoreErrorsInput", (main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore") != null ? main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore").toString() : "empty"), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			if (input.equalsIgnoreCase("clear")) {
				Player currPlayer = ((Player) context.getForWhom());
				main.getConfigManager().getMainConfig().set("Translator.errorsToIgnore", new String[0]);
				final TextComponent clearChange = Component.text()
								.content(refs.getMsg("wwcConfigConversationIgnoreErrorsCleared", currPlayer))
								.color(NamedTextColor.YELLOW)
						.build();
				refs.sendMsg(currPlayer, clearChange);
				return this;
			} else {
				return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationIgnoreErrorsSuccess",
						new String[] {"Translator.errorsToIgnore"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.TRANS_SET.smartInv);
			}
		}
	}
}
