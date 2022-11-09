package com.badskater0729.worldwidechat.conversations.wwctranslategui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.commands.WWCTranslateRateLimit;
import com.badskater0729.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.badskater0729.worldwidechat.util.ActiveTranslator;
import com.badskater0729.worldwidechat.util.CommonDefinitions;

import net.md_5.bungee.api.ChatColor;

public class PersonalRateLimitConvo extends NumericPrompt {

	private ActiveTranslator currTranslator;

	public PersonalRateLimitConvo(ActiveTranslator inTranslator) {
		currTranslator = inTranslator;
	}

	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player) context.getForWhom()).closeInventory();
		return ChatColor.AQUA + CommonDefinitions.getMessage("wwctGUIConversationRateLimit", new String[] {currTranslator.getRateLimit() + ""});
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		WWCTranslateRateLimit rateCommand;
		if (input.intValue() > 0) { // Enable rate limit
			rateCommand = new WWCTranslateRateLimit(((CommandSender) context.getForWhom()), null,
					null, new String[] {Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName(), input.intValue() + ""});
			rateCommand.processCommand();
		} else if (input.intValue() == 0) { // Disable rate limit
			rateCommand = new WWCTranslateRateLimit(((CommandSender) context.getForWhom()), null,
					null, new String[] {Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName()});
			rateCommand.processCommand();
		} // Go back
		WWCTranslateGuiMainMenu.getTranslateMainMenu(currTranslator.getUUID()).open((Player) context.getForWhom());
		return END_OF_CONVERSATION;
	}

}
