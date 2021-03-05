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

    public WWCTranslate(CommandSender sender, Command command, String label, String[] args, WorldwideChat main) {
        super(sender, command, label, args, main);
    }

    /*
     * Correct Syntax: /wwct <id-in> <id-out>
     * EX for id: en, ar, cs, nl, etc.
     * id MUST be valid, is checked by Definitions class
     */

    public boolean processCommand(boolean isGlobal) {
        /* Sanity checks */
        WWCActiveTranslator currTarget = main.getActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString());
        if (currTarget instanceof WWCActiveTranslator) {
            main.removeActiveTranslator(currTarget);
            final TextComponent chatTranslationStopped = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            sender.sendMessage(chatTranslationStopped);
            if (args.length == 0 || args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        } else if (isGlobal && main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED") instanceof WWCActiveTranslator) //If /wwcg is called
        {
            main.removeActiveTranslator(main.getActiveTranslator("GLOBAL-TRANSLATE-ENABLED"));
            final TextComponent chatTranslationStopped = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgTranslationStopped")).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                eaPlayer.sendMessage(chatTranslationStopped);
            }
            if (args.length == 0 || args[0].equalsIgnoreCase("Stop")) {
                return true;
            }
        }

        /* Sanitize args */
        if (args.length == 0 || args.length > 3) {
            //Not enough/too many args
            final TextComponent invalidArgs = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
                .build();
            sender.sendMessage(invalidArgs);
            return false;
        }
        
        /* Check if already running on another player; Sanity Checks P2 */
        Player testPlayer = Bukkit.getServer().getPlayer(args[0]);
        if (testPlayer != null && main.getActiveTranslator(testPlayer.getUniqueId().toString()) instanceof WWCActiveTranslator && args.length == 1) {
            main.removeActiveTranslator(main.getActiveTranslator(testPlayer.getUniqueId().toString()));
            final TextComponent chatTranslationStopped = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctTranslationStoppedOtherPlayer").replace("%i", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                .build();
            sender.sendMessage(chatTranslationStopped);
        }

        /* Process input */
        WWCDefinitions defs = new WWCDefinitions();
        if (args[0] instanceof String && args.length == 1) {
            if (main.getTranslatorName().equals("Watson")) {
                for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++) {
                    if (defs.getSupportedWatsonLangCodes()[i].equals(args[0])) {
                        //We got a valid lang code, continue and add player to ArrayList
                        if (!isGlobal) //Not global
                        {
                            main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(),
                                "None",
                                args[0],
                                false));
                            final TextComponent autoTranslate = Component.text()
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                                .build();
                            sender.sendMessage(autoTranslate);
                            return true;
                        } else { //Is global
                            main.addActiveTranslator(new WWCActiveTranslator("GLOBAL-TRANSLATE-ENABLED",
                                "None",
                                args[0],
                                false));
                            final TextComponent autoTranslate = Component.text()
                                .append(main.getPluginPrefix().asComponent())
                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgAutoTranslateStart").replace("%o", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                                .build();
                            for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                                eaPlayer.sendMessage(autoTranslate);
                            }
                            return true;
                        }
                    }
                }
            }
        } else if (args[0] instanceof String && args[1] instanceof String && args.length == 2) {
            if (main.getTranslatorName().equals("Watson")) {
                for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++) {
                    if (defs.getSupportedWatsonLangCodes()[i].equalsIgnoreCase(args[0])) {
                        for (int j = 0; j < defs.getSupportedWatsonLangCodes().length; j++) {
                            if (defs.getSupportedWatsonLangCodes()[j].equalsIgnoreCase(args[1])) {
                                //We got a valid lang code 2x, continue and add player to ArrayList
                                if (args[0].equalsIgnoreCase(args[1])) //input lang cannot be equal to output lang
                                {
                                    final TextComponent sameLangError = Component.text()
                                        .append(main.getPluginPrefix().asComponent())
                                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctSameLangError")).color(NamedTextColor.RED))
                                        .build();
                                    sender.sendMessage(sameLangError);
                                    return false;
                                }
                                if (!isGlobal) //Not global
                                {
                                    main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(sender.getName()).getUniqueId().toString(),
                                        args[0],
                                        args[1],
                                        false));
                                    final TextComponent langToLang = Component.text()
                                        .append(main.getPluginPrefix().asComponent())
                                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                                        .build();
                                    sender.sendMessage(langToLang);
                                } else {
                                    main.addActiveTranslator(new WWCActiveTranslator("GLOBAL-TRANSLATE-ENABLED",
                                        args[0],
                                        args[1],
                                        false));
                                    final TextComponent langToLang = Component.text()
                                        .append(main.getPluginPrefix().asComponent())
                                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcgLangToLangStart").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                                        .build();
                                    for (Player eaPlayer: Bukkit.getOnlinePlayers()) {
                                        eaPlayer.sendMessage(langToLang);
                                    }
                                }
                                return true;
                            }
                        }
                    } else if (Bukkit.getServer().getPlayer(args[0]) != null && (!(args[0].equals(sender.getName())))) { /* First arg is another player who is not ourselves...*/
                        if (sender.hasPermission("worldwidechat.wwctotherplayers")) {
                            for (int j = 0; j < defs.getSupportedWatsonLangCodes().length; j++) {
                                if (defs.getSupportedWatsonLangCodes()[j].equalsIgnoreCase(args[1])) {
                                    main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString(),
                                            args[1],
                                            "None",
                                            false));
                                    final TextComponent autoTranslateOtherPlayer = Component.text()
                                        .append(main.getPluginPrefix().asComponent())
                                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctAutoTranslateStartOtherPlayer").replace("%i", args[0]).replace("%o", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                                        .build();
                                    sender.sendMessage(autoTranslateOtherPlayer);
                                    Bukkit.getServer().getPlayer(args[0]).sendMessage(autoTranslateOtherPlayer);
                                    return true;
                                }
                            }
                        } else { //Bad Perms
                            final TextComponent badPerms = Component.text()
                                    .append(main.getPluginPrefix().asComponent())
                                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBadPerms")).color(NamedTextColor.RED))
                                    .append(Component.text().content(" (" + "worldwidechat.wwctotherplayers" + ")").color(NamedTextColor.LIGHT_PURPLE))
                                    .build();
                                sender.sendMessage(badPerms);
                            return true;
                        }
                    }
                }
            }
        } else if (args[0] instanceof String && args[1] instanceof String && args[2] instanceof String && args.length == 3) {
            //If args[0] == a player, see if sender has perms to change player translation preferences
            if (sender.hasPermission("worldwidechat.wwctotherplayers")) {
                if (Bukkit.getServer().getPlayer(args[0]) != null && (!(args[0].equals(sender.getName())))) {
                    if (main.getTranslatorName().equals("Watson")) {
                        for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++) {
                            if (defs.getSupportedWatsonLangCodes()[i].equalsIgnoreCase(args[1])) {
                                for (int j = 0; j < defs.getSupportedWatsonLangCodes().length; j++) {
                                    if (defs.getSupportedWatsonLangCodes()[j].equalsIgnoreCase(args[2])) {
                                        //We got a valid lang code 2x, continue and add player to ArrayList
                                        if (args[1].equalsIgnoreCase(args[2])) //input lang cannot be equal to output lang
                                        {
                                            final TextComponent sameLangError = Component.text()
                                                .append(main.getPluginPrefix().asComponent())
                                                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctSameLangError")).color(NamedTextColor.RED))
                                                .build();
                                            sender.sendMessage(sameLangError);
                                            return false;
                                        }
                                        main.addActiveTranslator(new WWCActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString(),
                                            args[1],
                                            args[2],
                                            false));
                                        final TextComponent langToLangOtherPlayer = Component.text()
                                            .append(main.getPluginPrefix().asComponent())
                                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctLangToLangStartOtherPlayer").replace("%o", args[0]).replace("%i", args[1]).replace("%e", args[2])).color(NamedTextColor.LIGHT_PURPLE))
                                            .build();
                                        sender.sendMessage(langToLangOtherPlayer);
                                        Bukkit.getServer().getPlayer(args[0]).sendMessage(langToLangOtherPlayer);
                                        return true;
                                    }
                                }
                            }
                        }    
                    }
                } else {
                    final TextComponent playerNotFound = Component.text() //Player not found
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                            .build();
                        sender.sendMessage(playerNotFound);
                        return true;
                }
            } else {
                final TextComponent badPerms = Component.text() //Bad perms
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcBadPerms")).color(NamedTextColor.RED))
                    .append(Component.text().content(" (" + "worldwidechat.wwctotherplayers" + ")").color(NamedTextColor.LIGHT_PURPLE))
                    .build();
                sender.sendMessage(badPerms);
                return true;
            }
        }
        
        /* If we got here, invalid lang code */
        String validLangCodes = "\n";
        for (int i = 0; i < defs.getSupportedWatsonLangCodes().length; i++) {
            validLangCodes += " (" + defs.getSupportedWatsonLangCodes()[i] + ")";
        }
        
        final TextComponent invalidLangCode = Component.text()
            .append(main.getPluginPrefix().asComponent())
            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidLangCode").replace("%o", validLangCodes)).color(NamedTextColor.RED))
            .build();
        sender.sendMessage(invalidLangCode);
        return false;
    }

}