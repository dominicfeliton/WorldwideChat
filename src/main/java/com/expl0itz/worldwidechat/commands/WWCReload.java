package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCReload extends BasicCommand{

	public WWCReload(CommandSender sender, Command command, String label, String[] args, WorldwideChat main) 
	{
		super(sender, command, label, args, main);
	}
	
	public boolean processCommand()
	{
		if (main.reloadWWC())
		{
			final TextComponent wwcrSuccess = Component.text()
				.append(main.getPluginPrefix().asComponent())
				.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcrSuccess")).color(NamedTextColor.GREEN))
				.build();
			sender.sendMessage(wwcrSuccess);
		}
		else
		{
		    final TextComponent wwcrFail = Component.text()
				    .append(main.getPluginPrefix().asComponent())
				    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcrFail")).color(NamedTextColor.RED))
				    .build();
		    sender.sendMessage(wwcrFail);
		}
	    return true;
	}
}
