package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpigotComponentMessenger implements ComponentMessenger {
    public static final SpigotComponentMessenger INSTANCE = new SpigotComponentMessenger();

    private static final GsonComponentSerializer GSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private SpigotComponentMessenger() {
    }

    @Override
    public void sendMessage(CommandSender sender, Component message) {
        if (sender == null || message == null) return;
        runOnMainThread(() -> sendMessageNow(sender, message));
    }

    @Override
    public void sendActionBar(CommandSender sender, Component message) {
        if (!(sender instanceof Player) || message == null) return;
        runOnMainThread(() -> sendActionBarNow((Player) sender, message));
    }

    private void sendMessageNow(CommandSender sender, Component message) {
        try {
            sender.spigot().sendMessage(toBaseComponents(message));
        } catch (RuntimeException | LinkageError ignored) {
            try {
                sender.spigot().sendMessage(toLegacyBaseComponents(message));
            } catch (RuntimeException | LinkageError fallbackIgnored) {
                try {
                    sender.sendMessage(LEGACY.serialize(message));
                } catch (RuntimeException | LinkageError finalIgnored) {
                }
            }
        }
    }

    private void sendActionBarNow(Player player, Component message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, toBaseComponents(message));
        } catch (RuntimeException | LinkageError ignored) {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, toLegacyBaseComponents(message));
            } catch (RuntimeException | LinkageError fallbackIgnored) {
            }
        }
    }

    private void runOnMainThread(Runnable action) {
        if (Bukkit.isPrimaryThread()) {
            action.run();
            return;
        }

        WorldwideChat main = WorldwideChat.instance;
        if (main == null || !main.isEnabled()) return;

        try {
            main.getServer().getScheduler().runTask(main, action);
        } catch (RuntimeException | LinkageError ignored) {
        }
    }

    BaseComponent[] toBaseComponents(Component message) {
        return ComponentSerializer.parse(GSON.serialize(message));
    }

    BaseComponent[] toLegacyBaseComponents(Component message) {
        return new BaseComponent[]{
                net.md_5.bungee.api.chat.TextComponent.fromLegacy(LEGACY.serialize(message))
        };
    }
}
