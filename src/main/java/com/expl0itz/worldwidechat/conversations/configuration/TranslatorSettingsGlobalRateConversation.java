package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.ConfigurationTranslatorSettingsGUI;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class TranslatorSettingsGlobalRateConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.getInstance();

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationRateLimitInput", new String[] {"" + main.getRateLimit()});
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		if (input.intValue() > -1) {
			main.setRateLimit(input.intValue());
			main.getConfigManager().getMainConfig().set("Translator.rateLimit", input.intValue());
			main.addPlayerUsingConfigurationGUI((Player) context.getForWhom());
			final TextComponent successfulChange = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcConfigConversationRateLimitSuccess"))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage((Player)context.getForWhom(), successfulChange);
			main.getConfigManager().saveMainConfig(true);
		}
		ConfigurationTranslatorSettingsGUI.translatorSettings.open((Player) context.getForWhom());
		return END_OF_CONVERSATION;
	}

}
