package com.dominicfeliton.worldwidechat.input;

import org.bukkit.entity.Player;

public interface InputService {
    String getActiveBackendName();

    void open(Player player, InputRequest request);
}
