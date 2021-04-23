package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.configuration.ConfigurationGeneralSettingsGUI;

public class WWCConfiguration extends BasicCommand {

	public WWCConfiguration(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	public boolean processCommand() {
		Player currPlayer = Bukkit.getServer().getPlayer(sender.getName());
		ConfigurationGeneralSettingsGUI.generalSettings.open(currPlayer);
		return true;
	}

}
