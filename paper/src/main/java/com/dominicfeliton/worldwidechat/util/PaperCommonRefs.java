package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

public class PaperCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        if (sender == null || originalMessage == null) return;
        if (!Bukkit.isPrimaryThread()) {
            wwcHelper.runSync(true, 0, new GenericRunnable() {
                @Override protected void execute() {
                    sendMsg(sender, originalMessage, addPrefix);
                }
            }, sender instanceof Player ? ENTITY : GLOBAL, sender instanceof Player ? new Object[]{sender} : null);
            return;
        }
        final Component outMessage = addPrefix
                ? main.getPluginPrefix()
                .append(Component.space())
                .append(originalMessage)
                : Component.empty().append(originalMessage);
        try {
            sender.sendMessage(outMessage);
        } catch (RuntimeException | LinkageError ignored) {
        }
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
            }, GLOBAL, null);
            return;
        }
        Player p = Bukkit.getPlayer(playerId);
        if (p == null) return;
        sendMsg(p, originalMessage, addPrefix);
    }

    public void sendMsg(UUID playerId, Component originalMessage) {
        sendMsg(playerId, originalMessage, true);
    }
}
