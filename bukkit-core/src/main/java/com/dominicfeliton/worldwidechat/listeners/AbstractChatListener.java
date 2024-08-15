package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    public Component getVaultMessage(Player eventPlayer, String message, String name) {
        return getVaultMessage(eventPlayer, refs.deserial(message), refs.deserial(name));
    }

    // To provide one common method for spigot/paper/folia/etc.
    // We assume that Vault is functional and has a valid ChatProvider set
    public Component getVaultMessage(Player eventPlayer, Component message, Component name) {
        Chat chat = main.getChat();

        refs.debugMsg(chat.getPlayerPrefix(eventPlayer));
        refs.debugMsg(chat.getPlayerSuffix(eventPlayer));

        return main.getTranslateLayout(chat.getPlayerPrefix(eventPlayer), refs.serial(name), chat.getPlayerSuffix(eventPlayer), eventPlayer)
                .append(Component.space())
                .append(message);
    }

}
