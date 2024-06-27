package com.dominicfeliton.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateItem extends WWCTranslateInGameObjects {

	// Permission: worldwidechat.wwcti, worldwidechat.wwcti.otherplayers

	public WWCTranslateItem(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
}
