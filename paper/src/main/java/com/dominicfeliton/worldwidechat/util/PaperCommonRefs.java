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

public class PaperCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        if (sender == null || originalMessage == null) return;
        if (!Bukkit.isPrimaryThread()) {
            Object anchor = (sender instanceof Player) ? sender : null;
            wwcHelper.runSync(true, 0, new GenericRunnable() {
                @Override protected void execute() {
                    sendMsg(sender, originalMessage, addPrefix);
                }
            }, ENTITY, new Object[]{anchor});
            return;
        }
        if (sender instanceof Player && !((Player) sender).isOnline()) return;
        try {
            Audience adventureSender = main.adventure().sender(sender);
            TextComponent outMessage = addPrefix
                    ? Component.text().append(main.getPluginPrefix().asComponent()).append(Component.space()).append(originalMessage.asComponent()).build()
                    : Component.text().append(originalMessage.asComponent()).build();
            adventureSender.sendMessage(outMessage);
        } catch (IllegalStateException ignored) {}
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

    public void sendMsg(UUID playerId, Component originalMessage, boolean addPrefix) {
        if (playerId == null || originalMessage == null) return;
        if (!Bukkit.isPrimaryThread()) {
            wwcHelper.runSync(true, 0, new GenericRunnable() {
                @Override protected void execute() {
                    sendMsg(playerId, originalMessage, addPrefix);
                }
            }, ENTITY, new Object[]{null});
            return;
        }
        Player p = Bukkit.getPlayer(playerId);
        if (p == null || !p.isOnline()) return;
        sendMsg(p, originalMessage, addPrefix);
    }

    public void sendMsg(UUID playerId, Component originalMessage) {
        sendMsg(playerId, originalMessage, true);
    }
}