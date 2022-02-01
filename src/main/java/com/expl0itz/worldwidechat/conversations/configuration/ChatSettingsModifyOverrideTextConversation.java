package com.expl0itz.worldwidechat.conversations.configuration;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import fr.minuskube.inv.SmartInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class ChatSettingsModifyOverrideTextConversation extends StringPrompt {

	private WorldwideChat main = WorldwideChat.instance;
	
	private SmartInventory previousInventory;
	
	private String currentOverrideName;
	
	public ChatSettingsModifyOverrideTextConversation(SmartInventory previousInventory, String currentOverrideName) {
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
