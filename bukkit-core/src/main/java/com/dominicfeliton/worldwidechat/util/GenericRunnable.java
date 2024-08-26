package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GenericRunnable extends BukkitRunnable implements Cloneable {

    private volatile boolean isCancelled = false;

    @Override
    public void run() {
        if (isCancelled) {
            WorldwideChat.instance.getLogger().info("Cancelling!!! Hip hip");
            return;
        }

        execute();
    }

    public void cancel() {
        isCancelled = true;
    }

    protected abstract void execute();  // User-defined task logic

    @Override
    public GenericRunnable clone() {
        try {
            GenericRunnable cloned = (GenericRunnable) super.clone();
            cloned.isCancelled = false;
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
