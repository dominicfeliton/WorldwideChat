package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CommonTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;

interface FoliaSchedulerAccess {
    CommonTask runGlobal(Runnable task);

    CommonTask runGlobalDelayed(Runnable task, long delayTicks);

    CommonTask runGlobalAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks);

    CommonTask runRegion(Location location, Runnable task);

    CommonTask runRegionDelayed(Location location, Runnable task, long delayTicks);

    CommonTask runRegionAtFixedRate(Location location, Runnable task, long initialDelayTicks, long periodTicks);

    CommonTask runRegion(World world, int chunkX, int chunkZ, Runnable task);

    CommonTask runRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks);

    CommonTask runRegionAtFixedRate(World world, int chunkX, int chunkZ, Runnable task, long initialDelayTicks, long periodTicks);

    CommonTask runEntity(Entity entity, Runnable task);

    CommonTask runEntityDelayed(Entity entity, Runnable task, long delayTicks);

    CommonTask runEntityAtFixedRate(Entity entity, Runnable task, long initialDelayTicks, long periodTicks);

    CommonTask runAsyncNow(Runnable task);

    CommonTask runAsyncDelayed(Runnable task, long delay, TimeUnit unit);

    CommonTask runAsyncAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    void severe(String message);
}
