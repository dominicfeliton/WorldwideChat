package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class PaperCommonRefs extends CommonRefs {

    private WorldwideChat main = WorldwideChat.instance;

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        try {
            final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                    .append(Component.space())
                    .append(originalMessage.asComponent())
                    .build();
            sender.sendMessage(outMessage);
        } catch (IllegalStateException e) {}
    }

}
