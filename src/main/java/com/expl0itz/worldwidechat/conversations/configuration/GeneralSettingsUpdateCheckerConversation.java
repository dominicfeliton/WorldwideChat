package com.expl0itz.worldwidechat.conversations.configuration;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.configuration.ConfigurationGeneralSettingsGUI;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class GeneralSettingsUpdateCheckerConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player)context.getForWhom()).closeInventory();
		return ChatColor.AQUA + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationUpdateCheckerInput").replace("%i", "" + main.getUpdateCheckerDelay());
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		if (input.intValue() > 10) {
			main.setUpdateCheckerDelay(input.intValue());
			main.getConfigManager().getMainConfig().set("General.updateCheckerDelay", input.intValue());
			try {
				main.addPlayerUsingConfigurationGUI((Player)context.getForWhom());
				final TextComponent successfulChange = Component.text()
		                .append(main.getPluginPrefix().asComponent())
		                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConversationUpdateCheckerSuccess")).color(NamedTextColor.GREEN))
		                .build();
		            Audience adventureSender = main.adventure().sender((CommandSender)context.getForWhom());
		        adventureSender.sendMessage(successfulChange);
				main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* Re-open ConfigurationInventoryGUI */
		ConfigurationGeneralSettingsGUI.generalSettings.open((Player)context.getForWhom());
		return END_OF_CONVERSATION;
	}

}
