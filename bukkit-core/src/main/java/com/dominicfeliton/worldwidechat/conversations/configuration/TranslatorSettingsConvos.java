package com.dominicfeliton.worldwidechat.conversations.configuration;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.inventory.WWCInventoryManager;
import com.dominicfeliton.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TranslatorSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;

	private static WWCInventoryManager invMan = main.getInventoryManager();
	
	public static class CharacterLimit extends NumericPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationCharacterLimitInput",
					"&6" + main.getMessageCharLimit(),
					"&b",
					currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > 0 && input.intValue() <= 255, context, "wwcConfigConversationCharacterLimitSuccess",
					"Translator.messageCharLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class ErrorLimit extends NumericPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationErrorLimitInput",
					"&6" + main.getErrorLimit(),
					"&b",
					currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > 0, context, "wwcConfigConversationErrorLimitSuccess",
					"Translator.errorLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class GlobalRateLimit extends NumericPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationRateLimitInput",
					"&6" + main.getGlobalRateLimit(),
					"&b",
					currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationRateLimitSuccess",
					"Translator.rateLimit", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}
	
	public static class TranslationCache extends NumericPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationTranslationCacheInput",
					"&6" + main.getConfigManager().getMainConfig().getInt("Translator.translatorCacheSize"),
					"&b",
					currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(@NotNull ConversationContext context, Number input) {
			return invMan.genericConfigConvo(input.intValue() > -1, context, "wwcConfigConversationTranslationCacheSuccess",
					"Translator.translatorCacheSize", input.intValue(), CONFIG_GUI_TAGS.TRANS_SET.smartInv);
		}
	}

	public static class IgnoreErrors extends StringPrompt {
		@Override
		public @NotNull String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return refs.getPlainMsg("wwcConfigConversationIgnoreErrorsInput",
					"&6"+(main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore") != null ? main.getConfigManager().getMainConfig().getList("Translator.errorsToIgnore").toString() : "empty"),
					"&b",
					currPlayer);
		}

		@Override
		public Prompt acceptInput(@NotNull ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			if (input.equalsIgnoreCase("clear")) {
				Player currPlayer = ((Player) context.getForWhom());
				main.getConfigManager().getMainConfig().set("Translator.errorsToIgnore", new String[0]);
				refs.sendMsg("wwcConfigConversationIgnoreErrorsCleared",
						"",
						"&e",
						currPlayer);
				return this;
			} else {
				return invMan.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationIgnoreErrorsSuccess",
						new String[] {"Translator.errorsToIgnore"}, new Object[] {input.split(",")}, CONFIG_GUI_TAGS.TRANS_SET.smartInv);
			}
		}
	}
}
