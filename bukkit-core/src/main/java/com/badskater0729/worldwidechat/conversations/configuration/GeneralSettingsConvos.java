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

import static com.badskater0729.worldwidechat.util.CommonRefs.supportedPluginLangCodes;

import com.badskater0729.worldwidechat.util.CommonRefs;
public class GeneralSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class FatalAsyncAbort extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationFatalAsyncAbort", main.getConfigManager().getMainConfig().getString("General.fatalAsyncTaskTimeout"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() >= 7, context, "wwcConfigConversationFatalAsyncSuccess", "General.fatalAsyncTaskTimeout", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
		
	}
	
	public static class Lang extends StringPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationLangInput", new String[] {main.getConfigManager().getMainConfig().getString("General.pluginLang"), refs.getFormattedLangCodes("local")}, currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			if (refs.isSupportedLang(input, "local") || input.equals("0")) {
				input = !input.equals("0") ? refs.getSupportedLang(input, "local").getLangCode() : "0";
				return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationLangSuccess", "General.pluginLang", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
			}
			Player currPlayer = ((Player) context.getForWhom());
			final TextComponent badChange = Component.text()
							.content(refs.getMsg("wwcConfigConversationLangInvalid", currPlayer))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(currPlayer, badChange);
			return this;
		}
		
	}
	
	public static class Prefix extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA
					+ refs.getMsg("wwcConfigConversationPrefixInput", LegacyComponentSerializer.legacyAmpersand().serialize(main.getPluginPrefix()), currPlayer);
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(!input.equals("0"), context, "wwcConfigConversationPrefixSuccess", "General.prefixName", input, CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class SyncUserData extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationSyncUserDataDelayInput", "" + main.getConfigManager().getMainConfig().getInt("General.syncUserDataDelay"), currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationSyncUserDataDelaySuccess", "General.syncUserDataDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
	public static class UpdateChecker extends NumericPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			Player currPlayer = ((Player) context.getForWhom());
			currPlayer.closeInventory();
			return ChatColor.AQUA + refs.getMsg("wwcConfigConversationUpdateCheckerInput", main.getConfigManager().getMainConfig().getInt("General.updateCheckerDelay") + "", currPlayer);
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
			CommonRefs refs = main.getServerFactory().getCommonRefs();
			return refs.genericConfigConvo(input.intValue() > 10, context, "wwcConfigConversationUpdateCheckerSuccess", "General.updateCheckerDelay", input.intValue(), CONFIG_GUI_TAGS.GEN_SET.smartInv);
		}
	}
	
}
