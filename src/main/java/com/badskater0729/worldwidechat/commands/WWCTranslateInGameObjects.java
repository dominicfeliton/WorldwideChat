package com.badskater0729.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.ActiveTranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendNoConsoleChatMsg;

public class WWCTranslateInGameObjects extends BasicCommand {
	
	public WWCTranslateInGameObjects(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;
	
	private WorldwideChat main = WorldwideChat.instance;
	
	/* Process command */
	@Override
	public boolean processCommand() {
		/* Check args */
		if (args.length > 1) {
			final TextComponent invalidArgs = Component.text()
							.content(getMsg("wwctInvalidArgs"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(sender, invalidArgs);
		}
		
		/* If no args are provided */
		if (isConsoleSender && args.length == 0) {
			return sendNoConsoleChatMsg(sender);
		}
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(sender.getName()))) {
			if (main.isActiveTranslator(((Player)sender))) {
				return toggleStatus((Player)sender);
			}
			// Player is not an active translator
			final TextComponent notATranslator = Component.text()
							.content(getMsg("wwctbNotATranslator"))
							.color(NamedTextColor.RED)
					.build();
			sendMsg(sender, notATranslator);
			return true;
		}
		
		/* If there is an argument (another player) */
		if (args.length == 1) {
			if (Bukkit.getServer().getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
				if (this instanceof WWCTranslateBook) {
					if (sender.hasPermission("worldwidechat.wwctb.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwctb.otherplayers");
					}
				} else if (this instanceof WWCTranslateItem) {
					if (sender.hasPermission("worldwidechat.wwcti.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwcti.otherplayers");
					}
				} else if (this instanceof WWCTranslateSign) {
					if (sender.hasPermission("worldwidechat.wwcts.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwcts.otherplayers");
					}
				} else if (this instanceof WWCTranslateEntity) {
					if (sender.hasPermission("worldwidechat.wwcte.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwcte.otherplayers");
					}
				} else if (this instanceof WWCTranslateChatOutgoing) {
					if (sender.hasPermission("worldwidechat.wwctco.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0])); 
					} else {
						badPermsMessage("worldwidechat.wwctco.otherplayers");
					}
				} else if (this instanceof WWCTranslateChatIncoming) {
					if (sender.hasPermission("worldwidechat.wwctci.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						badPermsMessage("worldwidechat.wwctci.otherplayers");
					}
				}
			} else {
				// If target is not a string or active translator:
				final TextComponent notAPlayer = Component.text()
								.content(getMsg("wwctbPlayerNotFound", args[0]))
								.color(NamedTextColor.RED)
						.build();
				sendMsg(sender, notAPlayer);
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
			currentTranslator.setTranslatingBook(!currentTranslator.getTranslatingBook());
			/* Toggle book translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingBook()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctbOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctbOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Book translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle book translation for target! */
			} else {
				if (currentTranslator.getTranslatingBook()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctbOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctbOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctbOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctbOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Book translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateSign) {
			currentTranslator.setTranslatingSign(!currentTranslator.getTranslatingSign());
			/* Toggle sign translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingSign()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctsOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctsOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Sign translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle sign translation for target! */
			} else {
				if (currentTranslator.getTranslatingSign()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctsOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctsOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctsOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctsOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Sign translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateItem) {
			currentTranslator.setTranslatingItem(!currentTranslator.getTranslatingItem());
			/* Toggle item translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingItem()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctiOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Item translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctiOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Item translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle item translation for target! */
			} else {
				if (currentTranslator.getTranslatingItem()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctiOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctiOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Item translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctiOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctiOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Item translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateEntity) {
			/* Toggle entity translation for sender! */
			currentTranslator.setTranslatingEntity(!currentTranslator.getTranslatingEntity());
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingEntity()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwcteOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Entity translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwcteOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Entity translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle entity translation for target! */
			} else {
				if (currentTranslator.getTranslatingEntity()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwcteOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwcteOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Entity translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwcteOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwcteOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Entity translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateChatOutgoing) {
			currentTranslator.setTranslatingChatOutgoing(!currentTranslator.getTranslatingChatOutgoing());
			/* Toggle chat translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingChatOutgoing()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctcoOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Outgoing chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctcoOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Outgoing chat translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle chat translation for target! */
			} else {
				if (currentTranslator.getTranslatingChatOutgoing()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctcoOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctcoOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Outgoing chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctcoOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctcoOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Outgoing chat translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateChatIncoming) {
			currentTranslator.setTranslatingChatIncoming(!currentTranslator.getTranslatingChatIncoming());
			/* Toggle chat translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingChatIncoming()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctciOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Incoming chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctciOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslation);
					debugMsg("Incoming chat translation disabled for " + inPlayer.getName() + ".");
				}
			/* Toggle chat translation for target! */
			} else {
				if (currentTranslator.getTranslatingChatIncoming()) {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctciOnTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctciOnSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Incoming chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					final TextComponent toggleTranslation = Component.text()
									.content(getMsg("wwctciOffTarget", args[0]))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(sender, toggleTranslation);
					final TextComponent toggleTranslationTarget = Component.text()
									.content(getMsg("wwctciOffSender"))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					sendMsg(inPlayer, toggleTranslationTarget);
					debugMsg("Incoming chat translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		}
		return false;
	}
	
	private void badPermsMessage(String correctPerm) {
		final TextComponent badPerms = Component.text() // Bad Perms
						.content(getMsg("wwcBadPerms"))
						.color(NamedTextColor.RED)
				.append(Component.text().content(" (" + correctPerm + ")")
						.color(NamedTextColor.LIGHT_PURPLE))
				.build();
		sendMsg(sender, badPerms);
	}
}
