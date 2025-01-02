package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.TestCommon;
import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.bukkit.event.HandlerList;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Performance {

    private WorldwideChat plugin;
    private PlayerMock p1, p2;
    private CommonRefs refs;
    private ServerMock server;

    public Performance(PlayerMock p1, PlayerMock p2, WorldwideChat plugin, ServerMock server) {
        this.p1 = p1;
        this.p2 = p2;
        this.plugin = plugin;
        this.server = server;
        this.refs = plugin.getServerFactory().getCommonRefs();
    }

    public void testListenerRegistration() {
        int size = HandlerList.getRegisteredListeners(plugin).size();
        TestCommon.reload(server, plugin);
        server.getScheduler().waitAsyncEventsFinished();
        server.getScheduler().waitAsyncTasksFinished();
        assertNotEquals(size, HandlerList.getRegisteredListeners(plugin).size());
    }

}
