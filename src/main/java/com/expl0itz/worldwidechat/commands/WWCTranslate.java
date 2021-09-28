package com.expl0itz.worldwidechat.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.inventory.wwctranslategui.WWCTranslateGUIMainMenu;
import com.expl0itz.worldwidechat.util.ActiveTranslator;
import com.expl0itz.worldwidechat.util.CommonDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCTranslate extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	private boolean isGlobal = this instanceof WWCGlobal;
	private boolean isConsoleSender = sender instanceof ConsoleCommandSender;
	
	public WWCTranslate(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}

	/*
	 * Correct Syntax: /wwct <id-in> <id-out> EX for id: en, ar, cs, nl, etc. id
	 * MUST be valid, we will check with CommonDefinitions class
	 */

	public boolean processCommand() {
		/* Sanitize args */
		if ((isGlobal && args.length > 2) || (!isGlobal && args.length > 3)) {
			// Too many args
			final TextComponent invalidArgs = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctInvalidArgs"))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, invalidArgs);
			return false;
		}

		/* GUI Checks */
		if (args.length == 0 && isGlobal && !isConsoleSender) {
			WWCTranslateGUIMainMenu.getTranslateMainMenu("GLOBAL-TRANSLATE-ENABLED").open((Player) sender);
			return true;
		} else if (!isConsoleSender) {
			if ((args.length == 0 && !isGlobal) || (args.length == 1 && !isGlobal && args[0].equalsIgnoreCase(sender.getName()))) { /* User wants to see their own translation session */
				WWCTranslateGUIMainMenu.getTranslateMainMenu(((Player)sender).getUniqueId().toString()).open((Player) sender);
				return true;
			} else if (args.length == 1 && !isGlobal
					&& main.getServer().getPlayerExact(args[0]) != null) {
				if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
					WWCTranslateGUIMainMenu
							.getTranslateMainMenu(main.getServer().getPlayerExact(args[0]).getUniqueId().toString()).open((Player) sender);
					return true;
				} else {
					final TextComponent badPerms = Component.text() // Bad perms
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwcBadPerms"))
									.color(NamedTextColor.RED))
							.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, badPerms);
					return false;
				}
			}
		}

		/* Existing translator checks */
		if (!isGlobal) {
			/* User wants to exit their own translation session. */
			if (!isConsoleSender && args.length > 0 && (Bukkit.getServer().getPlayerExact(args[0]) == null || (args[0] instanceof String && args[0].equalsIgnoreCase(sender.getName()))) && !main.getActiveTranslator(((Player)sender).getUniqueId().toString()).getUUID().equals("")) {
				ActiveTranslator currTarget = main.getActiveTranslator(((Player)sender).getUniqueId().toString());
				main.removeActiveTranslator(currTarget);
				final TextComponent chatTranslationStopped = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(sender, chatTranslationStopped);
				if ((args.length >= 1 && args[0].equalsIgnoreCase("Stop")) || (args.length >= 2 && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			/* User wants to delete another player's translation session. */
			} else if (args.length > 0 && Bukkit.getServer().getPlayerExact(args[0]) != null && !main.getActiveTranslator(Bukkit.getServer().getPlayerExact(args[0]).getUniqueId().toString()).getUUID().equals("")) {
				Player testPlayer = Bukkit.getServer().getPlayerExact(args[0]);
				main.removeActiveTranslator(main.getActiveTranslator(testPlayer.getUniqueId().toString()));
				final TextComponent chatTranslationStopped = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwctTranslationStopped"))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(testPlayer, chatTranslationStopped);
				final TextComponent chatTranslationStoppedOtherPlayer = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwctTranslationStoppedOtherPlayer", new String[] {args[0]}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				CommonDefinitions.sendMessage(sender, chatTranslationStoppedOtherPlayer);
				if ((isConsoleSender && args.length == 1) || (args.length >= 2 && args[1] instanceof String && args[1].equalsIgnoreCase("Stop"))) {
					return true;
				}
			}
		} else if (args.length > 0 && isGlobal && !main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")) {
			main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
			final TextComponent chatTranslationStopped = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcgTranslationStopped"))
							.color(NamedTextColor.LIGHT_PURPLE))
					.build();
			for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
				CommonDefinitions.sendMessage(eaPlayer, chatTranslationStopped);
			}
			if (args.length >= 1 && args[0] instanceof String && args[0].equalsIgnoreCase("Stop")) {
				return true;
			}
		}

		/* Player has given us one argument */
		if (args.length == 1) {
			/* If the sender is a player */
			if (!isConsoleSender) {
				return startNewTranslationSession(isGlobal ? "GLOBAL-TRANSLATE-ENABLED" : ((Player)sender).getUniqueId().toString(), "None", args[0]);
			}
			/* If we are console... */
			if (isGlobal) {
				return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", "None", args[0]);
			}
			final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwctCannotTranslateConsole", new String[0]))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, noConsoleChat);
			return false;
		}
		
		/* Player has given us two arguments */
		if (args.length == 2) {
			if (!isGlobal) {
				/* If we are passing two languages (or attempting to pass two languages) */
				if (Bukkit.getPlayerExact(args[0]) == null) {
					if (!isConsoleSender) {
						return startNewTranslationSession(((Player)sender).getUniqueId().toString(), args[0], args[1]);
					}
					final TextComponent noConsoleChat = Component.text() // Cannot translate console chat
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctCannotTranslateConsole", new String[0]))
									.color(NamedTextColor.RED))
							.build();
					CommonDefinitions.sendMessage(sender, noConsoleChat);
					return false;
				/* If we are attempting to pass a player and an outLang */
				}
				return startNewTranslationSession(Bukkit.getPlayer(args[0]).getUniqueId().toString(), "None", args[1]);
			}
			return startNewTranslationSession("GLOBAL-TRANSLATE-ENABLED", args[0], args[1]);
		}
		
		/* Player has given us three arguments */
		if (args.length == 3) {
			return startNewTranslationSession(Bukkit.getPlayer(args[0]).getUniqueId().toString(), args[1], args[2]);
		}
		return false;
	}
	
	private boolean startNewTranslationSession(String inUUID, String inLang, String outLang) {
		if (CommonDefinitions.isSameLang(inLang, outLang)) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							CommonDefinitions.getMessage("wwctSameLangError", new String[] {CommonDefinitions.getFormattedValidLangCodes()}))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, sameLangError);
			return false;
		}
		if ((!inLang.equalsIgnoreCase("None") && CommonDefinitions.getSupportedTranslatorLang(inLang).getLangCode().equals(""))) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							CommonDefinitions.getMessage("wwctInvalidInputLangCode", new String[] {CommonDefinitions.getFormattedValidLangCodes()}))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, sameLangError);
			return false;
		}
		if (CommonDefinitions.getSupportedTranslatorLang(outLang).getLangCode().equals("")) {
			final TextComponent sameLangError = Component.text()
					.append(Component.text().content(
							CommonDefinitions.getMessage("wwctInvalidOutputLangCode", new String[] {CommonDefinitions.getFormattedValidLangCodes()}))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, sameLangError);
			return false;
		}
		Player targetPlayer = null;
		if (!isGlobal) {
			targetPlayer = Bukkit.getPlayer(UUID.fromString(inUUID));
		}
		if (!isGlobal && targetPlayer == null) {
			final TextComponent playerNotFound = Component.text() // Target player not found
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcPlayerNotFound", new String[] {Bukkit.getPlayer(UUID.fromString(inUUID)).getName()}))
							.color(NamedTextColor.RED))
					.build();
			CommonDefinitions.sendMessage(sender, playerNotFound);
			return false;
		}
		/* Check if user is targetting themselves, which doesn't need this permission (or if we are console) */
		
		boolean targetIsSelf = !isConsoleSender ? inUUID.equals((((Player)sender)).getUniqueId().toString()) : false;
		if (!isGlobal && !targetIsSelf && !sender.hasPermission("worldwidechat.wwct.otherplayers")) {
			final TextComponent badPerms = Component.text()
					.append(Component.text()
							.content(CommonDefinitions.getMessage("wwcBadPerms"))
							.color(NamedTextColor.RED))
					.append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")")
							.color(NamedTextColor.LIGHT_PURPLE))
					.build();
			CommonDefinitions.sendMessage(sender, badPerms);
			return false;
		}
		
		/* All input vars have been checked; create new ActiveTranslator */
		if (!isGlobal) {
			if (inLang.equalsIgnoreCase("None")) {
				if (targetIsSelf) {
					final TextComponent autoTranslate = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctAutoTranslateStart", new String[] {outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, autoTranslate);
				} else {
					final TextComponent autoTranslateOtherPlayer = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctAutoTranslateStartOtherPlayer", new String[] {targetPlayer.getName(), outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, autoTranslateOtherPlayer);
					final TextComponent autoTranslate = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctAutoTranslateStart", new String[] {outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(Bukkit.getPlayer(UUID.fromString(inUUID)), autoTranslate);
				}
			} else {
                if (targetIsSelf) {
                	final TextComponent langToLang = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, langToLang);
				} else {
					final TextComponent langToLangOtherPlayer = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctLangToLangStartOtherPlayer", new String[] {targetPlayer.getName(), inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(sender, langToLangOtherPlayer);
					final TextComponent langToLang = Component.text()
							.append(Component.text()
									.content(CommonDefinitions.getMessage("wwctLangToLangStart", new String[] {inLang, outLang}))
									.color(NamedTextColor.LIGHT_PURPLE))
							.build();
					CommonDefinitions.sendMessage(Bukkit.getPlayer(UUID.fromString(inUUID)), langToLang);
				}
			}
		} else {
			if (inLang.equalsIgnoreCase("None")) {
				final TextComponent autoTranslate = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcgAutoTranslateStart", new String[] {outLang}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					CommonDefinitions.sendMessage(eaPlayer, autoTranslate);
				}
			} else {
				final TextComponent langToLang = Component.text()
						.append(Component.text()
								.content(CommonDefinitions.getMessage("wwcgLangToLangStart", new String[] {inLang, outLang}))
								.color(NamedTextColor.LIGHT_PURPLE))
						.build();
				for (Player eaPlayer : Bukkit.getOnlinePlayers()) {
					CommonDefinitions.sendMessage(eaPlayer, langToLang);
				}
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