package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

public class BasicCommand {

	public CommandSender sender;
	public Command command;
	public String label;
	public String[] args;
	public WorldwideChat main;
	
	public BasicCommand(CommandSender sender, Command command, String label, String[] args, WorldwideChat main)
	{
		this.sender = sender;
		this.command = command;
		this.label = label;
		this.args = args;
		this.main = main;
	}
	
}
