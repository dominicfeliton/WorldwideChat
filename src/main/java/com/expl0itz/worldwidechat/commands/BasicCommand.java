package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BasicCommand {

    public CommandSender sender;
    public Command command;
    public String label;
    public String[] args;

    public BasicCommand(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
    }

}