package com.dominicfeliton.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class WWCTranslateChatServer extends WWCTranslateInGameObjects {

    // Permission: worldwidechat.wwctcs, worldwidechat.wwctcs.otherplayers

    public WWCTranslateChatServer(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }
}
