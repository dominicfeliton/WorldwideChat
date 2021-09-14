package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateEntity extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwcte, worldwidechat.wwcte.otherplayers
	
	public WWCTranslateEntity(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

}
