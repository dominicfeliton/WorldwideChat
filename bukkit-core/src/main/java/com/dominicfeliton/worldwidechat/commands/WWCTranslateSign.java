package com.dominicfeliton.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateSign extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwcts, worldwidechat.wwcts.otherplayers

	public WWCTranslateSign(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
}
