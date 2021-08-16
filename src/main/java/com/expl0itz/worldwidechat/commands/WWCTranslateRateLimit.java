package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.util.ActiveTranslator;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCTranslateRateLimit extends BasicCommand {

	private WorldwideChat main = WorldwideChat.getInstance();
	
	public WWCTranslateRateLimit(CommandSender sender, Command command, String label, String[] args) {
		super(sender, command, label, args);
	}
	
	public boolean processCommand() { /* Init vars */
		/* Init vars */
        Audience adventureSender = main.adventure().sender(sender);
		
		/* Sanitize args */
        if (args.length > 2) {
            //Not enough/too many args
            final TextComponent invalidArgs = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
                .build();
            adventureSender.sendMessage(invalidArgs);
            return false;
        }
        
        /* Disable existing rate limit */
        if (args.length == 0) {
        	if (main.getActiveTranslator(((Player)sender).getUniqueId().toString()) != null) {
        		ActiveTranslator currTranslator = main.getActiveTranslator(((Player)sender).getUniqueId().toString());
        		if (currTranslator.getRateLimit() == 0) {
        			final TextComponent rateLimitAlreadyOff = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitAlreadyOffSender")).color(NamedTextColor.YELLOW))
                            .build();
                        adventureSender.sendMessage(rateLimitAlreadyOff);
        			return false;
        		}
        		currTranslator.setRateLimit(0);
        		final TextComponent rateLimitOff = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitOffSender")).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    adventureSender.sendMessage(rateLimitOff);
                return true;
        	} else {
            	// If player is not translating:
            	final TextComponent notAPlayer = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlNotATranslator")).color(NamedTextColor.RED))
                        .build();
                    adventureSender.sendMessage(notAPlayer);
                return true;
            }
        } 
        
        /* Set personal rate limit */
        if (args.length == 1 && args[0].matches("[0-9]+")) {
            if (main.getActiveTranslator(((Player)sender).getUniqueId().toString()) != null) {
            	ActiveTranslator currTranslator = main.getActiveTranslator(((Player)sender).getUniqueId().toString());
            	currTranslator.setRateLimit(Integer.parseInt(args[0]));
            	final TextComponent rateLimitSet = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitSetSender").replace("%i", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    adventureSender.sendMessage(rateLimitSet);
                return true;
            } else {
                final TextComponent notAPlayer = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlNotATranslator")).color(NamedTextColor.RED))
                        .build();
                    adventureSender.sendMessage(notAPlayer);
                return true;
            }
        }
        
        /* Disable another user's existing rate limit */
        if (args.length == 1 && args[0] instanceof String) {
            if (Bukkit.getPlayer(args[0]) != null && main.getActiveTranslator((Bukkit.getPlayer(args[0])).getUniqueId().toString()) != null && !args[0].equalsIgnoreCase(sender.getName())) {
            	ActiveTranslator currTranslator = main.getActiveTranslator((Bukkit.getPlayer(args[0])).getUniqueId().toString());
            	if (currTranslator.getRateLimit() == 0) {
        			final TextComponent rateLimitAlreadyOff = Component.text()
                            .append(main.getPluginPrefix().asComponent())
                            .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitAlreadyOffTarget").replace("%i", args[0])).color(NamedTextColor.YELLOW))
                            .build();
                        adventureSender.sendMessage(rateLimitAlreadyOff);
        			return false;
        		}
            	currTranslator.setRateLimit(0);
                final TextComponent rateLimitOff = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitOffTarget").replace("%i", args[0])).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    adventureSender.sendMessage(rateLimitOff);
                final TextComponent rateLimitOffReceiver = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitOffSender")).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    main.adventure().player(Bukkit.getPlayer(args[0])).sendMessage(rateLimitOffReceiver);
                return true;
            } else {
                final TextComponent notAPlayer = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                        .build();
                    adventureSender.sendMessage(notAPlayer);
                return true;
            }
        }
        
        /* Set rate limit for another user */
        if (args.length == 2 && args[0] instanceof String && args[1].matches("[0-9]+")) {
            if (Bukkit.getServer().getPlayer(args[0]) != null && main.getActiveTranslator(Bukkit.getPlayer(args[0]).getUniqueId().toString()) != null && !args[0].equalsIgnoreCase(sender.getName())) {
            	ActiveTranslator currTranslator =  main.getActiveTranslator(Bukkit.getPlayer(args[0]).getUniqueId().toString());
                currTranslator.setRateLimit(Integer.parseInt(args[1]));
                final TextComponent rateLimitSet = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitSetTarget").replace("%i", args[0]).replace("%o", args[1] + "")).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    adventureSender.sendMessage(rateLimitSet);
                final TextComponent rateLimitSetReceiver = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlRateLimitSetSender").replace("%i", args[1])).color(NamedTextColor.LIGHT_PURPLE))
                        .build();
                    main.adventure().player(Bukkit.getPlayer(args[0])).sendMessage(rateLimitSetReceiver);
                return true;
            } else {
            	// If target is not a string, active translator, or is themselves:
                final TextComponent notAPlayer = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctrlPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                        .build();
                    adventureSender.sendMessage(notAPlayer);
                return true;
            }
        }
        return false;
	}
}
