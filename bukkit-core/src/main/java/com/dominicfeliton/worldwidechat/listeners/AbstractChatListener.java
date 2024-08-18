package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public abstract class AbstractChatListener<T extends Event> implements Listener {

    private WorldwideChat main = WorldwideChat.instance;
    private CommonRefs refs = main.getServerFactory().getCommonRefs();

    protected static final EventPriority priority = WorldwideChat.instance.getChatPriority();

    @EventHandler
    public abstract void onPlayerChat(T event);

    public Component getVaultMessage(Player eventPlayer, String message, String name) {
        return getVaultMessage(eventPlayer, refs.deserial(message), refs.deserial(name));
    }

    // To provide one common method for spigot/paper/folia/etc.
    // We assume that Vault is functional and has a valid ChatProvider set
    // name field is for compatibility with spigot AND paper
    public Component getVaultMessage(Player eventPlayer, Component message, Component name) {
        Chat chat = main.getChat();
        if (chat != null) {
            return main.getTranslateFormat(chat.getPlayerPrefix(eventPlayer), refs.serial(name), chat.getPlayerSuffix(eventPlayer), eventPlayer)
                    .append(Component.space())
                    .append(message);
        } else {
            return main.getTranslateFormat("", refs.serial(name), "", eventPlayer)
                    .append(Component.space())
                    .append(message);
        }
    }

    public Component getVaultHoverMessage(Player eventPlayer, Component message, Component name, Player targetPlayer) {
        Chat chat = main.getChat();
        if (chat != null) {
            return main.getTranslateHoverFormat(chat.getPlayerPrefix(eventPlayer), refs.serial(name), chat.getPlayerSuffix(eventPlayer), eventPlayer, targetPlayer)
                    .append(Component.space())
                    .append(message);
        } else {
            return main.getTranslateHoverFormat("", refs.serial(name), "", eventPlayer, targetPlayer)
                    .append(Component.space())
                    .append(message);
        }
    }
}
