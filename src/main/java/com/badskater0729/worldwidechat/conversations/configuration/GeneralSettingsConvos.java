package com.badskater0729.worldwidechat.conversations.configuration;

import java.util.Arrays;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.configuration.MenuGui.CONFIG_GUI_TAGS;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.genericConfigConvo;
import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

public class GeneralSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class FatalAsyncAbort extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationFatalAsyncAbort", new String[] {main.getConfigManager().getMainConfig().getString("General.fatalAsyncTaskTimeout")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
		
	}
	
	public static class Lang extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationLangInput", new String[] {main.getConfigManager().getMainConfig().getString("General.pluginLang"), Arrays.toString(supportedPluginLangCodes)});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			for (String eaLangCode : supportedPluginLangCodes) {
				if (eaLangCode.equalsIgnoreCase(input) || input.equals("0")) {
					return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationLangSuccess", "General.pluginLang", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
				}
			}
			final TextComponent badChange = Component.text()
							.content(getMsg("wwcConfigConversationLangInvalid"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg((Player)context.getForWhom(), badChange);
			return this;
		}
		
	}
	
	public static class Prefix extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA
					+ getMsg("wwcConfigConversationPrefixInput", new String[] {LegacyComponentSerializer.legacyAmpersand().serialize(main.getPluginPrefix())});
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			return genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPrefixSuccess", "General.prefixName", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class SyncUserData extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationSyncUserDataDelayInput", new String[] {"" + main.getConfigManager().getMainConfig().getInt("General.syncUserDataDelay")});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class UpdateChecker extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + getMsg("wwcConfigConversationUpdateCheckerInput", new String[] {main.getConfigManager().getMainConfig().getInt("General.updateCheckerDelay") + ""});
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			return genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationUpdateCheckerSuccess", "General.updateCheckerDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
}
