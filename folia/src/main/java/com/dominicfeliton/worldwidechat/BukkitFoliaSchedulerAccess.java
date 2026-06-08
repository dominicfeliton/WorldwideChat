package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.CommonTask;
import com.dominicfeliton.worldwidechat.util.FoliaTaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class BukkitFoliaSchedulerAccess implements FoliaSchedulerAccess {
    private final WorldwideChat main;

    BukkitFoliaSchedulerAccess(WorldwideChat main) {
        this.main = main;
    }

    @Override
    public CommonTask runGlobal(Runnable task) {
        return wrap(main.getServer().getGlobalRegionScheduler().run(main, convert(task)));
    }

    @Override
    public CommonTask runGlobalDelayed(Runnable task, long delayTicks) {
        return wrap(main.getServer().getGlobalRegionScheduler().runDelayed(main, convert(task), delayTicks));
    }

    @Override
    public CommonTask runGlobalAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks) {
        return wrap(main.getServer().getGlobalRegionScheduler()
                .runAtFixedRate(main, convert(task), initialDelayTicks, periodTicks));
    }

    @Override
    public CommonTask runRegion(Location location, Runnable task) {
        return wrap(main.getServer().getRegionScheduler().run(main, location, convert(task)));
    }

    @Override
    public CommonTask runRegionDelayed(Location location, Runnable task, long delayTicks) {
        return wrap(main.getServer().getRegionScheduler().runDelayed(main, location, convert(task), delayTicks));
    }

    @Override
    public CommonTask runRegionAtFixedRate(Location location, Runnable task, long initialDelayTicks, long periodTicks) {
        return wrap(main.getServer().getRegionScheduler()
                .runAtFixedRate(main, location, convert(task), initialDelayTicks, periodTicks));
    }

    @Override
    public CommonTask runRegion(World world, int chunkX, int chunkZ, Runnable task) {
        return wrap(main.getServer().getRegionScheduler().run(main, world, chunkX, chunkZ, convert(task)));
    }

    @Override
    public CommonTask runRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
        return wrap(main.getServer().getRegionScheduler()
                .runDelayed(main, world, chunkX, chunkZ, convert(task), delayTicks));
    }

    @Override
    public CommonTask runRegionAtFixedRate(World world, int chunkX, int chunkZ, Runnable task, long initialDelayTicks, long periodTicks) {
        return wrap(main.getServer().getRegionScheduler()
                .runAtFixedRate(main, world, chunkX, chunkZ, convert(task), initialDelayTicks, periodTicks));
    }

    @Override
    public CommonTask runEntity(Entity entity, Runnable task) {
        return wrap(entity.getScheduler().run(main, convert(task), null));
    }

    @Override
    public CommonTask runEntityDelayed(Entity entity, Runnable task, long delayTicks) {
        return wrap(entity.getScheduler().runDelayed(main, convert(task), null, delayTicks));
    }

    @Override
    public CommonTask runEntityAtFixedRate(Entity entity, Runnable task, long initialDelayTicks, long periodTicks) {
        return wrap(entity.getScheduler().runAtFixedRate(main, convert(task), null, initialDelayTicks, periodTicks));
    }

    @Override
    public CommonTask runAsyncNow(Runnable task) {
        return wrap(main.getServer().getAsyncScheduler().runNow(main, convert(task)));
    }

    @Override
    public CommonTask runAsyncDelayed(Runnable task, long delay, TimeUnit unit) {
        return wrap(main.getServer().getAsyncScheduler().runDelayed(main, convert(task), delay, unit));
    }

    @Override
    public CommonTask runAsyncAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return wrap(main.getServer().getAsyncScheduler()
                .runAtFixedRate(main, convert(task), initialDelay, period, unit));
    }

    @Override
    public void severe(String message) {
        main.getLogger().severe(message);
    }

    private Consumer<ScheduledTask> convert(Runnable task) {
        return scheduledTask -> task.run();
    }

    private CommonTask wrap(ScheduledTask task) {
        if (task == null) return null;
        return new FoliaTaskWrapper(task);
    }
}
