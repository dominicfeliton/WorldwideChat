package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Folia implementation that mirrors {@link PaperCommonRefs} but without any
 * thread‑scheduling logic.  Folia already runs on the main server thread
 * for command‑sender messaging, and Adventure is natively available, so we
 * simply use the standard Bukkit {@code sender.sendMessage(...)} call.
 */
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

        // Do not attempt to send to offline players
        if (sender instanceof Player && !((Player) sender).isOnline()) return;
        sender.sendMessage(outMessage);
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

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

        p.sendMessage(outMessage);
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