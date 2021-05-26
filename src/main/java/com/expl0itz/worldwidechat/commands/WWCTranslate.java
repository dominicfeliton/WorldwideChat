package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.ActiveTranslator;
import com.expl0itz.worldwidechat.misc.CommonDefinitions;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCTranslate extends BasicCommand {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    public WWCTranslate(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    /*
     * Correct Syntax: /wwct <id-in> <id-out>
     * EX for id: en, ar, cs, nl, etc.
     * id MUST be valid, we will check with CommonDefinitions class
     */

    public boolean processCommand(boolean isGlobal) {
    	/* Basic var initialization */
    	Audience adventureSender = main.adventure().sender(sender);
    	ActiveTranslator currTarget = main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString());
    	
    	/* Sanitize args */
        if (args.length > 3) {
            //Not enough/too many args
            final TextComponent invalidArgs = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
                .build();
            adventureSender.sendMessage(invalidArgs);
            return false;
        }
    	
    	/* GUI Checks */
    	if (args.length == 0 && !isGlobal) { /* User wants to see their own translation session */
    		WWCTranslateGUIMainMenu.getTranslateMainMenu(null).open((Player)sender);
    		return true;
    	} else if (args.length == 0 && isGlobal) { /* Global translate */
    		WWCTranslateGUIMainMenu.getTranslateMainMenu("GLOBAL-TRANSLATE-ENABLED").open((Player)sender);
    		return true;
    	} else if (args.length == 1 && args[0] instanceof String && !isGlobal && !args[0].equalsIgnoreCase(sender.getName()) && main.getServer().getPlayer(args[0]) != null) { /* User wants to see another translation session */
    		if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
    			WWCTranslateGUIMainMenu.getTranslateMainMenu(main.getServer().getPlayer(args[0]).getUniqueId().toString()).open((Player)sender);
    		} else {
    			final TextComponent badPerms = Component.text() //Bad perms
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBadPerms")).color(NamedTextColor.RED))
                        .append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")").color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    main.adventure().sender(sender).sendMessage(badPerms);
    		}
    		return true;
    	}
    	
        /* Existing translator checks */
        //If sender wants to overwrite their own existing translation session
        if (Bukkit.getServer().getPlayer(args[0]) == null && !isGlobal && currTarget instanceof ActiveTranslator) { //Don't let a person be multiple ActiveTranslator objs
        	main.removeActiveTranslator(currTarget);
            final TextComponent chatTranslationStopped = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            adventureSender.sendMessage(chatTranslationStopped);
            if (args[0] instanceof String && args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        //If sender wants to overwrite a target player's existing translation session
        } else if (args[0] instanceof String && Bukkit.getServer().getPlayer(args[0]) != null && !Bukkit.getServer().getPlayer(args[0]).getName().equals(sender.getName()) && !isGlobal && main.getActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString()) instanceof ActiveTranslator) {
        	Player testPlayer = Bukkit.getServer().getPlayer(args[0]);
        	Audience targetSender = main.adventure().sender(testPlayer);
            main.removeActiveTranslator(main.getActiveTranslator(testPlayer.getUniqueId().toString()));
            final TextComponent chatTranslationStopped = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
                    .build();
            targetSender.sendMessage(chatTranslationStopped);
            final TextComponent chatTranslationStoppedOtherPlayer = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStoppedOtherPlayer").replace("%i", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            adventureSender.sendMessage(chatTranslationStoppedOtherPlayer);
            if (args[1] instanceof String && args[1].equalsIgnoreCase("Stop")) {
                return true;
            }
        } 
        //If sender wants to overwrite /wwcg's existing translation session
        else if (isGlobal && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof ActiveTranslator) {
            main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
            final TextComponent chatTranslationStopped = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                main.adventure().sender(eaPlayer).sendMessage(chatTranslationStopped);
            } if (args[0] instanceof String && args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        }
            
        /* Process input */
        //Player has given us one argument
        if (args.length == 1 && args[0] instanceof String && CommonDefinitions.getSupportedTranslatorLang(args[0]) != null) {
        	//We got a valid lang code, continue and add player to ArrayList
            if (!isGlobal) {
            	ActiveTranslator currTranslator = new ActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(),
                        "None",
                        args[0],
                        false);
                main.addActiveTranslator(currTranslator);
                final TextComponent autoTranslate = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                adventureSender.sendMessage(autoTranslate);
            } else {
            	ActiveTranslator currTranslator = new ActiveTranslator("GLOBAL-TRANSLATE-ENABLED",
                        "None",
                        args[0],
                        false);
                main.addActiveTranslator(currTranslator);
                final TextComponent autoTranslate = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                    main.adventure().sender(eaPlayer).sendMessage(autoTranslate);
                }
            }
            return true;
        //Player has given us two arguments
        } else if (args.length == 2 && args[0] instanceof String && args[1] instanceof String && CommonDefinitions.getSupportedTranslatorLang(args[0]) != null && CommonDefinitions.getSupportedTranslatorLang(args[1]) != null) {
        	//We got a valid lang code 2x, continue and add player to ArrayList
            if (args[0].equalsIgnoreCase(args[1]) || (CommonDefinitions.isSameLang(args[0], args[1]))) {
                final TextComponent sameLangError = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctSameLangError")).color(NamedTextColor.RED))
                    .build();
                adventureSender.sendMessage(sameLangError);
                return false;
            } 
            if (!isGlobal) {
                ActiveTranslator currTranslator = new ActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(),
                        args[0],
                        args[1],
                        false);
                main.addActiveTranslator(currTranslator);
                final TextComponent langToLang = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                adventureSender.sendMessage(langToLang);
            } else {
            	ActiveTranslator currTranslator = new ActiveTranslator("GLOBAL-TRANSLATE-ENABLED",
                        args[0],
                        args[1],
                        false);
                main.addActiveTranslator(currTranslator);
                final TextComponent langToLang = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                //Add global data to its own config in /data
                for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                    Audience eaAudience = main.adventure().sender(eaPlayer);
                    eaAudience.sendMessage(langToLang);
                }
            } 
            return true;
        // Player has given us two arguments: Case 2 (First arg is another player who is not ourselves...)
        } else if (args.length == 2 && args[0] instanceof String && args[1] instanceof String && (!(args[0].equals(sender.getName())) && CommonDefinitions.getSupportedTranslatorLang(args[1]) != null)) {
        	if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
        		if (Bukkit.getServer().getPlayer(args[0]) != null && (!(args[0].equals(sender.getName())))) {
        			if (!isGlobal) {
        				ActiveTranslator currTranslator = new ActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString(),
                                "None",
                                args[1],
                                false);
                        main.addActiveTranslator(currTranslator);
                        final TextComponent autoTranslateOtherPlayer = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStartOtherPlayer").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                            .build();
                        adventureSender.sendMessage(autoTranslateOtherPlayer);
                        final TextComponent autoTranslate = Component.text()
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStart").replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                                .build();
                        main.adventure().sender(main.getServer().getPlayer(args[0])).sendMessage(autoTranslate);
        			}
            	} else {
                    final TextComponent playerNotFound = Component.text() //Target player not found
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                            .build();
                        adventureSender.sendMessage(playerNotFound);
                }
            } else { //Bad Perms
                final TextComponent badPerms = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBadPerms")).color(NamedTextColor.RED))
                        .append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")").color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    adventureSender.sendMessage(badPerms);
            }
        	return true;
        //Player has given us three arguments
        } else if (args.length == 3 && args[0] instanceof String && args[1] instanceof String && args[2] instanceof String && CommonDefinitions.getSupportedTranslatorLang(args[1]) != null && CommonDefinitions.getSupportedTranslatorLang(args[2]) != null) {
            //If args[0] == a player, see if sender has perms to change player translation preferences
            if (sender.hasPermission("worldwidechat.wwct.otherplayers")) {
                if (Bukkit.getServer().getPlayer(args[0]) != null && (!(args[0].equals(sender.getName())))) {
                	//We got a valid lang code 2x, continue and add player to ArrayList
                    if (args[1].equalsIgnoreCase(args[2]) || (CommonDefinitions.isSameLang(args[1], args[2]))) {
                        final TextComponent sameLangError = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctSameLangError")).color(NamedTextColor.RED))
                            .build();
                        adventureSender.sendMessage(sameLangError);
                        return false;
                    }
                     //Add target user to ArrayList with input + output lang
                    ActiveTranslator currTranslator = new ActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString(),
                            args[1],
                            args[2],
                            false);
                     main.addActiveTranslator(currTranslator);
                     final TextComponent langToLangOtherPlayer = Component.text()
                         .append(main.getPluginPrefix().asComponent())
                         .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStartOtherPlayer").replace("%o", args[0]).replace("%i", args[1]).replace("%e", args[2])).color(NamedTextColor.LIGHT_PURPLE))
                         .build();
                     adventureSender.sendMessage(langToLangOtherPlayer);
                     final TextComponent langToLang = Component.text()
                             .append(main.getPluginPrefix().asComponent())
                             .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStart").replace("%i", args[1]).replace("%o", args[2])).color(NamedTextColor.LIGHT_PURPLE))
                             .build();
                     main.adventure().sender(main.getServer().getPlayer(args[0])).sendMessage(langToLang);
                } else {
                    final TextComponent playerNotFound = Component.text() //Target player not found
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                            .build();
                        adventureSender.sendMessage(playerNotFound);
                }
            } else {
                final TextComponent badPerms = Component.text() //Bad perms
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBadPerms")).color(NamedTextColor.RED))
                    .append(Component.text().content(" (" + "worldwidechat.wwct.otherplayers" + ")").color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                adventureSender.sendMessage(badPerms);
            }
            return true;
        }
        
        /* If we got here, invalid lang code */
        final TextComponent invalidLangCode = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidLangCode").replace("%o", CommonDefinitions.getFormattedValidLangCodes())).color(NamedTextColor.RED))
            .build();
        adventureSender.sendMessage(invalidLangCode);
        return false;
    }

}