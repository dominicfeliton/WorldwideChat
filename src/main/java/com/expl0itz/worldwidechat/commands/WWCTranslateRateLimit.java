package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCTranslateRateLimit extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();

	public WWCTranslateRateLimit(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	public boolean processCommand() {
		/* Sanitize args */
		if (args.length > 2) {
			// Not enough/too many args
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctInvalidArgs", new String[0]))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalidArgs);
		}

		/* Disable existing rate limit */
		if (args.length == 0 || (args.length == 1 && args[0].equals(sender.getName()))) {
			if (main.getActiveTranslator(((Player) sender).getUniqueId().toString()) != null) {
				return changeRateLimit((Player) sender, 0);
			} else {
				// If player is not translating:
				notATranslatorMessage((Player)sender);
			}
		}

		/* Set personal rate limit */
		if (args.length == 1 && args[0].matches("[0-9]+")) {
			if (main.getActiveTranslator(((Player) sender).getUniqueId().toString()) != null) {
				if (Integer.parseInt(args[0]) > 0) {
				    return changeRateLimit((Player)sender, Integer.parseInt(args[0]));
				} else {
					/* If rate limit is not valid/user wants to disable it */
					return changeRateLimit((Player)sender, 0);
				}
			} else {
				// If player is not translating:
				notATranslatorMessage((Player)sender);
			}
		}

		/* Disable another user's existing rate limit */
		if (args.length == 1 && args[0] instanceof String) {
			if (Bukkit.getPlayer(args[0]) != null
					&& main.getActiveTranslator((Bukkit.getPlayer(args[0])).getUniqueId().toString()) != null) {
				return changeRateLimit(Bukkit.getPlayer(args[0]), 0);
			} else {
				// Player not found
				notAPlayerMessage((Player)sender, args[0]);
			}
		}

		/* Set rate limit for another user */
		if (args.length == 2 && args[0] instanceof String && args[1].matches("[0-9]+")) {
			if (Bukkit.getServer().getPlayer(args[0]) != null
					&& main.getActiveTranslator(Bukkit.getPlayer(args[0]).getUniqueId().toString()) != null) {
				if (Integer.parseInt(args[1]) > 0) {
					/* If rate limit is valid */
					return changeRateLimit(Bukkit.getPlayer(args[0]), Integer.parseInt(args[1]));
				} else {
					/* If rate limit is not valid/user wants to disable it */
					return changeRateLimit(Bukkit.getPlayer(args[0]), 0);
				}
			} else {
				// If target is not a string, active translator, or is themselves:
				notAPlayerMessage((Player)sender, args[0]);
			}
		}
		return false;
	}
	
	private boolean changeRateLimit(Player inPlayer, int newLimit) {
		ActiveTranslator currTranslator = main.getActiveTranslator((inPlayer).getUniqueId().toString());
		if (inPlayer.getName().equals(sender.getName())) {
			/* If we are changing our own rate limit */
			if (newLimit > 0) {
				/* Enable rate limit */
				currTranslator.setRateLimit(newLimit);
				return enableRateLimitMessage(inPlayer, newLimit);
			} else {
				/* Disable rate limit */
				if (!(currTranslator.getRateLimit() > 0)) {
					/* Rate limit already disabled */
					final TextComponent rateLimitAlreadyOff = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctrlRateLimitAlreadyOffSender", new String[0]))
									.color(NamedTextColor.YELLOW))
							.build();
					CommonDefinitions.sendMessage(inPlayer, rateLimitAlreadyOff);
				} else {
					/* Disable rate limit */
					currTranslator.setRateLimit(0);
					return disableRateLimitMessage(inPlayer);
				}
			}
		} else {
			/* If we are changing somebody else's rate limit */
			if (newLimit > 0) {
				/* Enable rate limit */
				currTranslator.setRateLimit(newLimit);
				final TextComponent rateLimitSet = Component.text().append(Component.text()
								.content(CommonDefinitions.getMessage("wwctrlRateLimitSetTarget", new String[] {inPlayer.getName(), newLimit + ""}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(sender, rateLimitSet);
				return enableRateLimitMessage(inPlayer, newLimit);
			} else {
				/* Disable rate limit */
				if (!(currTranslator.getRateLimit() > 0)) {
					/* Rate limit already disabled */
					final TextComponent rateLimitAlreadyOff = Component.text()
							.append(Component.text().content(CommonDefinitions.getMessage("wwctrlRateLimitAlreadyOffTarget", new String[] {inPlayer.getName()}))
									.color(NamedTextColor.YELLOW))
							.build();
					CommonDefinitions.sendMessage(inPlayer, rateLimitAlreadyOff);
				} else {
					/* Disable rate limit */
					currTranslator.setRateLimit(0);
					final TextComponent rateLimitOffReceiver = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctrlRateLimitOffTarget", new String[] {inPlayer.getName()}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, rateLimitOffReceiver);
					return disableRateLimitMessage(inPlayer);
				}
			}
		}
		return false;
	}
	
	private boolean disableRateLimitMessage(Player inPlayer) {
		final TextComponent rateLimitOffReceiver = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctrlRateLimitOffSender", new String[0]))
						.color(NamedTextColor.LIGHT_PURPLE))
				.build();
		CommonDefinitions.sendMessage(inPlayer, rateLimitOffReceiver);
		return true;
	}
	
	public boolean enableRateLimitMessage(Player inPlayer, int limit) {
		final TextComponent rateLimitSet = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctrlRateLimitSetSender", new String[] {limit + ""}))
						.color(NamedTextColor.LIGHT_PURPLE))
				.build();
		CommonDefinitions.sendMessage(inPlayer, rateLimitSet);
		return true;
	}
	
	public void notATranslatorMessage(Player inPlayer) {
		final TextComponent notAPlayer = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctrlNotATranslator", new String[0]))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(inPlayer, notAPlayer);
	}
	
	public void notAPlayerMessage(Player inPlayer, String inName) {
		final TextComponent notAPlayer = Component.text()
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwctrlPlayerNotFound", new String[] {inName}))
						.color(NamedTextColor.RED))
				.build();
		CommonDefinitions.sendMessage(inPlayer, notAPlayer);
	}
	
}

