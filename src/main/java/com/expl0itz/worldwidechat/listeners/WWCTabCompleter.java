package com.expl0itz.worldwidechat.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.CommonDefinitions;
import com.expl0itz.worldwidechat.util.SupportedLanguageObject;

public class WWCTabCompleter implements TabCompleter {

	private WorldwideChat main = WorldwideChat.instance;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// Init out list
		List<String> out = new ArrayList<String>();

		if (command.getName().equals("wwct") && args.length > 0 && args.length < 4) {
			if (args[args.length - 1].isEmpty()) {
				if (args.length == 1) {
					if (!main.getActiveTranslator(((Player) sender).getUniqueId().toString()).getUUID().equals("")) {
						out.add("stop");
					}
					for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
						out.add(eaPlayer.getName());
					}
				}
				if (args.length == 2 && Bukkit.getPlayerExact(args[0]) != null && !args[0].equalsIgnoreCase(sender.getName())
						&& !main.getActiveTranslator(Bukkit.getPlayerExact(args[0]).getUniqueId().toString()).getUUID().equals("")) {
					out.add("stop");
				}
				if (args.length == 1
						|| (args.length == 2 && (!CommonDefinitions.getSupportedTranslatorLang(args[0]).getLangCode().equals("")
										|| Bukkit.getPlayerExact(args[0]) != null))
						|| (args.length == 3 && !CommonDefinitions.getSupportedTranslatorLang(args[1]).getLangCode().equals("") && Bukkit.getPlayerExact(args[0]) != null)) {
					for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
						out.add(eaObj.getLangName());
						out.add(eaObj.getLangCode());
					}
				}
			} else {
				if (args.length == 1) {
					if (!main.getActiveTranslator(((Player) sender).getUniqueId().toString()).getUUID().equals("")) {
						if ("stop".startsWith(args[0].toLowerCase())) {
							out.add("stop");
						}
					}
					for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
						if (eaPlayer.getName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
							out.add(eaPlayer.getName());
						}
					}
				}
				if (args.length == 2) {
					if (Bukkit.getPlayerExact(args[0]) != null && !main.getActiveTranslator(Bukkit.getPlayerExact(args[0]).getUniqueId().toString()).getUUID().equals("")) {
						if ("stop".startsWith(args[args.length - 1].toLowerCase())) {
							out.add("stop");
						}
					}

				}
				if (args.length == 1
						|| (args.length == 2 && (!CommonDefinitions.getSupportedTranslatorLang(args[0]).getLangCode().equals("")
										|| Bukkit.getPlayerExact(args[0]) != null))
						|| (args.length == 3 && !CommonDefinitions.getSupportedTranslatorLang(args[1]).getLangCode().equals("") && Bukkit.getPlayerExact(args[0]) != null)) {
					for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
						if (eaObj.getLangName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
							out.add(eaObj.getLangName());
						}
						if (eaObj.getLangCode().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
							out.add(eaObj.getLangCode());
						}
					}
				}
			}
		} else if (command.getName().equals("wwcg") && args.length > 0 && args.length < 3) {
			if (args[args.length - 1].isEmpty()) {
				if (args.length == 1) {
					if (!main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")) {
						out.add("stop");
					}
				}
				if (args.length == 1
						|| (args.length == 2 && !CommonDefinitions.getSupportedTranslatorLang(args[0]).getLangCode().equals(""))) {
					for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
						out.add(eaObj.getLangName());
						out.add(eaObj.getLangCode());
					}
				}
			} else {
				if (args.length == 1) {
					if ("stop".startsWith(args[0].toLowerCase())
							&& !main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED").getUUID().equals("")) {
						out.add("stop");
					}
				}
				if (args.length == 1
						|| (args.length == 2 && !CommonDefinitions.getSupportedTranslatorLang(args[0]).getLangCode().equals(""))) {
					for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
						if (eaObj.getLangName().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
							out.add(eaObj.getLangName());
						}
						if (eaObj.getLangCode().toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
							out.add(eaObj.getLangCode());
						}
					}
				}
			}
		} else if ((command.getName().equals("wwcts") || command.getName().equals("wwctb")
				|| command.getName().equals("wwcti") || command.getName().equals("wwcte")
				|| command.getName().equals("wwctci") || command.getName().equals("wwctco")) && args.length == 1) {
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
		} else if (command.getName().equals("wwcs") && args.length == 1) {
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
		} else if (command.getName().equals("wwctrl") && args.length > 0 && args.length < 3) {
			if (args[args.length - 1].isEmpty()) {
				if (args.length == 1) {
					for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
						if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
								&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null) {
							out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
						}
					}
				}
			} else {
				if (args.length == 1) {
					for (String eaTranslatorUUID : main.getActiveTranslators().keySet()) {
						if (!eaTranslatorUUID.equals("GLOBAL-TRANSLATE-ENABLED")
								&& Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)) != null
								&& (Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName())
										.toLowerCase().startsWith(args[0].toLowerCase())) {
							out.add(Bukkit.getPlayer(UUID.fromString(eaTranslatorUUID)).getName());
						}
					}
				}
			}
			if (args.length == 1 || (args.length == 2 && !CommonDefinitions.isInteger(args[0]) && Bukkit.getPlayerExact(args[0]) != null
					&& !main.getActiveTranslator(Bukkit.getPlayerExact(args[0]).getUniqueId().toString()).getUUID().equals(""))) {
				out.add("0");
				out.add("3");
				out.add("5");
				out.add("10");
			}
		}
		return out;
	}

}
