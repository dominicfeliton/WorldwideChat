package com.dominicfeliton.worldwidechat.listeners;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public abstract class AbstractChatListener<T extends Event> implements Listener {

    protected WorldwideChat main = WorldwideChat.instance;
    protected CommonRefs refs = main.getServerFactory().getCommonRefs();

    protected static final EventPriority priority = WorldwideChat.instance.getChatPriority();

    @EventHandler
    public abstract void onPlayerChat(T event);
}
