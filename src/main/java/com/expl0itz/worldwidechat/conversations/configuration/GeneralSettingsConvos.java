package com.expl0itz.worldwidechat.conversations.configuration;

import java.util.Arrays;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class FatalAsyncAbort extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationFatalAsyncAbort", new String[] {main.getConfigManager().getMainConfig().getString("General.fatalAsyncTaskTimeout")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
		
	}
	
	public static class Lang extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationLangInput", new String[] {main.getConfigManager().getMainConfig().getString("General.pluginLang"), Arrays.toString(CommonDefinitions.supportedPluginLangCodes)});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (!CommonDefinitions.getSupportedTranslatorLang(input).getLangCode().equals("") || input.equals("0")) {
				return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationlangSuccess", "General.pluginLang", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
			}
			final TextComponent badChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationLangInvalid"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage((Player)context.getForWhom(), badChange);
			return this;
		}
		
	}
	
	public static class Prefix extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA
					+ CommonDefinitions.getMessage("wwcConfigConversationPrefixInput", new String[] {LegacyComponentSerializer.legacyAmpersand().serialize(main.getPluginPrefix())});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationPrefixSuccess", "General.prefixName", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class SyncUserData extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationSyncUserDataDelayInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("General.syncUserDataDelay")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class UpdateChecker extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationUpdateCheckerInput", new String[] {main.getConfigManager().getMainConfig().getInt("General.updateCheckerDelay") + ""});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return CommonDefinitions.genericConfigConversation(input.intValue() > 10, context, "wwcConfigConversationUpdateCheckerSuccess", "General.updateCheckerDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
}
