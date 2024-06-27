package com.dominicfeliton.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.ActiveTranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import com.dominicfeliton.worldwidechat.util.CommonRefs;

public class WWCTranslateInGameObjects extends BasicCommand {

	public WWCTranslateInGameObjects(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();

	/* Process command */
	@Override
	public boolean processCommand() {
		/* Check args */
		if (args.length > 1) {
			refs.sendFancyMsg("wwctInvalidArgs", "", "&c", sender);
		}

		/* If no args are provided */
		if (isConsoleSender && args.length == 0) {
			return refs.sendNoConsoleChatMsg(sender);
		}
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(sender.getName()))) {
			if (main.isActiveTranslator((Player)sender)) {
				return toggleStatus((Player)sender);
			}
			// Player is not an active translator
			refs.sendFancyMsg("wwctbNotATranslator", "", "&c", sender);
			return true;
		}

		/* If there is an argument (another player) */
		if (args.length == 1) {
			if (Bukkit.getServer().getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
				if (this instanceof WWCTranslateBook) {
					if (sender.hasPermission("worldwidechat.wwctb.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwctb.otherplayers", sender);
					}
				} else if (this instanceof WWCTranslateItem) {
					if (sender.hasPermission("worldwidechat.wwcti.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwcti.otherplayers", sender);
					}
				} else if (this instanceof WWCTranslateSign) {
					if (sender.hasPermission("worldwidechat.wwcts.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwcts.otherplayers", sender);
					}
				} else if (this instanceof WWCTranslateEntity) {
					if (sender.hasPermission("worldwidechat.wwcte.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwcte.otherplayers", sender);
					}
				} else if (this instanceof WWCTranslateChatOutgoing) {
					if (sender.hasPermission("worldwidechat.wwctco.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwctco.otherplayers", sender);
					}
				} else if (this instanceof WWCTranslateChatIncoming) {
					if (sender.hasPermission("worldwidechat.wwctci.otherplayers")) {
						return toggleStatus(Bukkit.getPlayerExact(args[0]));
					} else {
						refs.badPermsMessage("worldwidechat.wwctci.otherplayers", sender);
					}
				}
			} else {
				// If target is not a string or active translator:
				refs.sendFancyMsg("wwctbPlayerNotFound", "&6"+args[0], "&c", sender);
			}
		}
		return false;
	}

	/* Toggle Each Class's Status */
	private boolean toggleStatus(Player inPlayer) {
		ActiveTranslator currentTranslator = main
				.getActiveTranslator((inPlayer));
		/* If we are book translation... */
		if (this instanceof WWCTranslateBook) {
			currentTranslator.setTranslatingBook(!currentTranslator.getTranslatingBook());
			/* Toggle book translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingBook()) {
					refs.sendFancyMsg("wwctbOnSender", inPlayer);
					refs.debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctbOffSender", inPlayer);
					refs.debugMsg("Book translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle book translation for target! */
			} else {
				if (currentTranslator.getTranslatingBook()) {
					refs.sendFancyMsg("wwctbOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctbOnSender", inPlayer);
					refs.debugMsg("Book translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctbOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctbOffSender", inPlayer);
					refs.debugMsg("Book translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateSign) {
			currentTranslator.setTranslatingSign(!currentTranslator.getTranslatingSign());
			/* Toggle sign translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingSign()) {
					refs.sendFancyMsg("wwctsOnSender", inPlayer);
					refs.debugMsg("Sign translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctsOffSender", inPlayer);
					refs.debugMsg("Sign translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle sign translation for target! */
			} else {
				if (currentTranslator.getTranslatingSign()) {
					refs.sendFancyMsg("wwctsOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctsOnSender", inPlayer);
					refs.debugMsg("Sign translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctsOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctsOffSender", inPlayer);
					refs.debugMsg("Sign translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateItem) {
			currentTranslator.setTranslatingItem(!currentTranslator.getTranslatingItem());
			/* Toggle item translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingItem()) {
					refs.sendFancyMsg("wwctiOnSender", inPlayer);
					refs.debugMsg("Item translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctiOffSender", inPlayer);
					refs.debugMsg("Item translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle item translation for target! */
			} else {
				if (currentTranslator.getTranslatingItem()) {
					refs.sendFancyMsg("wwctiOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctiOnSender", inPlayer);
					refs.debugMsg("Item translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctiOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctiOffSender", inPlayer);
					refs.debugMsg("Item translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateEntity) {
			/* Toggle entity translation for sender! */
			currentTranslator.setTranslatingEntity(!currentTranslator.getTranslatingEntity());
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingEntity()) {
					refs.sendFancyMsg("wwcteOnSender", inPlayer);
					refs.debugMsg("Entity translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwcteOffSender", inPlayer);
					refs.debugMsg("Entity translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle entity translation for target! */
			} else {
				if (currentTranslator.getTranslatingEntity()) {
					refs.sendFancyMsg("wwcteOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwcteOnSender", inPlayer);
					refs.debugMsg("Entity translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwcteOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwcteOffSender", inPlayer);
					refs.debugMsg("Entity translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateChatOutgoing) {
			currentTranslator.setTranslatingChatOutgoing(!currentTranslator.getTranslatingChatOutgoing());
			/* Toggle chat translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingChatOutgoing()) {
					refs.sendFancyMsg("wwctcoOnSender", inPlayer);
					refs.debugMsg("Outgoing chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctcoOffSender", inPlayer);
					refs.debugMsg("Outgoing chat translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle chat translation for target! */
			} else {
				if (currentTranslator.getTranslatingChatOutgoing()) {
					refs.sendFancyMsg("wwctcoOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctcoOnSender", inPlayer);
					refs.debugMsg("Outgoing chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctcoOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctcoOffSender", inPlayer);
					refs.debugMsg("Outgoing chat translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		} else if (this instanceof WWCTranslateChatIncoming) {
			currentTranslator.setTranslatingChatIncoming(!currentTranslator.getTranslatingChatIncoming());
			/* Toggle chat translation for sender! */
			if (!isConsoleSender && inPlayer.getName().equalsIgnoreCase(sender.getName())) {
				if (currentTranslator.getTranslatingChatIncoming()) {
					refs.sendFancyMsg("wwctciOnSender", inPlayer);
					refs.debugMsg("Incoming chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctciOffSender", inPlayer);
					refs.debugMsg("Incoming chat translation disabled for " + inPlayer.getName() + ".");
				}
				/* Toggle chat translation for target! */
			} else {
				if (currentTranslator.getTranslatingChatIncoming()) {
					refs.sendFancyMsg("wwctciOnTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctciOnSender", inPlayer);
					refs.debugMsg("Incoming chat translation enabled for " + inPlayer.getName() + ".");
				} else {
					refs.sendFancyMsg("wwctciOffTarget", "&6"+args[0], "&d", sender);
					refs.sendFancyMsg("wwctciOffSender", inPlayer);
					refs.debugMsg("Incoming chat translation disabled for " + inPlayer.getName() + ".");
				}
			}
			return true;
		}
		return false;
	}
}