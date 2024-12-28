package com.dominicfeliton.worldwidechat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

public class FoliaCommonRefs extends CommonRefs {

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage, boolean addPrefix) {
        try {
            final TextComponent outMessage;

            if (addPrefix) {
                outMessage = Component.text().append(main.getPluginPrefix().asComponent())
                        .append(Component.space())
                        .append(originalMessage.asComponent())
                        .build();
            } else {
                outMessage = Component.text().append(originalMessage.asComponent())
                        .build();
            }
            sender.sendMessage(outMessage);
        } catch (IllegalStateException e) {
        }
    }

    @Override
    public void sendMsg(CommandSender sender, Component originalMessage) {
        sendMsg(sender, originalMessage, true);
    }

    /**
     * Checks if the server is stopping or reloading, by attempting to register a scheduler task.
     * This will throw an IllegalPluginAccessException if we are on Bukkit or one of its derivatives.
     *
     * @return Boolean - Whether the server is reloading/stopping or not
     */
    @Override
    public boolean serverIsStopping() {
        boolean stopping = !main.isEnabled() && main.getServer().isStopping();
        debugMsg("Folia stop check: " + stopping);
        return stopping;
    }

}
