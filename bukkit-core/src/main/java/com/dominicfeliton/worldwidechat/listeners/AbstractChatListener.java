package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

    public Component getVaultMessage(Player eventPlayer, Component message, Component displayName) {
        return getVaultMessage(null, eventPlayer, message, displayName);
    }
    // To provide one common method for spigot/paper/folia/etc.
    // We assume that Vault is functional and has a valid ChatProvider set
    public Component getVaultMessage(Player targetPlayer, Player eventPlayer, Component message, Component displayName) {
        if (targetPlayer == null) {
            targetPlayer = eventPlayer;
            refs.debugMsg("Translating outgoing message!");
        } else {
            refs.debugMsg("Translating incoming message!");
        }
        Chat chat = main.getChat();

        refs.debugMsg("Generating message for " + targetPlayer.getName() + " w/ Vault info...");
        refs.debugMsg(chat.getPlayerPrefix(eventPlayer));
        refs.debugMsg(chat.getPlayerSuffix(eventPlayer));
        PluginManager man = main.getServer().getPluginManager();
        TextComponent icon = main.getTranslateIcon() == null ? Component.empty() : main.getTranslateIcon();
        Component outMsg;

        if (man.getPlugin("EssentialsChat") != null) {
            // EssX makes displayName include prefix/suffix...
            refs.debugMsg("EssentialsChat!");
            outMsg = Component.empty()
                    .append(icon)
                    .append(displayName)
                    .append(Component.text(":"))
                    .append(Component.space())
                    .append(message);
        } else {
            refs.debugMsg("Default!");
            outMsg = Component.empty()
                    .append(icon)
                    .append(Component.text(chat.getPlayerPrefix(eventPlayer)))
                    .append(displayName)
                    .append(Component.text(chat.getPlayerSuffix(eventPlayer)))
                    .append(Component.text(":"))
                    .append(Component.space())
                    .append(message);
        }

        return outMsg;
    }

    public Component getVaultMessage(Player targetPlayer, Player eventPlayer, String message, String displayName) {
        return getVaultMessage(targetPlayer, eventPlayer, refs.deserial(message), refs.deserial(displayName));
    }

}
