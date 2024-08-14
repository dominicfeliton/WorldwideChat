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

    public Component getVaultMessage(Player eventPlayer, String message, String displayName) {
        return getVaultMessage(eventPlayer, refs.deserial(message), refs.deserial(displayName));
    }

    // To provide one common method for spigot/paper/folia/etc.
    // We assume that Vault is functional and has a valid ChatProvider set
    public Component getVaultMessage(Player eventPlayer, Component message, Component displayName) {
        Chat chat = main.getChat();

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
            outMsg = getDefaultVaultMessage(eventPlayer, chat, displayName, icon, message);
        }

        return outMsg;
    }

    private Component getDefaultVaultMessage(Player eventPlayer, Chat chat, Component displayName, Component icon, Component message) {
        refs.debugMsg("Default!");
        return Component.empty()
                .append(icon)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        chat.getPlayerPrefix(eventPlayer)))
                .append(displayName)
                .append(LegacyComponentSerializer.legacyAmpersand().deserialize(
                        chat.getPlayerSuffix(eventPlayer)))
                .append(Component.text(":"))
                .append(Component.space())
                .append(message);
    }

}
