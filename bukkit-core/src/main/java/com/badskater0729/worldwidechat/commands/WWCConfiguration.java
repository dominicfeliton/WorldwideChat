package com.badskater0729.worldwidechat.commands;

import com.badskater0729.worldwidechat.WorldwideChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.inventory.configuration.MenuGui;

public class WWCConfiguration extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;

	public WWCConfiguration(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	@Override
	public boolean processCommand() {
		MenuGui menuGui = new MenuGui((Player)sender, main.getTranslatorName());
		menuGui.genAllConfigUIs();
		MenuGui.CONFIG_GUI_TAGS.GEN_SET.smartInv.open((Player) sender);
		return true;
	}

}
