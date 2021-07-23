package com.expl0itz.worldwidechat.conversations.wwctranslategui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.commands.WWCTranslateRateLimit;
import com.expl0itz.worldwidechat.inventory.wwctranslategui.WWCTranslateGUIMainMenu;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;

import net.md_5.bungee.api.ChatColor;

public class RateLimitConversation extends NumericPrompt {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private ActiveTranslator currTranslator;
	
	public RateLimitConversation(ActiveTranslator inTranslator) {
		currTranslator = inTranslator;
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		/* Close any open inventories */
		((Player)context.getForWhom()).closeInventory();
		return ChatColor.AQUA + main.getConfigManager().getMessagesConfig().getString("Messages.wwctGUIConversationRateLimit").replace("%i", currTranslator.getRateLimit() + "");
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
		if (input.intValue() > 0) { //Enable rate limit
			//Sender
			String[] args = {};
			if (((Player)context.getForWhom()).getUniqueId().toString().equals(currTranslator.getUUID())) {
				args = new String[]{input.intValue() + ""};
			} else { //Target Player
				args = new String[]{Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName(), input.intValue() + ""};
			}
			WWCTranslateRateLimit rateCommand = new WWCTranslateRateLimit(((CommandSender)context.getForWhom()), null, null, args);
			rateCommand.processCommand();
		} else if (input.intValue() == 0){ //Disable rate limit
			//Sender
			String[] args = {};
			if (!((Player)context.getForWhom()).getUniqueId().toString().equals(currTranslator.getUUID())) { //Target Player
				args = new String[]{Bukkit.getPlayer(UUID.fromString(currTranslator.getUUID())).getName()};
			}
			WWCTranslateRateLimit rateCommand = new WWCTranslateRateLimit(((CommandSender)context.getForWhom()), null, null, args);
			rateCommand.processCommand();
		} //Go back
		if (((Player)context.getForWhom()).getUniqueId().toString().equals(currTranslator.getUUID())) {
			WWCTranslateGUIMainMenu.getTranslateMainMenu(null).open((Player)context.getForWhom());
		} else {
			WWCTranslateGUIMainMenu.getTranslateMainMenu(currTranslator.getUUID()).open((Player)context.getForWhom());
		}
		return END_OF_CONVERSATION;
	}

}
