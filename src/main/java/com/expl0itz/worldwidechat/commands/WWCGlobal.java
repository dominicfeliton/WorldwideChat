package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCGlobal extends BasicCommand {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    public WWCGlobal(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    public boolean processCommand() {
        if (!(args.length > 2))
        {
            WWCTranslate initGlobal = new WWCTranslate(sender, command, label, args);
            return initGlobal.processCommand(true); //lets WWCTranslate know that we want a global user
        }
        //Too many args
        final TextComponent invalidArgs = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
            .build();
        Audience adventureSender = main.adventure().sender(sender);
        adventureSender.sendMessage(invalidArgs);
        return false;
    }

}