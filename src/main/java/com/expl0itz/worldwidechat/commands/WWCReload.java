package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

public class WWCReload extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();

	public WWCReload(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		main.reload(sender);
		return true;
	}
}