package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCGlobal extends BasicCommand {

	public WWCGlobal(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		if (!(args.length > 2)) {
			WWCTranslate initGlobal = new WWCTranslate(sender, command, label, args);
			return initGlobal.processCommand(true); // lets WWCTranslate know that we want a global user
		}
		// Too many args
		final TextComponent invalidArgs = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctInvalidArgs", new String[0]))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(sender, invalidArgs);
		return false;
	}

}