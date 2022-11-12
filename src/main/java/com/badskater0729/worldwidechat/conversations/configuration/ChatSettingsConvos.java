package com.badskater0729.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class ChatSettingsConvos {

	private static WorldwideChat main = WorldwideChat.instance;
	
	public static class ModifyOverrideText extends StringPrompt {
		private SmartInventory previousInventory;
		
		private String currentOverrideName;
		
		public ModifyOverrideText(SmartInventory previousInventory, String currentOverrideName) {
			this.previousInventory = previousInventory;
			this.currentOverrideName = currentOverrideName;
		}
		
		@Override
		public String getPromptText(ConversationContext context) {
			/* Close any open inventories */
			((Player) context.getForWhom()).closeInventory();
			return ChatColor.AQUA + CommonDefinitions.getMessage("wwcConfigConversationOverrideTextChange");
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String input) {
			if (!input.equals("0")) {
				main.getConfigManager().getMessagesConfig().set("Overrides." + currentOverrideName, input);
				main.addPlayerUsingConfigurationGUI(((Player) context.getForWhom()).getUniqueId());
				final TextComponent successfulChange = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcConfigConversationOverrideTextChangeSuccess"))
								.color(NamedTextColor.GREEN))
						.build();
				CommonDefinitions.sendMessage((Player)context.getForWhom(), successfulChange);
				main.getConfigManager().saveMessagesConfig(true);
			}
			previousInventory.open((Player)context.getForWhom());
			return END_OF_CONVERSATION;
		}
	}
	
}
