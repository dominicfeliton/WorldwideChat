package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.PlayerRecord;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.inventory.wwcstatsgui.WWCStatsGUIMainMenu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class WWCStats extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

	public WWCStats(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		/* Sanitize args */
		if (args.length > 1) {
			// Not enough/too many args
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctInvalidArgs"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalidArgs);
		}

		/* Get Sender Stats */
		if (args.length == 0) {
			if (isConsoleSender) {
				return noRecordsMessage("Console");
			}
			return translatorMessage((Player)sender);
		}

		/* Get Target Stats */
		if (args.length == 1) {
			if (Bukkit.getServer().getPlayerExact(args[0]) != null) {
				return translatorMessage(Bukkit.getServer().getPlayerExact(args[0]));
			}
			// Target player not found
			final TextComponent playerNotFound = Component.text()
					.append(Component
							.text().content(CommonDefinitions.getMessage("wwcPlayerNotFound", new String[] {args[0]}))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, playerNotFound);
		}
		return false;
	}
	
	private boolean translatorMessage(Player inPlayer) {
		if (!main.getPlayerRecord(inPlayer.getUniqueId().toString(), false).getUUID().equals("")) {
			// Is on record; continue
			if (sender instanceof Player) {
				WWCStatsGUIMainMenu.getStatsMainMenu(inPlayer.getUniqueId().toString()).open(inPlayer);
			} else {
				String isActiveTranslator = ChatColor.BOLD + "" + ChatColor.RED + "\u2717";
				PlayerRecord record = main
						.getPlayerRecord(inPlayer.getUniqueId().toString(), false);
				if (!main.getActiveTranslator(inPlayer.getUniqueId().toString()).getUUID().equals("")) {
					// Is currently an active translator
					isActiveTranslator = ChatColor.BOLD + "" + ChatColor.GREEN + "\u2713";
				}
				final TextComponent stats = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcsTitle", new String[] {inPlayer.getName()}))
								.color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
						.append(Component.text()
								.content("\n- " + CommonDefinitions.getMessage("wwcsIsActiveTranslator", new String[] {isActiveTranslator}))
								.color(NamedTextColor.AQUA))
						.append(Component.text()
								.content("\n- " + CommonDefinitions.getMessage("wwcsAttemptedTranslations", new String[] {record.getAttemptedTranslations() + ""}))
								.color(NamedTextColor.AQUA))
						.append(Component.text()
								.content("\n- " + CommonDefinitions.getMessage("wwcsSuccessfulTranslations", new String[] {record.getSuccessfulTranslations() + ""}))
								.color(NamedTextColor.AQUA))
						.append(Component.text()
								.content("\n- " + CommonDefinitions.getMessage("wwcsLastTranslationTime", new String[] {record.getLastTranslationTime()}))
								.color(NamedTextColor.AQUA))
						.build();
				CommonDefinitions.sendMessage(sender, stats);
			}
			return true;
		} else {
			return noRecordsMessage(inPlayer.getName());
		}
	}
	
	private boolean noRecordsMessage(String name) {
		final TextComponent playerNotFound = Component.text() // No records found
				.append(Component
						.text().content(CommonDefinitions.getMessage("wwcsNotATranslator", new String[] {name}))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(sender, playerNotFound);
		return true;
	}

}
