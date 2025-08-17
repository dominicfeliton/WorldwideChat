package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.util.GenericRunnable;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class FoliaCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        if (sender == null || originalMessage == null) return;

        // Build the final component (with or without prefix)
        TextComponent outMessage = addPrefix
                ? Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.space())
                .append(originalMessage.asComponent())
                .build()
                : Component.text().append(originalMessage.asComponent()).build();

        // Adventure send – no Bukkit send attempt
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.isOnline()) return;
            Audience adventureSender = main.adventure().sender(p);
            adventureSender.sendMessage(outMessage);
        } else {
            Audience adventureSender = main.adventure().sender(sender);
            adventureSender.sendMessage(outMessage);
        }
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

    /**
     * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
     * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
     *
     * @return Boolean - Whether the server is reloading/stopping or not
     */
    public void sendMsg(UUID playerId, Component originalMessage, boolean addPrefix) {
        if (playerId == null || originalMessage == null) return;
        Player p = Bukkit.getPlayer(playerId);
        if (p == null || !p.isOnline()) return;
        TextComponent outMessage = addPrefix
                ? Component.text()
                .append(main.getPluginPrefix().asComponent())
                .append(Component.space())
                .append(originalMessage.asComponent())
                .build()
                : Component.text().append(originalMessage.asComponent()).build();
        Audience adventureSender = main.adventure().sender(p);
        adventureSender.sendMessage(outMessage);
    }

    public void sendMsg(UUID playerId, Component originalMessage) {
        sendMsg(playerId, originalMessage, true);
    }

    @Override
    public boolean serverIsStopping() {
        boolean stopping = !main.isEnabled() && main.getServer().isStopping();
        debugMsg("Folia stop check: " + stopping);
        return stopping;
    }
}