package com.dominicfeliton.worldwidechat.input;

import org.bukkit.entity.Player;

public class InputContext {
    private final Player player;

    public InputContext(Player player) {
        this.player = player;
    }

    public Player getForWhom() {
        return player;
    }

    public Player getPlayer() {
        return player;
    }
}
