package com.badskater0729.worldwidechat.conversations.configuration;

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

import static com.badskater0729.worldwidechat.util.CommonRefs.*;

public class TranslatorSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class CharacterLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationCharacterLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.messageCharLimit"));
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationCharacterLimitSuccess", 
					"Translator.messageCharLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class ErrorLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationErrorLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.errorLimit"));
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess", 
					"Translator.errorLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class GlobalRateLimit extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationRateLimitInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.rateLimit"));
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess", 
					"Translator.rateLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class TranslationCache extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationTranslationCacheInput", "" + main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize"));
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess", 
					"Translator.translatorCacheSize", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}

	public static class IgnoreErrors extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationIgnoreErrorsInput", main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore") != null ? main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore").toString() : "empty");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("clear")) {
				main.getConfigManager().getMainConfig().set("Translator.errorsToIgnore", new String[0]);
				final TextComponent clearChange = Component.text()
								.content(getMsg("wwcConfigConversationIgnoreErrorsCleared"))
								.color(NamedTextColor.YELLOW)
						.build();
				sendMsg((Player)context.getForWhom(), clearChange);
				return this;
			} else {
				return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationIgnoreErrorsSuccess",
						new String[] {"Translator.errorsToIgnore"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.TRANS_SET.smartInv);
			}
		}
	}
}
