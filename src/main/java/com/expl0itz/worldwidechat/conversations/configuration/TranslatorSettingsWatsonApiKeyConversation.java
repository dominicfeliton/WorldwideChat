package com.expl0itz.worldwidechat.conversations.configuration;

import java.io.IOException;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.configuration.ConfigurationEachTranslatorSettingsGUI;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class TranslatorSettingsWatsonApiKeyConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.getInstance();

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationWatsonAPIKeyInput", new String[] {main.getConfigManager().getMainConfig().getString("Translator.watsonAPIKey")});
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if (!input.equals("0")) {
			main.getConfigManager().getMainConfig().set("Translator.watsonAPIKey", input);
			main.getConfigManager().getMainConfig().set("Translator.useWatsonTranslate", false);
			try {
				main.getConfigManager().getMainConfig().save(main.getConfigManager().getConfigFile());
				main.addPlayerUsingConfigurationGUI((Player) context.getForWhom());
				final TextComponent successfulChange = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcConfigConversationWatsonAPIKeySuccess"))
								.color(NamedTextColor.GREEN))
						.build();
				CommonDefinitions.sendMessage((Player)context.getForWhom(), successfulChange);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ConfigurationEachTranslatorSettingsGUI.getCurrentTranslatorSettings("Watson")
				.open((Player) context.getForWhom());
		return END_OF_CONVERSATION;
	}

}