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

import com.badskater0729.worldwidechat.util.CommonRefs;

public class WWCTranslate extends BasicCommand {

	private WorldwideChat main = WorldwideChat.instance;
	private CommonRefs refs = main.getServerFactory().getCommonRefs();
	
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
							.content(refs.getMsg("wwctInvalidArgs"))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, invalidArgs);
			return false;
		}

		/* GUI Checks */
		if (args.length == 0 && isGlobal && !isConsoleSender) {
			new WWCTranslateGuiMainMenu("GLOBAL-TRANSLATE-ENABLED").getTranslateMainMenu().open((Player) sender);
			return true;
		} else if (!isConsoleSender) {
			if ((args.length == 0 && !isGlobal) || (args.length == 1 && !isGlobal && args[0].equalsIgnoreCase(sender.getName()))) { /* User wants to see their own translation session */
				new WWCTranslateGuiMainMenu(((Player)sender).getUniqueId().toString()).getTranslateMainMenu().open((Player) sender);
				return true;
			} else if (args.length == 1 && !isGlobal
					&& main.getServer().getPlayerExact(args[0]) != null) {
				if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
					new WWCTranslateGuiMainMenu(main.getServer().getPlayerExact(args[0]).getUniqueId().toString())
							.getTranslateMainMenu().open((Player) sender);
					return true;
				} else {
					final TextComponent badPerms = Component.text() // Bad Perms
									.content(refs.getMsg("wwcBadPerms"))
									.color(NamedTextColor.RED)
							.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					refs.sendMsg(sender, badPerms);
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
								.content(refs.getMsg("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE)
						.build();
				refs.sendMsg(sender, chatTranslationStopped);
				if ((args.length >= 1 && args[0].equalsIgnoreCase("Stop")) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			/* User wants to delete another player's translation session. */
			} else if (args.length > 0 && Bukkit.getServer().getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]))) {
				Player testPlayer = Bukkit.getServer().getPlayerExact(args[0]);
				main.removeActiveTranslator(main.getActiveTranslator(testPlayer.getUniqueId().toString()));
				final TextComponent chatTranslationStopped = Component.text()
								.content(refs.getMsg("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE)
						.build();
				refs.sendMsg(testPlayer, chatTranslationStopped);
				final TextComponent chatTranslationStoppedOtherPlayer = Component.text()
								.content(refs.getMsg("wwctTranslationStoppedOtherPlayer", args[0]))
								.color(NamedTextColor.LIGHT_PURPLE)
						.build();
				refs.sendMsg(sender, chatTranslationStoppedOtherPlayer);
				if ((isConsoleSender && args.length == 1) || (args.length >= 2 && args[1] instanceof String && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			}
		} else if (args.length > 0 && isGlobal && main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED")) {
			main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
			final TextComponent chatTranslationStopped = Component.text()
							.content(refs.getMsg("wwcgTranslationStopped"))
							.color(NamedTextColor.LIGHT_PURPLE)
					.build();
			for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
				refs.sendMsg(eaPlayer, chatTranslationStopped);
			}
			refs.sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), chatTranslationStopped);
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
			return refs.sendNoConsoleChatMsg(sender);
		}
		
		/* Player has given us two arguments */
		if (args.length == 2) {
			if (!isGlobal) {
				/* If we are passing two languages (or attempting to pass two languages) */
				if (Bukkit.getPlayerExact(args[0]) == null) {
					if (!isConsoleSender) {
						return startNewTranslationSession(((Player)sender).getName(), args[0], args[1]);
					}
					return refs.sendNoConsoleChatMsg(sender);
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
		if (inLang.equalsIgnoreCase(outLang) || refs.isSameTranslatorLang(inLang, outLang, "all")) {
			final TextComponent sameLangError = Component.text()
							.content(refs.getMsg("wwctSameLangError", refs.getFormattedValidLangCodes("in")))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, sameLangError);
			return false;
		}
		/* NOTICE:
		 * Do not let users use None inputLang with Amazon Translate. 
		 * Remove this if we ever find a workaround for each. */
		// Check if valid inLang
		if ((!inLang.equalsIgnoreCase("None") && !refs.isSupportedTranslatorLang(inLang, "in")) || 
				(inLang.equalsIgnoreCase("None") && main.getTranslatorName().equalsIgnoreCase("Amazon Translate"))) {
			final TextComponent sameLangError = Component.text()
							.content(refs.getMsg("wwctInvalidInputLangCode", refs.getFormattedValidLangCodes("in")))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, sameLangError);
			return false;
		}
		// Check if valid outLang
		if (!refs.isSupportedTranslatorLang(outLang, "out")) {
			final TextComponent sameLangError = Component.text()
							.content(refs.getMsg("wwctInvalidOutputLangCode", refs.getFormattedValidLangCodes("out")))
							.color(NamedTextColor.RED)
					.build();
			refs.sendMsg(sender, sameLangError);
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
								.content(refs.getMsg("wwcPlayerNotFound", inName))
								.color(NamedTextColor.RED)
						.build();
				refs.sendMsg(sender, playerNotFound);
				return false;
			}
			inUUID = targetPlayer.getUniqueId().toString();
		} else {
			// If global, set UUID to reflect that
			inUUID = "GLOBAL-TRANSLATE-ENABLED";
		}
		
		/* Check if user is targetting themselves, which doesn't need this permission (or if we are console) */
		boolean targetIsSelf = !isConsoleSender ? inUUID.equals((((Player)sender)).getUniqueId().toString()) : false;
		if (!isGlobal && !targetIsSelf && !sender.hasPermission("worldwidechat.wwct.otherplayers")) {
			final TextComponent badPerms = Component.text()
							.content(refs.getMsg("wwcBadPerms"))
							.color(NamedTextColor.RED)
					.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
							.color(NamedTextColor.LIGHT_PURPLE))
					.build();
			refs.sendMsg(sender, badPerms);
			return false;
		}
		
		/* All input vars have been checked; create new ActiveTranslator */
		if (!isGlobal) {
			if (inLang.equalsIgnoreCase("None")) {
				if (targetIsSelf) {
					final TextComponent autoTranslate = Component.text()
									.content(refs.getMsg("wwctAutoTranslateStart", outLang))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(sender, autoTranslate);
				} else {
					final TextComponent autoTranslateOtherPlayer = Component.text()
									.content(refs.getMsg("wwctAutoTranslateStartOtherPlayer", new String[] {targetPlayer.getName(), outLang}))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(sender, autoTranslateOtherPlayer);
					final TextComponent autoTranslate = Component.text()
									.content(refs.getMsg("wwctAutoTranslateStart", outLang))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(Bukkit.getPlayer(UUID.fromString(inUUID)), autoTranslate);
				}
			} else {
                if (targetIsSelf) {
                	final TextComponent langToLang = Component.text()
									.content(refs.getMsg("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(sender, langToLang);
				} else {
					final TextComponent langToLangOtherPlayer = Component.text()
									.content(refs.getMsg("wwctLangToLangStartOtherPlayer", new String[] {targetPlayer.getName(), inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(sender, langToLangOtherPlayer);
					final TextComponent langToLang = Component.text()
									.content(refs.getMsg("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE)
							.build();
					refs.sendMsg(Bukkit.getPlayer(UUID.fromString(inUUID)), langToLang);
				}
			}
		} else {
			if (inLang.equalsIgnoreCase("None")) {
				final TextComponent autoTranslate = Component.text()
								.content(refs.getMsg("wwcgAutoTranslateStart", outLang))
								.color(NamedTextColor.LIGHT_PURPLE)
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					refs.sendMsg(eaPlayer, autoTranslate);
				}
				refs.sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), autoTranslate);
			} else {
				final TextComponent langToLang = Component.text()
								.content(refs.getMsg("wwcgLangToLangStart", new String[] {inLang, outLang}))
								.color(NamedTextColor.LIGHT_PURPLE)
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					refs.sendMsg(eaPlayer, langToLang);
				}
				refs.sendMsg(WorldwideChat.instance.getServer().getConsoleSender(), langToLang);
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