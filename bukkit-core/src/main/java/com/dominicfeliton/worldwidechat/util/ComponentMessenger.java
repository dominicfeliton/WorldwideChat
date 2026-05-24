package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public interface ComponentMessenger {
    void sendMessage(CommandSender sender, Component message);

    void sendActionBar(CommandSender sender, Component message);
}
