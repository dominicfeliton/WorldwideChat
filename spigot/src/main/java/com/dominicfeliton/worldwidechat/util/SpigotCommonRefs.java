package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class SpigotCommonRefs extends CommonRefs {
    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        if (sender == null || originalMessage == null) return;

        final Component outMessage = addPrefix
                ? main.getPluginPrefix()
                .append(Component.space())
                .append(originalMessage)
                : Component.empty().append(originalMessage);
        SpigotComponentMessenger.INSTANCE.sendMessage(sender, outMessage);
    }
}
