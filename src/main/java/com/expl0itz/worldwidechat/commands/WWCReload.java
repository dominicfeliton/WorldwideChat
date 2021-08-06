package com.expl0itz.worldwidechat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.expl0itz.worldwidechat.WorldwideChat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class WWCReload extends BasicCommand {

    private WorldwideChat main = WorldwideChat.getInstance();
    
    public WWCReload(CommandSender sender, Command command, String label, String[] args) {
        super(sender, command, label, args);
    }

    public boolean processCommand() {
    	//Convert sender to Adventure Audience
        Audience adventureSender = main.adventure().sender(sender);
        
        //Reload!
        main.reload();
        
		//We made it!
        final TextComponent wwcrSuccess = Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcrSuccess")).color(NamedTextColor.GREEN))
                .build();
            adventureSender.sendMessage(wwcrSuccess);
        main.getLogger().info(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcEnabled").replace("%i", main.getPluginVersion() + ""));
        
        //TODO: Readd fail message in a try/catch 
        return true;
    }
}