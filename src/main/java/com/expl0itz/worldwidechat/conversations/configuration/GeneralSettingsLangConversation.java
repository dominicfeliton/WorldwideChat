package com.expl0itz.worldwidechat.conversations.configuration;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.configuration.ConfigurationGeneralSettingsGUI;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsLangConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player)context.getForWhom()).closeInventory();
		return ChatColor.AQUA + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationLangInput").replace("%i", main.getPluginLang()).replace("%o", Arrays.toString(CommonDefinitions.supportedPluginLangCodes));
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		for (String eaStr : CommonDefinitions.supportedPluginLangCodes) {
			if (!input.equals("0")) {
				if (eaStr.equals(input)) {
					main.setPluginLang(input);
					main.getConfigManager().getMainConfig().set("General.pluginLang", input);
					try {
						main.addPlayerUsingConfigurationGUI((Player)context.getForWhom());
						final TextComponent successfulChange = Component.text()
				                .append(main.getPluginPrefix().asComponent())
				                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationLangSuccess")).color(NamedTextColor.GREEN))
				                .build();
				            Audience adventureSender = main.adventure().sender((CommandSender)context.getForWhom());
				        adventureSender.sendMessage(successfulChange);
						main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
					} catch (IOException e) {
						e.printStackTrace();
					}
			        /* Re-open ConfigurationInventoryGUI */
					ConfigurationGeneralSettingsGUI.generalSettings.open((Player)context.getForWhom());
					return END_OF_CONVERSATION;
				}
			} else {
				/* Re-open ConfigurationInventoryGUI */
				ConfigurationGeneralSettingsGUI.generalSettings.open((Player)context.getForWhom());
				return END_OF_CONVERSATION;
			}
		}
		final TextComponent badChange = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationLangInvalid")).color(NamedTextColor.RED))
                .build();
            Audience adventureSender = main.adventure().sender((CommandSender)context.getForWhom());
        adventureSender.sendMessage(badChange);
		return this;
	}

}
