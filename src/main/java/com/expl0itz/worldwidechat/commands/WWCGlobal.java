package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCGlobal extends BasicCommand {

    public WWCGlobal(CommandSender sender, Command command, String label, String[] args, WorldwideChat main) {
        super(sender, command, label, args, main);
    }

    public boolean processCommand() {
        if (!(args.length > 2))
        {
            WWCTranslate initGlobal = new WWCTranslate(sender, command, label, args, main);
            return initGlobal.processCommand(true); //lets WWCTranslate know that we want a global user
        }
      //Too many args
        final TextComponent invalidArgs = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
            .build();
        sender.sendMessage(invalidArgs);
        return false;
    }

}