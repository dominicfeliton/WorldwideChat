package com.badskater0729.worldwidechat.util;

import com.badskater0729.worldwidechat.WorldwideChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

public class FoliaCommonRefs extends CommonRefs {

    private WorldwideChat main = WorldwideChat.instance;

    @Override
    public void sendMsg(CommandSender sender, TextComponent originalMessage) {
        try {
            final TextComponent outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                    .append(Component.text().content(" "))
                    .append(originalMessage.asComponent())
                    .build();
            sender.sendMessage(outMessage);
        } catch (IllegalStateException e) {}
    }

}
