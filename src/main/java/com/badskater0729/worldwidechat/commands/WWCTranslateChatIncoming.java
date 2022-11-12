package com.badskater0729.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateChatIncoming extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwctci, worldwidechat.wwctci.otherplayers
	
	public WWCTranslateChatIncoming(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
}
