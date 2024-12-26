package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

public class PaperCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        try {
            final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                    .append(Component.space())
                    .append(originalMessage.asComponent())
                    .build();
            sender.sendMessage(outMessage);
        } catch (IllegalStateException e) {
        }
    }

}
