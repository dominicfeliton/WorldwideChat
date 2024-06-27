package com.dominicfeliton.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateChatOutgoing extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwctco, worldwidechat.wwctco.otherplayers
	
	public WWCTranslateChatOutgoing(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

}
