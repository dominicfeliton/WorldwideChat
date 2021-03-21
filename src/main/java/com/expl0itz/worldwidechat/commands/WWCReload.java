package com.expl0itz.worldwidechat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import com.expl0itz.worldwidechat.WorldwideChat;
import com.expl0itz.worldwidechat.runnables.LoadUserData;
import com.expl0itz.worldwidechat.runnables.UpdateChecker;

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
        
        //Cancel all background tasks
        main.cancelBackgroundTasks();

        //Reload main config + other configs
        if (main.loadPluginConfigs()) { //If config loading succeeded
            main.getLogger().info(ChatColor.LIGHT_PURPLE + main.getConfigManager().getMessagesConfig().getString("Messages.wwcConfigConnectionSuccess").replace("%o", main.getTranslatorName()));
            
            //Check for Updates
            BukkitTask updateChecker = Bukkit.getScheduler().runTaskAsynchronously(main, new UpdateChecker()); //Run update checker now
            main.addBackgroundTask("updateChecker", updateChecker);
            
            //Load saved user data
            Bukkit.getScheduler().runTaskAsynchronously(main, new LoadUserData());
            main.getLogger().info(ChatColor.LIGHT_PURPLE + main.getConfigManager().getMessagesConfig().getString("Messages.wwcUserDataReloaded"));
            
            //We made it!
            final TextComponent wwcrSuccess = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcrSuccess")).color(NamedTextColor.GREEN))
                    .build();
                adventureSender.sendMessage(wwcrSuccess);
            main.getLogger().info(ChatColor.GREEN + main.getConfigManager().getMessagesConfig().getString("Messages.wwcEnabled").replace("%i", main.getPluginVersion() + ""));
        } else { //If config loading failed
            final TextComponent wwcrFail = Component.text()
                    .append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(main.getConfigManager().getMessagesConfig().getString("Messages.wwcrFail")).color(NamedTextColor.RED))
                    .build();
                adventureSender.sendMessage(wwcrFail);
            main.getLogger().severe(ChatColor.RED + main.getConfigManager().getMessagesConfig().getString("Messages.wwcInitializationFail").replace("%o", main.getTranslatorName()));
            main.getServer().getPluginManager().disablePlugin(main);
        }
        return true;
    }
}