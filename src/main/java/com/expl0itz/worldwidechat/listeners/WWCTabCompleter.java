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
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.SupportedLanguageObject;

public class WWCTabCompleter implements TabCompleter {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// Init out list
		List<String> out = new ArrayList<String>();
		
		if (command.getName().equals("wwct")) {
			if (args.length == 1 || args.length == 2 || args.length == 3) {
				if (args.length == 1) {
					if (main.getActiveTranslator(((Player)sender).getUniqueId().toString()) != null) {
						out.add("stop");
					}
					for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
						out.add(eaPlayer.getName());
					}
				}
				for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
					out.add(eaObj.getLangName());
					out.add(eaObj.getLangCode());
				}
			}
		} else if (command.getName().equals("wwcg")) {
			if (args.length == 1 || args.length == 2) {
				if (args.length == 1 && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") != null) {
					out.add("stop");
				}
				for (SupportedLanguageObject eaObj : main.getSupportedTranslatorLanguages()) {
					out.add(eaObj.getLangName());
					out.add(eaObj.getLangCode());
				}
			}
		} else if (command.getName().equals("wwcts") || command.getName().equals("wwctb")) {
			if (args.length == 1) {
				synchronized (main.getActiveTranslators()) {
					for (ActiveTranslator eaTranslator : main.getActiveTranslators()) {
						out.add(Bukkit.getPlayer(UUID.fromString(eaTranslator.getUUID())).getName());
					}
				}
			}
		} else if (command.getName().equals("wwcs")) {
			if (args.length == 1) {
				for (Player eaPlayer : Bukkit.getServer().getOnlinePlayers()) {
					out.add(eaPlayer.getName());
				}
			}
		} else if (command.getName().equals("wwcrl")) {
			if (args.length == 1 || args.length == 2) {
				if (args.length == 1) {
					synchronized (main.getActiveTranslators()) {
						for (ActiveTranslator eaTranslator : main.getActiveTranslators()) {
							out.add(Bukkit.getPlayer(UUID.fromString(eaTranslator.getUUID())).getName());
						}
					}
				}
				out.add("0");
				out.add("3");
				out.add("5");
				out.add("10");
			}
		}
		
		return out;
	}

}
