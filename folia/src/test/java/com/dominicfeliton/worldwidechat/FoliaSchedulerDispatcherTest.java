package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType;
import com.dominicfeliton.worldwidechat.util.CommonTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FoliaSchedulerDispatcherTest {

    @Test
    void asyncImmediateUsesRunNow() {
        RecordingSchedulerAccess access = new RecordingSchedulerAccess();

        CommonTask task = dispatch(access, SchedulerType.ASYNC, null, 0, 0);

        assertSame(access.task, task);
        assertEquals("asyncNow", access.method);
    }

    @Test
    void asyncDelayedConvertsTicksToMilliseconds() {
        long[][] cases = new long[][]{
                {1, 50},
                {19, 950},
                {20, 1000}
        };

        for (long[] currentCase : cases) {
            RecordingSchedulerAccess access = new RecordingSchedulerAccess();

            CommonTask task = dispatch(access, SchedulerType.ASYNC, null, currentCase[0], 0);

            assertSame(access.task, task);
            assertEquals("asyncDelayed", access.method);
            assertEquals(currentCase[1], access.delay);
            assertEquals(TimeUnit.MILLISECONDS, access.unit);
        }
    }

    @Test
    void asyncRepeatingConvertsDelayAndPeriodToMilliseconds() {
        RecordingSchedulerAccess access = new RecordingSchedulerAccess();

        CommonTask task = dispatch(access, SchedulerType.ASYNC, null, 3, 7);

        assertSame(access.task, task);
        assertEquals("asyncFixed", access.method);
        assertEquals(150, access.delay);
        assertEquals(350, access.period);
        assertEquals(TimeUnit.MILLISECONDS, access.unit);
    }

    @Test
    void startupAsyncRepeatingTaskShapesConvertTicksToMilliseconds() {
        RecordingSchedulerAccess userDataSync = new RecordingSchedulerAccess();
        RecordingSchedulerAccess updateChecker = new RecordingSchedulerAccess();

        dispatch(userDataSync, SchedulerType.ASYNC, null, 144000, 144000);
        dispatch(updateChecker, SchedulerType.ASYNC, null, 0, 1728000);

        assertEquals("asyncFixed", userDataSync.method);
        assertEquals(7200000, userDataSync.delay);
        assertEquals(7200000, userDataSync.period);
        assertEquals(TimeUnit.MILLISECONDS, userDataSync.unit);
        assertEquals("asyncFixed", updateChecker.method);
        assertEquals(0, updateChecker.delay);
        assertEquals(86400000, updateChecker.period);
        assertEquals(TimeUnit.MILLISECONDS, updateChecker.unit);
    }

    @Test
    void immediateMessageDeliveryShapesUseEntityAndGlobalSchedulers() {
        Entity playerSender = proxy(Entity.class);
        RecordingSchedulerAccess playerMessage = new RecordingSchedulerAccess();
        RecordingSchedulerAccess uuidMessageLookup = new RecordingSchedulerAccess();

        dispatch(playerMessage, SchedulerType.ENTITY, new Object[]{playerSender}, 0, 0);
        dispatch(uuidMessageLookup, SchedulerType.GLOBAL, null, 0, 0);

        assertEquals("entityRun", playerMessage.method);
        assertSame(playerSender, playerMessage.entity);
        assertEquals("globalRun", uuidMessageLookup.method);
    }

    @Test
    void globalSchedulerKeepsTickValues() {
        RecordingSchedulerAccess delayed = new RecordingSchedulerAccess();
        RecordingSchedulerAccess repeating = new RecordingSchedulerAccess();

        dispatch(delayed, SchedulerType.GLOBAL, null, 9, 0);
        dispatch(repeating, SchedulerType.GLOBAL, null, 9, 11);

        assertEquals("globalDelayed", delayed.method);
        assertEquals(9, delayed.delay);
        assertEquals("globalFixed", repeating.method);
        assertEquals(9, repeating.delay);
        assertEquals(11, repeating.period);
    }

    @Test
    void regionSchedulerSupportsLocationAndWorldChunkAnchors() {
        World world = proxy(World.class);
        Location location = new Location(world, 1, 2, 3);
        RecordingSchedulerAccess locationAccess = new RecordingSchedulerAccess();
        RecordingSchedulerAccess worldAccess = new RecordingSchedulerAccess();

        dispatch(locationAccess, SchedulerType.REGION, new Object[]{location}, 4, 6);
        dispatch(worldAccess, SchedulerType.REGION, new Object[]{world, 12, 34}, 5, 7);

        assertEquals("regionLocationFixed", locationAccess.method);
        assertSame(location, locationAccess.location);
        assertEquals(4, locationAccess.delay);
        assertEquals(6, locationAccess.period);
        assertEquals("regionWorldFixed", worldAccess.method);
        assertSame(world, worldAccess.world);
        assertEquals(12, worldAccess.chunkX);
        assertEquals(34, worldAccess.chunkZ);
        assertEquals(5, worldAccess.delay);
        assertEquals(7, worldAccess.period);
    }

    @Test
    void regionSchedulerKeepsDelayedTickValuesForAnchors() {
        World world = proxy(World.class);
        Location location = new Location(world, 1, 2, 3);
        RecordingSchedulerAccess locationAccess = new RecordingSchedulerAccess();
        RecordingSchedulerAccess worldAccess = new RecordingSchedulerAccess();

        dispatch(locationAccess, SchedulerType.REGION, new Object[]{location}, 13, 0);
        dispatch(worldAccess, SchedulerType.REGION, new Object[]{world, 12, 34}, 17, 0);

        assertEquals("regionLocationDelayed", locationAccess.method);
        assertSame(location, locationAccess.location);
        assertEquals(13, locationAccess.delay);
        assertEquals("regionWorldDelayed", worldAccess.method);
        assertSame(world, worldAccess.world);
        assertEquals(12, worldAccess.chunkX);
        assertEquals(34, worldAccess.chunkZ);
        assertEquals(17, worldAccess.delay);
    }

    @Test
    void inventoryBookCleanupUsesEntityDelayedTicks() {
        Entity player = proxy(Entity.class);
        RecordingSchedulerAccess access = new RecordingSchedulerAccess();

        dispatch(access, SchedulerType.ENTITY, new Object[]{player}, 6000, 0);

        assertEquals("entityDelayed", access.method);
        assertSame(player, access.entity);
        assertEquals(6000, access.delay);
    }

    @Test
    void translationProgressIndicatorEntityTasksKeepTickValues() {
        Entity player = proxy(Entity.class);
        RecordingSchedulerAccess startDelay = new RecordingSchedulerAccess();
        RecordingSchedulerAccess pulse = new RecordingSchedulerAccess();
        RecordingSchedulerAccess finishNow = new RecordingSchedulerAccess();
        RecordingSchedulerAccess finishClear = new RecordingSchedulerAccess();

        dispatch(startDelay, SchedulerType.ENTITY, new Object[]{player}, 8, 0);
        dispatch(pulse, SchedulerType.ENTITY, new Object[]{player}, 4, 4);
        dispatch(finishNow, SchedulerType.ENTITY, new Object[]{player}, 0, 0);
        dispatch(finishClear, SchedulerType.ENTITY, new Object[]{player}, 15, 0);

        assertEquals("entityDelayed", startDelay.method);
        assertEquals(8, startDelay.delay);
        assertEquals("entityFixed", pulse.method);
        assertEquals(4, pulse.delay);
        assertEquals(4, pulse.period);
        assertEquals("entityRun", finishNow.method);
        assertEquals("entityDelayed", finishClear.method);
        assertEquals(15, finishClear.delay);
    }

    @Test
    void entitySchedulerKeepsTickValues() {
        Entity entity = proxy(Entity.class);
        RecordingSchedulerAccess delayed = new RecordingSchedulerAccess();
        RecordingSchedulerAccess repeating = new RecordingSchedulerAccess();

        dispatch(delayed, SchedulerType.ENTITY, new Object[]{entity}, 8, 0);
        dispatch(repeating, SchedulerType.ENTITY, new Object[]{entity}, 8, 10);

        assertEquals("entityDelayed", delayed.method);
        assertSame(entity, delayed.entity);
        assertEquals(8, delayed.delay);
        assertEquals("entityFixed", repeating.method);
        assertSame(entity, repeating.entity);
        assertEquals(8, repeating.delay);
        assertEquals(10, repeating.period);
    }

    @Test
    void invalidRegionArgumentsReturnNoTaskAndLogSevereMessage() {
        RecordingSchedulerAccess missingAnchor = new RecordingSchedulerAccess();
        RecordingSchedulerAccess wrongAnchor = new RecordingSchedulerAccess();
        RecordingSchedulerAccess missingChunk = new RecordingSchedulerAccess();

        assertNull(dispatch(missingAnchor, SchedulerType.REGION, null, 0, 0));
        assertNull(dispatch(wrongAnchor, SchedulerType.REGION, new Object[]{"bad"}, 0, 0));
        assertNull(dispatch(missingChunk, SchedulerType.REGION, new Object[]{proxy(World.class), 1}, 0, 0));

        assertTrue(missingAnchor.severeMessages.getFirst().contains("location/world"));
        assertTrue(wrongAnchor.severeMessages.getFirst().contains("location/world"));
        assertTrue(missingChunk.severeMessages.getFirst().contains("chunkX/Z"));
    }

    @Test
    void invalidEntityArgumentsReturnNoTaskAndLogSevereMessage() {
        RecordingSchedulerAccess access = new RecordingSchedulerAccess();

        assertNull(dispatch(access, SchedulerType.ENTITY, new Object[]{proxy(World.class)}, 0, 0));

        assertTrue(access.severeMessages.getFirst().contains("entity"));
    }

    @Test
    void nullableEntitySchedulerResultReturnsNoTask() {
        RecordingSchedulerAccess access = new RecordingSchedulerAccess();
        access.returnNull = true;

        CommonTask task = dispatch(access, SchedulerType.ENTITY, new Object[]{proxy(Entity.class)}, 0, 0);

        assertNull(task);
        assertEquals("entityRun", access.method);
    }

    private CommonTask dispatch(RecordingSchedulerAccess access, SchedulerType schedulerType, Object[] taskObjs,
                                long delay, long period) {
        return new FoliaSchedulerDispatcher(access).dispatch(schedulerType, () -> {
        }, taskObjs, delay, period);
    }

    private static <T> T proxy(Class<T> type) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> defaultValue(method.getReturnType())));
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        if (returnType == char.class) return '\0';
        return null;
    }

    private static final class RecordingSchedulerAccess implements FoliaSchedulerAccess {
        private final CommonTask task = new FakeTask();
        private final List<String> severeMessages = new ArrayList<>();

        private boolean returnNull;
        private String method;
        private long delay;
        private long period;
        private TimeUnit unit;
        private Location location;
        private World world;
        private int chunkX;
        private int chunkZ;
        private Entity entity;

        @Override
        public CommonTask runGlobal(Runnable task) {
            return record("globalRun");
        }

        @Override
        public CommonTask runGlobalDelayed(Runnable task, long delayTicks) {
            this.delay = delayTicks;
            return record("globalDelayed");
        }

        @Override
        public CommonTask runGlobalAtFixedRate(Runnable task, long initialDelayTicks, long periodTicks) {
            this.delay = initialDelayTicks;
            this.period = periodTicks;
            return record("globalFixed");
        }

        @Override
        public CommonTask runRegion(Location location, Runnable task) {
            this.location = location;
            return record("regionLocationRun");
        }

        @Override
        public CommonTask runRegionDelayed(Location location, Runnable task, long delayTicks) {
            this.location = location;
            this.delay = delayTicks;
            return record("regionLocationDelayed");
        }

        @Override
        public CommonTask runRegionAtFixedRate(Location location, Runnable task, long initialDelayTicks, long periodTicks) {
            this.location = location;
            this.delay = initialDelayTicks;
            this.period = periodTicks;
            return record("regionLocationFixed");
        }

        @Override
        public CommonTask runRegion(World world, int chunkX, int chunkZ, Runnable task) {
            recordWorld(world, chunkX, chunkZ);
            return record("regionWorldRun");
        }

        @Override
        public CommonTask runRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
            recordWorld(world, chunkX, chunkZ);
            this.delay = delayTicks;
            return record("regionWorldDelayed");
        }

        @Override
        public CommonTask runRegionAtFixedRate(World world, int chunkX, int chunkZ, Runnable task,
                                               long initialDelayTicks, long periodTicks) {
            recordWorld(world, chunkX, chunkZ);
            this.delay = initialDelayTicks;
            this.period = periodTicks;
            return record("regionWorldFixed");
        }

        @Override
        public CommonTask runEntity(Entity entity, Runnable task) {
            this.entity = entity;
            return record("entityRun");
        }

        @Override
        public CommonTask runEntityDelayed(Entity entity, Runnable task, long delayTicks) {
            this.entity = entity;
            this.delay = delayTicks;
            return record("entityDelayed");
        }

        @Override
        public CommonTask runEntityAtFixedRate(Entity entity, Runnable task, long initialDelayTicks, long periodTicks) {
            this.entity = entity;
            this.delay = initialDelayTicks;
            this.period = periodTicks;
            return record("entityFixed");
        }

        @Override
        public CommonTask runAsyncNow(Runnable task) {
            return record("asyncNow");
        }

        @Override
        public CommonTask runAsyncDelayed(Runnable task, long delay, TimeUnit unit) {
            this.delay = delay;
            this.unit = unit;
            return record("asyncDelayed");
        }

        @Override
        public CommonTask runAsyncAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            this.delay = initialDelay;
            this.period = period;
            this.unit = unit;
            return record("asyncFixed");
        }

        @Override
        public void severe(String message) {
            severeMessages.add(message);
        }

        private CommonTask record(String method) {
            this.method = method;
            return returnNull ? null : task;
        }

        private void recordWorld(World world, int chunkX, int chunkZ) {
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    private static final class FakeTask implements CommonTask {
        private boolean cancelled;

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public int getTaskId() {
            return 1;
        }

        @Override
        public Object getUnderlyingTask() {
            return this;
        }
    }
}
