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

public class WWCTranslateInGameObjects extends BasicCommand {
	
	public WWCTranslateInGameObjects(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	private WorldwideChat main = WorldwideChat.getInstance();
	
	/* Process command */
	public boolean processCommand() {
		/* Check args */
		if (args.length > 1) {
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctInvalidArgs", new String[0]))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalidArgs);
		}
		
		/* If no args are provided */
		if (args.length == 0 || (args.length == 1 && args[0].equals(sender.getName()))) {
			if (main.getActiveTranslator(((Player)sender).getUniqueId().toString()) != null) {
				return toggleStatus((Player)sender);
			} else {
				// Player is not an active translator
				final TextComponent notATranslator = Component.text()
						.append(Component.text().content(
								CommonDefinitions.getMessage("wwctbNotATranslator", new String[0]))
								.color(NamedTextColor.RED))
						.build();
				CommonDefinitions.sendMessage(sender, notATranslator);
				return true;
			}
		}
		
		/* If there is an argument (another player) */
		if (args.length == 1) {
			if (Bukkit.getServer().getPlayer(args[0]) != null && main.getActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString()) != null) {
				if (this instanceof WWCTranslateBook) {
					if (sender.hasPermission("worldwidechat.wwctb.otherplayers")) {
						return toggleStatus(Bukkit.getPlayer(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwctb.otherplayers");
					}
				} else if (this instanceof WWCTranslateItem) {
					if (sender.hasPermission("worldwidechat.wwcti.otherplayers")) {
						return toggleStatus(Bukkit.getPlayer(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwcti.otherplayers");
					}
				} else if (this instanceof WWCTranslateSign) {
					if (sender.hasPermission("worldwidechat.wwcts.otherplayers")) {
						return toggleStatus(Bukkit.getPlayer(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwcts.otherplayers");
					}
				}
			} else {
				// If target is not a string or active translator:
				final TextComponent notAPlayer = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwctbPlayerNotFound", new String[] {args[0]}))
								.color(NamedTextColor.RED))
						.build();
				CommonDefinitions.sendMessage(sender, notAPlayer);
			}
		}
		return false;
	}

	/* Toggle Each Class's Status */
	private boolean toggleStatus(Player inPlayer) {
		ActiveTranslator currentTranslator = main
				.getActiveTranslator((inPlayer.getUniqueId().toString()));
		/* If we are book translation... */
		if (this instanceof WWCTranslateBook) {
			/* Enable book translation for sender! */
			if (inPlayer.getName().equals(sender.getName())) {
				currentTranslator.setTranslatingBook(!currentTranslator.getTranslatingBook());
				if (currentTranslator.getTranslatingBook()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				}
			/* Enable book translation for target! */
			} else {
				currentTranslator.setTranslatingBook(!currentTranslator.getTranslatingBook());
				if (currentTranslator.getTranslatingBook()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOnTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOffTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctbOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				}
			}
			return true;
		} else if (this instanceof WWCTranslateSign) {
			/* Enable sign translation for sender! */
			if (inPlayer.getName().equals(sender.getName())) {
				currentTranslator.setTranslatingSign(!currentTranslator.getTranslatingSign());
				if (currentTranslator.getTranslatingSign()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				}
			/* Enable sign translation for target! */
			} else {
				currentTranslator.setTranslatingSign(!currentTranslator.getTranslatingSign());
				if (currentTranslator.getTranslatingSign()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOnTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOffTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctsOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				}
			}
			return true;
		} else if (this instanceof WWCTranslateItem) {
			/* Enable item translation for sender! */
			if (inPlayer.getName().equals(sender.getName())) {
				currentTranslator.setTranslatingItem(!currentTranslator.getTranslatingItem());
				if (currentTranslator.getTranslatingItem()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslation);
				}
			/* Enable item translation for target! */
			} else {
				currentTranslator.setTranslatingItem(!currentTranslator.getTranslatingItem());
				if (currentTranslator.getTranslatingItem()) {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOnTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOnSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				} else {
					final TextComponent toggleTranslation = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOffTarget", new String[] {args[0]}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage((Player)sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctiOffSender", new String[0]))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(inPlayer, toggleTranslationTarget);
				}
			}
			return true;
		}
		return false;
	}
	
	private void badPermsMessage(String correctPerm) {
		final TextComponent badPerms = Component.text() // Bad perms
				.append(Component.text()
						.content(CommonDefinitions.getMessage("wwcBadPerms", new String[0]))
						.color(NamedTextColor.RED))
				.append(Component.text().content(" (" + correctPerm + ")")
						.color(NamedTextColor.LIGHT_PURPLE))
				.build();
		CommonDefinitions.sendMessage(sender, badPerms);
	}
}
