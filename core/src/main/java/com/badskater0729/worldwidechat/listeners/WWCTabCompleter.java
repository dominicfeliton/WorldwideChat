package com.badskater0729.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.badskater0729.worldwidechat.WorldwideChat;
import com.badskater0729.worldwidechat.util.SupportedLang;

public class WWCTabCompleter implements TabCompleter {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// Init out list
		List<String> out = new ArrayList<String>();
		String commandName = command.getName();

		/* Commands: /wwct */
		if (commandName.equals("wwct") && args.length > 0 && args.length < 4) {
			boolean prevEmptyArg = args[args.length - 1].isEmpty();
			switch (args.length) {
			case 1:
				// Add stop for sender
				if (main.isActiveTranslator((Player) sender) &&
						(prevEmptyArg || "stop".startsWith(args[args.length - 1]))) {
					out.add("stop");
				}
				
				// This argument could be another player
				// Do not add other players if sender does not have permissions
				for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
					String name = eaPlayer.getName();
					if ((prevEmptyArg || name.startsWith(args[args.length - 1]))
							&& (sender.hasPermission("worldwidechat.wwct.otherplayers") || name.equals(sender.getName()))) {
						out.add(eaPlayer.getName());
					}
				}
				
				// This argument could be an input or outputLang
				// Add input and output languages
				// ** Only add if the previous argument is empty OR the user is typing this suggestion
				for (SupportedLang eaLang : main.getSupportedInputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
			    break;
			case 2:
				// Do not say anything if first arg is stop
				if (args[0].equalsIgnoreCase("stop")) {
					break;
				}

				// Add stop for target player
				// Do not add stop if user does not have permission
				Player possiblePlayer = Bukkit.getPlayerExact(args[0]);
				if (main.isActiveTranslator(possiblePlayer) && (sender.hasPermission("worldwidechat.wwct.otherplayers") || args[0].equalsIgnoreCase(sender.getName()))
						&& (prevEmptyArg || "stop".startsWith(args[args.length - 1]))) {
					out.add("stop");
				}
				
				// This argument could be an input or outputLang
				// Add input and output languages
				// ** Only add if the previous argument is empty OR the user is typing this suggestion
				for (SupportedLang eaLang : main.getSupportedInputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				break;
			case 3:
				// Don't suggest anything if first arg is not a player...
				if (Bukkit.getPlayerExact(args[0]) == null) {
					break;
				}
				
				// The third argument can only be outLang
				// Therefore, simply add all possible output languages
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				break;
			}
		
		/* Commands: /wwcg */
		} else if (commandName.equals("wwcg") && args.length > 0 && args.length < 3) {
			boolean prevEmptyArg = args[args.length - 1].isEmpty();
			switch (args.length) {
			case 1:
				// Add "stop" if global translate is active
				if (main.isActiveTranslator("GLOBAL-TRANSLATE-ENABLED") && 
						(prevEmptyArg || "stop".startsWith(args[args.length - 1]))) {
					out.add("stop");
				}
				
				// This argument could be an input or outputLang
				// Add input and output languages
				// ** Only add if the previous argument is empty OR the user is typing this suggestion
				for (SupportedLang eaLang : main.getSupportedInputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				break;
			case 2:
				// Do not say anything if first arg is stop
				if (args[0].equalsIgnoreCase("stop")) {
					break;
				}

				// This argument can only be an outLang
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
					}
				}
				break;
			}
		
		/* Commands: /wwcts, /wwctb, /wwcti, /wwcte, /wwctci, /wwctco */
		} else if ((commandName.equals("wwcts") || commandName.equals("wwctb")
				|| commandName.equals("wwcti") || commandName.equals("wwcte")
				|| commandName.equals("wwctci") || commandName.equals("wwctco")) && args.length == 1 && sender.hasPermission("worldwidechat.wwct.otherplayers")) {
			if (args[0].isEmpty()) {
				for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
					if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
							&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
					}
				}
			} else {
				for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
					if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
							&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null
							&& (Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName()).toLowerCase()
									.startsWith(args[0].toLowerCase())) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
					}
				}
			}
		
		/* Commands: /wwcs */
		} else if (commandName.equals("wwcs") && args.length == 1) {
			if (args[0].isEmpty()) {
				for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
					out.add(eaPlayer.getName());
				}
			} else {
				for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
					if (eaPlayer.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						out.add(eaPlayer.getName());
					}
				}
			}
		
		/* Commands: /wwctrl */
		} else if (commandName.equals("wwctrl") && args.length > 0 && args.length < 3) {
			if (args[args.length - 1].isEmpty() && args.length == 1) {
				for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
					if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
							&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
					}
				}
			} else if (args.length == 1) {
				for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
					if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
							&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null
							&& (Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName())
									.toLowerCase().startsWith(args[0].toLowerCase())) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
					}
				}
			}
			if (args.length == 1 || (args.length == 2 && !StringUtils.isNumeric(args[0]) && Bukkit.getPlayerExact(args[0]) != null
					&& main.isActiveTranslator(Bukkit.getPlayerExact(args[0]).getUniqueId()))) {
				out.add("0");
				out.add("3");
				out.add("5");
				out.add("10");
			}
		}
		return out;
	}

}
