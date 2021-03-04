package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.WWCActiveTranslator;
import com.expl0itz.worldwidechat.misc.WWCDefinitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCTranslate extends BasicCommand {

	public WWCTranslate(CommandSender sender, Command command, String label, String[] args, WorldwideChat main)
	{
		super(sender, command, label, args, main);
	}
	
	/*
	 * Correct Syntax: /wwct <id-in> <id-out>
	 * EX for id: en, ar, cs, nl, etc.
	 * id MUST be valid, is checked by Definitions class
	 */
	
	public boolean processCommand(boolean isGlobal)
	{
		/* Sanity checks */
		WWCActiveTranslator currTarget = main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString());
		if (currTarget instanceof WWCActiveTranslator)
		{
			main.removeActiveTranslator(currTarget);
			//TODO: remember previous lang code so another /wwct call will just use previous lang code
			final TextComponent chatTranslationStopped = Component.text()
					.append(main.getPluginPrefix().asComponent())
					.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
					.build();
			sender.sendMessage(chatTranslationStopped);
			if (args.length == 0 || args[0].equalsIgnoreCase("Stop"))
			{
				return true;
			}
		}
		else if (isGlobal && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof WWCActiveTranslator) //If /wwcg is called
		{
			main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
			final TextComponent chatTranslationStopped = Component.text()
					.append(main.getPluginPrefix().asComponent())
					.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
					.build();
			for (Player eaPlayer : Bukkit.getOnlinePlayers())
			{
				eaPlayer.sendMessage(chatTranslationStopped);
			}
			if (args.length == 0 || args[0].equalsIgnoreCase("Stop"))
			{
				return true;
			}
		}
		
		/* Sanitize args */
		if (args.length == 0 || args.length > 2)
		{
			//Not enough/too many args
			final TextComponent invalidArgs = Component.text()
					.append(main.getPluginPrefix().asComponent())
					.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
					.build();
			sender.sendMessage(invalidArgs);
			return false;
		}
		
		/* Process input */
		WWCDefinitions defs = new WWCDefinitions();
		if (args[0] instanceof String && args.length == 1)
		{
			//actually determine which translator (watson, g translate, bing) we are using (TODO)
			//Check if args[0] is a supported code by us + watson, defined in WWCDefinitions
			if (main.getTranslatorName().equals("Watson"))
			{
			    for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++)
			    {
			 	    if (defs.getSupportedWatsonLangCodes()[i].equals(args[0]))
				    {
					    //We got a valid lang code, continue and add player to ArrayList
					    if (!isGlobal) //Not global
					    {
					    	main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(), 
					    			"None",
					    			args[0], 
					    			"Watson",
					    			false));
					    	final TextComponent autoTranslate = Component.text()
					    			.append(main.getPluginPrefix().asComponent())
					    			.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
					    			.build();
					    	sender.sendMessage(autoTranslate);
					    	return true;
					    }
					    else //Is global
					    {
					    	main.addActiveTranslator(new WWCActiveTranslator("GLOBAL-TRANSLATE-ENABLED", 
					    			"None",
					    			args[0], 
					    			"Watson",
					    			false));
					    	final TextComponent autoTranslate = Component.text()
					    			.append(main.getPluginPrefix().asComponent())
					    			.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
					    			.build();
					    	for (Player eaPlayer : Bukkit.getOnlinePlayers())
					    	{
					    		eaPlayer.sendMessage(autoTranslate);
					    	}
					    	return true;
					    }	
				    }
			    }
			}
		} 
		else if (args[0] instanceof String && args[1] instanceof String && args.length == 2)
		{
			if (main.getTranslatorName().equals("Watson"))
			{
				for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++)
				{
					if (defs.getSupportedWatsonLangCodes()[i].equalsIgnoreCase(args[0]))
					{
						for (int j = 0; j < defs.getSupportedWatsonLangCodes().length; j++)
						{
							if (defs.getSupportedWatsonLangCodes()[j].equalsIgnoreCase(args[1]))
							{
								//We got a valid lang code 2x, continue and add player to ArrayList
								if (!isGlobal) //Not global
								{
									main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(), 
											args[0],
											args[1], 
											"Watson",
											false));
									final TextComponent langToLang = Component.text()
											.append(main.getPluginPrefix().asComponent())
											.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
											.build();
									sender.sendMessage(langToLang); 
								}
								else
								{
									main.addActiveTranslator(new WWCActiveTranslator("GLOBAL-TRANSLATE-ENABLED", 
											args[0],
											args[1], 
											"Watson",
											false));
									final TextComponent langToLang = Component.text()
											.append(main.getPluginPrefix().asComponent())
											.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
											.build();
									for (Player eaPlayer : Bukkit.getOnlinePlayers())
									{
										eaPlayer.sendMessage(langToLang);
									} 
								}
								return true;
							}
						}			
					}
				}
			}
		}
		String validLangCodes = "\n";
		for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++)
		{
			validLangCodes += " (" + defs.getSupportedWatsonLangCodes()[i] + ")";
		}
		
		/* Invalid Lang Code response */
		final TextComponent invalidLangCode = Component.text()
				.append(main.getPluginPrefix().asComponent())
				.append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidLangCode").replace("%o", validLangCodes)).color(NamedTextColor.RED))
				.build();
		sender.sendMessage(invalidLangCode);
		return false;
	}

}
