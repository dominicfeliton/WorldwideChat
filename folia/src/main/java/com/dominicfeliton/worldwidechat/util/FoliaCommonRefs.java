package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;

public class FoliaCommonRefs extends CommonRefs {

    private WorldwideChat main = WorldwideChat.instance;

    @Override
    public void sendMsg(CommandSender sender, TextComponent originalMessage) {
        try {
            final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(" "))
                    .append(originalMessage.asComponent())
                    .build();
            sender.sendMessage(outMessage);
        } catch (IllegalStateException e) {}
    }

    /**
     * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
     * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
     * @return Boolean - Whether the server is reloading/stopping or not
     */
    @Override
    public boolean serverIsStopping() {
        boolean stopping = !main.isEnabled() && main.getServer().isStopping();
        debugMsg("Folia initial stop check: " + stopping);
        return stopping;
    }

}
