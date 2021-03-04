package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

public class WWCGlobal extends BasicCommand {

    public WWCGlobal(CommandSender sender, Command command, String label, String[] args, WorldwideChat main) {
        super(sender, command, label, args, main);
    }

    public boolean processCommand() {
        WWCTranslate initGlobal = new WWCTranslate(sender, command, label, args, main);
        return initGlobal.processCommand(true); //lets WWCTranslate know that we want a global user
    }

}