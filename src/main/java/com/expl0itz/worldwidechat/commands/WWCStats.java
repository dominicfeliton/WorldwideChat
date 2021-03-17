package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.misc.PlayerRecord;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WWCStats extends BasicCommand{

    private WorldwideChat main = WorldwideChat.getInstance();
    
    public WWCStats(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }
    
    public boolean processCommand() {
        /* Init vars */
        Audience adventureSender = main.adventure().sender(sender);
        boolean isActiveTranslator = false;
        
        /* Sanitze args */
        if (args.length == 0 || args.length > 1) {
          //Not enough/too many args
            final TextComponent invalidArgs = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwctInvalidArgs")).color(NamedTextColor.RED))
                .build();
            adventureSender.sendMessage(invalidArgs);
            return false;
        }
        
        if (args[0] instanceof String && Bukkit.getServer().getPlayer(args[0]) != null) {
            //Get stats
            if (main.getPlayerRecord(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString()) != null) {
                //Is on record; continue
                PlayerRecord record = main.getPlayerRecord(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString());
                if (main.getActiveTranslator(Bukkit.getServer().getPlayer(args[0]).getUniqueId().toString()) != null) { //is currently active
                    isActiveTranslator = true;
                }
                final TextComponent stats = Component.text()
                        .append(main.getPluginPrefix().asComponent())
                        .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcsName").replace("%i", args[0])).color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
                        .append(Component.text().content("\n- " + main.getConfigManager().getMessagesConfig().getString("Messages.wwcsIsActiveTranslator").replace("%i", isActiveTranslator + "")).color(NamedTextColor.AQUA))
                        .append(Component.text().content("\n- " + main.getConfigManager().getMessagesConfig().getString("Messages.wwcsAttemptedTranslations").replace("%i", record.getAttemptedTranslations() + "")).color(NamedTextColor.AQUA))
                        .append(Component.text().content("\n- " + main.getConfigManager().getMessagesConfig().getString("Messages.wwcsSuccessfulTranslations").replace("%i", record.getSuccessfulTranslations() + "")).color(NamedTextColor.AQUA))
                        .append(Component.text().content("\n- " + main.getConfigManager().getMessagesConfig().getString("Messages.wwcsLastTranslationTime").replace("%i", record.getLastTranslationTime())).color(NamedTextColor.AQUA))
                        .build();
                adventureSender.sendMessage(stats);
            } else {
            final TextComponent notATranslator = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcsNotATranslator").replace("%i", args[0])))
                    .build();
            adventureSender.sendMessage(notATranslator);
            return true;
            }
        } else {
            final TextComponent playerNotFound = Component.text() //Target player not found
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcPlayerNotFound").replace("%i", args[0])).color(NamedTextColor.RED))
                    .build();
                adventureSender.sendMessage(playerNotFound);
            return true;
        }
        return true;
    }

}
