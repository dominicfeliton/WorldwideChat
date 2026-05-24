package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType;
import com.dominicfeliton.worldwidechat.util.CommonTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;

final class FoliaSchedulerDispatcher {
    private static final long MILLIS_PER_TICK = 50L;

    private final FoliaSchedulerAccess access;

    FoliaSchedulerDispatcher(FoliaSchedulerAccess access) {
        this.access = access;
    }

    CommonTask dispatch(SchedulerType schedulerType, Runnable task, Object[] taskObjs, long delay, long period) {
        switch (schedulerType) {
            case GLOBAL:
                return dispatchGlobal(task, delay, period);
            case REGION:
                return dispatchRegion(task, taskObjs, delay, period);
            case ENTITY:
                return dispatchEntity(task, taskObjs, delay, period);
            case ASYNC:
                return dispatchAsync(task, delay, period);
            default:
                return null;
        }
    }

    private CommonTask dispatchGlobal(Runnable task, long delay, long period) {
        if (period > 0) {
            return access.runGlobalAtFixedRate(task, delay, period);
        }
        if (delay > 0) {
            return access.runGlobalDelayed(task, delay);
        }
        return access.runGlobal(task);
    }

    private CommonTask dispatchRegion(Runnable task, Object[] taskObjs, long delay, long period) {
        if (taskObjs == null || taskObjs.length == 0) {
            access.severe("Requested region scheduler but did not pass a location/world! Please contact the dev.");
            return null;
        }

        if (taskObjs[0] instanceof Location location) {
            return dispatchRegionLocation(location, task, delay, period);
        }
        if (taskObjs[0] instanceof World world) {
            if (taskObjs.length < 3 || !(taskObjs[1] instanceof Integer chunkX) || !(taskObjs[2] instanceof Integer chunkZ)) {
                access.severe("Requested region scheduler and passed a world but did not pass chunkX/Z! Please contact the dev.");
                return null;
            }
            return dispatchRegionWorld(world, chunkX, chunkZ, task, delay, period);
        }

        access.severe("Requested region scheduler but did not pass a location/world! Please contact the dev.");
        return null;
    }

    private CommonTask dispatchRegionLocation(Location location, Runnable task, long delay, long period) {
        if (period > 0) {
            return access.runRegionAtFixedRate(location, task, delay, period);
        }
        if (delay > 0) {
            return access.runRegionDelayed(location, task, delay);
        }
        return access.runRegion(location, task);
    }

    private CommonTask dispatchRegionWorld(World world, int chunkX, int chunkZ, Runnable task, long delay, long period) {
        if (period > 0) {
            return access.runRegionAtFixedRate(world, chunkX, chunkZ, task, delay, period);
        }
        if (delay > 0) {
            return access.runRegionDelayed(world, chunkX, chunkZ, task, delay);
        }
        return access.runRegion(world, chunkX, chunkZ, task);
    }

    private CommonTask dispatchEntity(Runnable task, Object[] taskObjs, long delay, long period) {
        if (taskObjs == null || taskObjs.length == 0 || !(taskObjs[0] instanceof Entity entity)) {
            access.severe("Requested entity scheduler but did not pass an entity! Please contact the dev.");
            return null;
        }

        if (period > 0) {
            return access.runEntityAtFixedRate(entity, task, delay, period);
        }
        if (delay > 0) {
            return access.runEntityDelayed(entity, task, delay);
        }
        return access.runEntity(entity, task);
    }

    private CommonTask dispatchAsync(Runnable task, long delay, long period) {
        if (period > 0) {
            return access.runAsyncAtFixedRate(task, ticksToMillis(delay), ticksToMillis(period), TimeUnit.MILLISECONDS);
        }
        if (delay > 0) {
            return access.runAsyncDelayed(task, ticksToMillis(delay), TimeUnit.MILLISECONDS);
        }
        return access.runAsyncNow(task);
    }

    private long ticksToMillis(long ticks) {
        return ticks * MILLIS_PER_TICK;
    }
}
