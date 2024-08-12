package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public abstract class AbstractChatListener<T extends Event> implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    protected static final EventPriority priority = WorldwideChat.instance.getChatPriority();

    @EventHandler
    public abstract void onPlayerChat(T event);

    // To provide one common method for spigot/paper/folia/etc.
    // We assume that Vault is functional and has a valid ChatProvider set
    public Component getVaultMessage(Player targetPlayer, Player eventPlayer, Component message, Component displayName) {
        Chat chat = main.getChat();

        refs.debugMsg("Sending message to " + targetPlayer.getName() + " w/ Vault info...");
        refs.debugMsg(chat.getPlayerPrefix(eventPlayer));
        refs.debugMsg(chat.getPlayerSuffix(eventPlayer));
        PluginManager man = main.getServer().getPluginManager();
        Component outMsg;

        if (man.getPlugin("EssentialsChat") != null) {
            // EssX makes displayName include prefix/suffix...
            refs.debugMsg("EssentialsChat!");
            outMsg = displayName
                    .append(Component.text(":"))
                    .append(Component.space())
                    .append(message)
                    .append(Component.space())
                    .append(Component.text("\uD83C\uDF10", NamedTextColor.LIGHT_PURPLE));
        } else {
            refs.debugMsg("Default!");
            outMsg = Component.text(chat.getPlayerPrefix(eventPlayer))
                    .append(displayName)
                    .append(Component.text(chat.getPlayerSuffix(eventPlayer)))
                    .append(Component.text(":"))
                    .append(Component.space())
                    .append(message)
                    .append(Component.space())
                    .append(Component.text("\uD83C\uDF10", NamedTextColor.LIGHT_PURPLE));
        }

        return outMsg;
    }

    public String getVaultMessage(Player targetPlayer, Player eventPlayer, String message, String displayName) {
        return refs.serial(getVaultMessage(targetPlayer, eventPlayer, refs.deserial(message), refs.deserial(displayName)));
    }

}
