package com.expl0itz.worldwidechat.conversations.configuration;

import java.util.Arrays;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.GeneralSettingsGUI;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsLangConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationLangInput", new String[] {main.getConfigManager().getMainConfig().getString("General.pluginLang"), Arrays.toString(CommonDefinitions.supportedPluginLangCodes)});
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if (!CommonDefinitions.getSupportedTranslatorLang(input).getLangCode().equals("") || input.equals("0")) {
			return CommonDefinitions.genericConfigConversation(!input.equals("0"), context, "wwcConfigConversationlangSuccess", "General.pluginLang", input, GeneralSettingsGUI.generalSettings);
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
