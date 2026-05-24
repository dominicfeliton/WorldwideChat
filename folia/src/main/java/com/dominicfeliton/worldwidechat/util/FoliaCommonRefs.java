package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;
import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.GLOBAL;

/**
 * Folia implementation that schedules command-sender messaging onto the
 * correct entity or global scheduler before touching Bukkit send APIs.
 */
public class FoliaCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        if (sender == null || originalMessage == null) return;
        Component outMessage = addPrefix
                ? main.getPluginPrefix()
                .append(Component.space())
                .append(originalMessage)
                : Component.empty().append(originalMessage);

        wwcHelper.runSync(true, 0, new GenericRunnable() {
            @Override
            protected void execute() {
                try {
                    sender.sendMessage(outMessage);
                } catch (RuntimeException | LinkageError ignored) {
                }
            }
        }, sender instanceof Player ? ENTITY : GLOBAL, sender instanceof Player ? new Object[]{sender} : null);
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

    public void sendMsg(UUID playerId, Component originalMessage, boolean addPrefix) {
        if (playerId == null || originalMessage == null) return;

        wwcHelper.runSync(true, 0, new GenericRunnable() {
            @Override
            protected void execute() {
                Player p = Bukkit.getPlayer(playerId);
                if (p == null) return;
                sendMsg(p, originalMessage, addPrefix);
            }
        }, GLOBAL, null);
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
