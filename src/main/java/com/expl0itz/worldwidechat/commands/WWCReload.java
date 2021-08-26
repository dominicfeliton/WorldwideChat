package com.expl0itz.worldwidechat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCReload extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();

	public WWCReload(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		// Reload!
		try {
			main.reload();
			final TextComponent wwcrSuccess = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcrSuccess", new String[0]))
							.color(NamedTextColor.GREEN))
					.build();
			CommonDefinitions.sendMessage(sender, wwcrSuccess);
		} catch (Exception e) {
			final TextComponent wwcrFail = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcrFail", new String[0]))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, wwcrFail);
		}
		return true;
	}
}