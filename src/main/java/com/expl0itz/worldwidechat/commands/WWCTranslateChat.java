package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateChat extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwctc, worldwidechat.wwctc.otherplayers
	
	public WWCTranslateChat(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

}
