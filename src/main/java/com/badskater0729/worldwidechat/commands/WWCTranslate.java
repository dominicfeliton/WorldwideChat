package com.badskater0729.worldwidechat.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.inventory.wwctranslategui.WWCTranslateGuiMainMenu;
import com.badskater0729.worldwidechat.util.ActiveTranslator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import static com.badskater0729.worldwidechat.util.CommonRefs.getMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.debugMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.sendNoConsoleChatMsg;
import static com.badskater0729.worldwidechat.util.CommonRefs.isSameTranslatorLang;
import static com.badskater0729.worldwidechat.util.CommonRefs.getFormattedValidLangCodes;
import static com.badskater0729.worldwidechat.util.CommonRefs.isSupportedTranslatorLang;

public class WWCTranslate extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;
	
	private boolean isGlobal = this instanceof WWCGlobal;
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;
	
	public WWCTranslate(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	/*
	 * Correct Syntax: /wwct <id-in> <id-out> EX for id: en, ar, cs, nl, etc. id
	 * MUST be valid, we will check with CommonRefs class
	 */

	@Override
	public boolean processCommand() {
		/* Sanitize args */
		if ((isGlobal && args.length > 2) || (!isGlobal && args.length > 3)) {
			// Too many args
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(getMsg("wwctInvalidArgs"))
							.color(NamedTextColor.RED))
					.build();
			sendMsg(sender, invalidArgs);
			return false;
		}

		/* GUI Checks */
		if (args.length == 0 && isGlobal && !isConsoleSender) {
			WWCTranslateGuiMainMenu.getTranslateMainMenu("GLOBAL-TRANSLATE-ENABLED").open((Player) sender);
			return true;
		} else if (!isConsoleSender) {
			if ((args.length == 0 && !isGlobal) || (args.length == 1 && !isGlobal && args[0].equalsIgnoreCase(sender.getName()))) { /* User wants to see their own translation session */
				WWCTranslateGuiMainMenu.getTranslateMainMenu(((Player)sender).getUniqueId().toString()).open((Player) sender);
				return true;
			} else if (args.length == 1 && !isGlobal
					&& main.getServer().getPlayerExact(args[0]) != null) {
				if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
					WWCTranslateGuiMainMenu
							.getTranslateMainMenu(main.getServer().getPlayerExact(args[0]).getUniqueId().toString()).open((Player) sender);
					return true;
				} else {
					final TextComponent badPerms = Component.text() // Bad perms
							.append(Component.text()
									.content(getMsg("wwcBadPerms"))
									.color(NamedTextColor.RED))
							.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(sender, badPerms);
					return false;
				}
			}
		}

		/* Existing translator checks */
		if (!isGlobal) {
			/* User wants to exit their own translation session. */
			if (!isConsoleSender && args.length > 0 && (Bukkit.getServer().getPlayerExact(args[0]) == null 
					|| (args[0] instanceof String && args[0].equalsIgnoreCase(sender.getName()))) && main.isActiveTranslator(((Player)sender))) {
				ActiveTranslator currTarget = main.getActiveTranslator(((Player)sender).getUniqueId().toString());
				main.removeActiveTranslator(currTarget);
				final TextComponent chatTranslationStopped = Component.text()
						.append(Component.text()
								.content(getMsg("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				sendMsg(sender, chatTranslationStopped);
				if ((args.length >= 1 && args[0].equalsIgnoreCase("Stop")) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			/* User wants to delete another player's translation session. */
			} else if (args.length > 0 && Bukkit.getServer().getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
				Player testPlayer = Bukkit.getServer().getPlayerExact(args[0]);
				main.removeActiveTranslator(main.getActiveTranslator(testPlayer.getUniqueId().toString()));
				final TextComponent chatTranslationStopped = Component.text()
						.append(Component.text()
								.content(getMsg("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				sendMsg(testPlayer, chatTranslationStopped);
				final TextComponent chatTranslationStoppedOtherPlayer = Component.text()
						.append(Component.text()
								.content(getMsg("wwctTranslationStoppedOtherPlayer", new String[] {args[0]}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				sendMsg(sender, chatTranslationStoppedOtherPlayer);
				if ((isConsoleSender && args.length == 1) || (args.length >= 2 && args[1] instanceof String && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			}
		} else if (args.length > 0 && isGlobal && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")) {
			main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
			final TextComponent chatTranslationStopped = Component.text()
					.append(Component.text()
							.content(getMsg("wwcgTranslationStopped"))
							.color(NamedTextColor.LIGHT_PURPLE))
					.build();
			for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
				sendMsg(eaPlayer, chatTranslationStopped);
			}
			sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), chatTranslationStopped);
			if (args.length >= 1 && args[0] instanceof String && args[0].equalsIgnoreCase("Stop")) {
				return true;
			}
		}

		/* NEW TRANSLATION SESSION: Player has given us one argument */
		if (args.length == 1) {
			/* If the sender is a player */
			if (!isConsoleSender) {
				return startNewTranslationSession(isGlobal ? "GLOBAL-TRANSLATE-ENABLED" : ((Player)sender).getName(), "None", args[0]);
			}
			/* If we are console... */
			if (isGlobal) {
				return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", "None", args[0]);
			}
			return sendNoConsoleChatMsg(sender);
		}
		
		/* Player has given us two arguments */
		if (args.length == 2) {
			if (!isGlobal) {
				/* If we are passing two languages (or attempting to pass two languages) */
				if (Bukkit.getPlayerExact(args[0]) == null) {
					if (!isConsoleSender) {
						return startNewTranslationSession(((Player)sender).getName(), args[0], args[1]);
					}
					return sendNoConsoleChatMsg(sender);
				/* If we are attempting to pass a player and an outLang */
				}
				return startNewTranslationSession(args[0], "None", args[1]);
			}
			return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", args[0], args[1]);
		}
		
		/* Player has given us three arguments */
		if (args.length == 3 && !isGlobal) {
			return startNewTranslationSession(args[0], args[1], args[2]);
		}
		return false;
	}
	
	private boolean startNewTranslationSession(String inName, String inLang, String outLang) {
		// Check if inLang/outLang are the same
		if (inLang.equalsIgnoreCase(outLang) || isSameTranslatorLang(inLang, outLang, "all")) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							getMsg("wwctSameLangError", new String[] {getFormattedValidLangCodes("in")}))
							.color(NamedTextColor.RED))
					.build();
			sendMsg(sender, sameLangError);
			return false;
		}
		/* NOTICE:
		 * Do not let users use None inputLang with Amazon Translate. 
		 * Remove this if we ever find a workaround for each. */
		// Check if valid inLang
		if ((!inLang.equalsIgnoreCase("None") && !isSupportedTranslatorLang(inLang, "in")) || 
				(inLang.equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							getMsg("wwctInvalidInputLangCode", new String[] {getFormattedValidLangCodes("in")}))
							.color(NamedTextColor.RED))
					.build();
			sendMsg(sender, sameLangError);
			return false;
		}
		// Check if valid outLang
		if (!isSupportedTranslatorLang(outLang, "out")) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							getMsg("wwctInvalidOutputLangCode", new String[] {getFormattedValidLangCodes("out")}))
							.color(NamedTextColor.RED))
					.build();
			sendMsg(sender, sameLangError);
			return false;
		}
		// Check if target is valid player (if not global)
		// Set UUID if valid, else exit
		String inUUID = "";
		Player targetPlayer = null;
		if (!isGlobal) {
			targetPlayer = Bukkit.getPlayerExact(inName);
			if (targetPlayer == null) {
				final TextComponent playerNotFound = Component.text() // Target player not found
						.append(Component.text()
								.content(getMsg("wwcPlayerNotFound", new String[] {inName}))
								.color(NamedTextColor.RED))
						.build();
				sendMsg(sender, playerNotFound);
				return false;
			}
			inUUID = targetPlayer.getUniqueId().toString();
		}
		
		/* Check if user is targetting themselves, which doesn't need this permission (or if we are console) */
		boolean targetIsSelf = !isConsoleSender ? inUUID.equals((((Player)sender)).getUniqueId().toString()) : false;
		if (!isGlobal && !targetIsSelf && !sender.hasPermission("worldwidechat.wwct.otherplayers")) {
			final TextComponent badPerms = Component.text()
					.append(Component.text()
							.content(getMsg("wwcBadPerms"))
							.color(NamedTextColor.RED))
					.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
							.color(NamedTextColor.LIGHT_PURPLE))
					.build();
			sendMsg(sender, badPerms);
			return false;
		}
		
		/* All input vars have been checked; create new ActiveTranslator */
		if (!isGlobal) {
			if (inLang.equalsIgnoreCase("None")) {
				if (targetIsSelf) {
					final TextComponent autoTranslate = Component.text()
							.append(Component.text()
									.content(getMsg("wwctAutoTranslateStart", new String[] {outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(sender, autoTranslate);
				} else {
					final TextComponent autoTranslateOtherPlayer = Component.text()
							.append(Component.text()
									.content(getMsg("wwctAutoTranslateStartOtherPlayer", new String[] {targetPlayer.getName(), outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(sender, autoTranslateOtherPlayer);
					final TextComponent autoTranslate = Component.text()
							.append(Component.text()
									.content(getMsg("wwctAutoTranslateStart", new String[] {outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(Bukkit.getPlayer(UUID.fromString(inUUID)), autoTranslate);
				}
			} else {
                if (targetIsSelf) {
                	final TextComponent langToLang = Component.text()
							.append(Component.text()
									.content(getMsg("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(sender, langToLang);
				} else {
					final TextComponent langToLangOtherPlayer = Component.text()
							.append(Component.text()
									.content(getMsg("wwctLangToLangStartOtherPlayer", new String[] {targetPlayer.getName(), inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(sender, langToLangOtherPlayer);
					final TextComponent langToLang = Component.text()
							.append(Component.text()
									.content(getMsg("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					sendMsg(Bukkit.getPlayer(UUID.fromString(inUUID)), langToLang);
				}
			}
		} else {
			if (inLang.equalsIgnoreCase("None")) {
				final TextComponent autoTranslate = Component.text()
						.append(Component.text()
								.content(getMsg("wwcgAutoTranslateStart", new String[] {outLang}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					sendMsg(eaPlayer, autoTranslate);
				}
				sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), autoTranslate);
			} else {
				final TextComponent langToLang = Component.text()
						.append(Component.text()
								.content(getMsg("wwcgLangToLangStart", new String[] {inLang, outLang}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					sendMsg(eaPlayer, langToLang);
				}
				sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), langToLang);
			}
		}
		ActiveTranslator newTranslator = new ActiveTranslator(inUUID, "None", outLang);
		if (!inLang.equalsIgnoreCase("None")) {
			newTranslator.setInLangCode(inLang);
		}
		main.addActiveTranslator(newTranslator);
		return true;
	}

}