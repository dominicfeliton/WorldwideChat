package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateBook extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwctb, worldwidechat.wwctb.otherplayers
	
	public WWCTranslateBook(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
}
