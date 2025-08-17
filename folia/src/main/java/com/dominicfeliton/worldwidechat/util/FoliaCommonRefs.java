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
        if (sender instanceof Player) {
            Player p = (Player) sender;
            wwcHelper.runSync(true, 0, new GenericRunnable() {
                @Override protected void execute() {
                    if (!p.isOnline()) return;
                    try {
                        Audience adventureSender = main.adventure().sender(p);
                        TextComponent outMessage = addPrefix
                                ? Component.text().append(main.getPluginPrefix().asComponent()).append(Component.space()).append(originalMessage.asComponent()).build()
                                : Component.text().append(originalMessage.asComponent()).build();
                        adventureSender.sendMessage(outMessage);
                    } catch (IllegalStateException ignored) {}
                }
            }, ENTITY, new Object[]{p});
            return;
        }
        wwcHelper.runSync(true, 0, new GenericRunnable() {
            @Override protected void execute() {
                try {
                    Audience adventureSender = main.adventure().sender(sender);
                    TextComponent outMessage = addPrefix
                            ? Component.text().append(main.getPluginPrefix().asComponent()).append(Component.space()).append(originalMessage.asComponent()).build()
                            : Component.text().append(originalMessage.asComponent()).build();
                    adventureSender.sendMessage(outMessage);
                } catch (IllegalStateException ignored) {}
            }
        }, ENTITY, new Object[]{null});
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

    public void sendMsg(UUID playerId, Component originalMessage, boolean addPrefix) {
        if (playerId == null || originalMessage == null) return;
        Player p = Bukkit.getPlayer(playerId);
        if (p == null || !p.isOnline()) return;
        wwcHelper.runSync(true, 0, new GenericRunnable() {
            @Override protected void execute() {
                try {
                    Audience adventureSender = main.adventure().sender(p);
                    TextComponent outMessage = addPrefix
                            ? Component.text().append(main.getPluginPrefix().asComponent()).append(Component.space()).append(originalMessage.asComponent()).build()
                            : Component.text().append(originalMessage.asComponent()).build();
                    adventureSender.sendMessage(outMessage);
                } catch (IllegalStateException ignored) {}
            }
        }, ENTITY, new Object[]{p});
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