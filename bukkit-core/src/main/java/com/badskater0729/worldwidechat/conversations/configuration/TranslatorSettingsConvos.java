package com.badskater0729.worldwidechat.conversations.configuration;

import com.badskater0729.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.md_5.bungee.api.ChatColor;

import com.badskater0729.worldwidechat.util.CommonRefs;

public class TranslatorSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class CharacterLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationCharacterLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.messageCharLimit"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationCharacterLimitSuccess",
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
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationErrorLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.errorLimit"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess",
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
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationRateLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.rateLimit"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess",
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
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess",
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
				return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationIgnoreErrorsSuccess",
						new String[] {"Translator.errorsToIgnore"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.TRANS_SET.smartInv);
			}
		}
	}
}
