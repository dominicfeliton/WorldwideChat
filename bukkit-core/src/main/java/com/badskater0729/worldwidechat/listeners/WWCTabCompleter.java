package com.badskater0729.worldwidechat.listeners;

import java.util.*;

import com.badskater0729.worldwidechat.util.CommonRefs;
import com.badskater0729.worldwidechat.util.PlayerRecord;
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
		// TODO: Factor in permissions otherplayers for all commands
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
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length - 1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
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
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
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
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
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
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
					}
				}
				for (SupportedLang eaLang : main.getSupportedOutputLangs()) {
					String langName = eaLang.getLangName();
					String langCode = eaLang.getLangCode();
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
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
					String nativeName = eaLang.getNativeLangName();
					if ((prevEmptyArg || 
							langName.startsWith(args[args.length - 1]) ||
							langCode.startsWith(args[args.length - 1]) ||
							nativeName.startsWith(args[args.length -1]))) {
						out.add(langName);
						out.add(langCode);
						out.add(nativeName);
					}
				}
				break;
			}
		
		/* Commands: /wwcts, /wwctb, /wwcti, /wwcte, /wwctci, /wwctco */
		} else if ((commandName.equals("wwcts") || commandName.equals("wwctb")
				|| commandName.equals("wwcti") || commandName.equals("wwcte")
				|| commandName.equals("wwctci") || commandName.equals("wwctco")) && args.length == 1 && sender.hasPermission("worldwidechat.wwct.otherplayers")) {
			for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
				if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
						&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null
						&& (Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName()).toLowerCase()
						.startsWith(args[0].toLowerCase())) {
					out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
				}
			}
		
		/* Commands: /wwcs */
		} else if (commandName.equals("wwcs") && args.length == 1) {
			for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
				if (eaPlayer.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					out.add(eaPlayer.getName());
				}
			}
		
		/* Commands: /wwctrl */
		} else if (commandName.equals("wwctrl") && args.length > 0 && args.length < 3) {
			String[] suggestedSeconds = new String[]{"0", "3", "5", "10"};

			if (args[0].isEmpty()) {
				for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
					if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
							&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
					}
				}
				if (main.isActiveTranslator((Player) sender)) {
					out.addAll(Arrays.asList(suggestedSeconds));
				}
			} else if (args.length == 1) {
				if (StringUtils.isNumeric(args[0]) && main.isActiveTranslator((Player) sender)) {
					for (String eaStr : suggestedSeconds) {
						if (eaStr.startsWith(args[0])) {
							out.add(eaStr);
						}
					}
				} else {
					for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
						if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
								&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null
								&& (Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName())
								.toLowerCase().startsWith(args[0].toLowerCase())) {
							out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
						}
					}
				}
			} else {
				if (!StringUtils.isNumeric(args[0]) && Bukkit.getPlayerExact(args[0]) != null && main.isActiveTranslator(Bukkit.getPlayerExact(args[0]).getUniqueId())) {
					for (String eaStr : suggestedSeconds) {
						if (eaStr.startsWith(args[1])) {
							out.add(eaStr);
						}
					}
				}
			}
		/* Commands: /wwcl */
		} else if (commandName.equals("wwcl") && args.length > 0 && args.length < 3) {
			if (args.length == 1) {
				for (PlayerRecord eaRecord : main.getPlayerRecords().values()) {
					if (!eaRecord.getUUID().equals("GLOBAL-TRANSLATE-ENABLED")
					&& Bukkit.getPlayer(UUID.fromString(eaRecord.getUUID())) != null
					&& (Bukkit.getPlayer(UUID.fromString(eaRecord.getUUID())).getName())
							.toLowerCase().startsWith(args[0].toLowerCase())) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaRecord.getUUID())).getName());
					}
				}
				for (String supportedLangCode : CommonRefs.supportedPluginLangCodes) {
					if (supportedLangCode.startsWith(args[0])) {
						out.add(supportedLangCode);
					}
				}
				if ("stop".startsWith(args[0]) && main.isPlayerRecord((Player)sender) && !main.getPlayerRecord((Player)sender, false).getLocalizationCode().isEmpty()) {
					out.add("stop");
				}
			} else if (args.length == 2) {
				for (String supportedLangCode : CommonRefs.supportedPluginLangCodes) {
					if (supportedLangCode.startsWith(args[1]) && Bukkit.getPlayerExact(args[0]) != null) {
						out.add(supportedLangCode);
					}
				}
				if (Bukkit.getPlayerExact(args[0]) != null && "stop".startsWith(args[1]) && main.isPlayerRecord(Bukkit.getPlayerExact(args[0])) && !main.getPlayerRecord(Bukkit.getPlayerExact(args[0]), false).getLocalizationCode().isEmpty()) {
					out.add("stop");
				}
			}
		/* Commands: /wwcd */
		} else if (commandName.equals("wwcd") && args.length > 0 && args.length < 3) {
			if (args.length == 1) {
				if ("cache".startsWith(args[0])) {
					out.add("cache");
				}
				if ("convert".startsWith(args[0])) {
					out.add("convert");
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("cache") && "clear".startsWith(args[1])) {
					out.add("clear");
				}
				if (args[0].equalsIgnoreCase("convert") && "yes".startsWith(args[1])) {
					out.add("yes");
				}
			}
		}
		return out;
	}

}
