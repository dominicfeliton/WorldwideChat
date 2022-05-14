package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.inventory.configuration.MenuGui;

public class WWCConfiguration extends BasicCommand {

	public WWCConfiguration(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	@Override
	public boolean processCommand() {
		MenuGui.CONFIG_GUI_TAGS.GEN_SET.smartInv.open((Player) sender);
		return true;
	}

}
