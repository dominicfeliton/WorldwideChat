package com.dominicfeliton.worldwidechat.util;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.WorldwideChatHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dominicfeliton.worldwidechat.WorldwideChatHelper.SchedulerType.ENTITY;

public class TranslationProgressIndicator {

    private static final int START_DELAY_TICKS = 8;
    private static final int PULSE_INTERVAL_TICKS = 4;
    private static final int FINISH_CLEAR_TICKS = 15;
    private static final String[] SPINNER_FRAMES = {"|", "/", "-", "\\"};

    private final WorldwideChat main;
    private final CommonRefs refs;
    private final WorldwideChatHelper wwcHelper;
    private final ConcurrentHashMap<UUID, PlayerIndicatorState> playerStates = new ConcurrentHashMap<>();

    public TranslationProgressIndicator(WorldwideChat main, CommonRefs refs, WorldwideChatHelper wwcHelper) {
        this.main = main;
        this.refs = refs;
        this.wwcHelper = wwcHelper;
    }

    public Handle begin(Player player) {
        if (!canDisplay(player)) {
            return Handle.noop();
        }

        UUID playerId = player.getUniqueId();
        PlayerIndicatorState state = playerStates.computeIfAbsent(playerId, ignored -> new PlayerIndicatorState());
        synchronized (state) {
            boolean starting = state.activeCount == 0;
            state.activeCount++;

            if (starting) {
                state.generation++;
                cancelTask(state.delayedStartTask);
                cancelTask(state.pulseTask);
                cancelTask(state.clearTask);
                state.visible = false;
                state.frameIndex = 0;
                scheduleDelayedStart(player, playerId, state, state.generation);
            }
        }

        return new Handle(this, playerId, player);
    }

    public boolean isTracking(Player player) {
        return player != null && playerStates.containsKey(player.getUniqueId());
    }

    public void clearAll() {
        for (PlayerIndicatorState state : playerStates.values()) {
            synchronized (state) {
                cancelTask(state.delayedStartTask);
                cancelTask(state.pulseTask);
                cancelTask(state.clearTask);
                state.activeCount = 0;
                state.visible = false;
                state.generation++;
            }
        }
        playerStates.clear();
    }

    private void finish(UUID playerId, Player player) {
        PlayerIndicatorState state = playerStates.get(playerId);
        if (state == null) {
            return;
        }

        synchronized (state) {
            if (state.activeCount > 0) {
                state.activeCount--;
            }

            if (state.activeCount > 0) {
                return;
            }

            cancelTask(state.delayedStartTask);
            state.delayedStartTask = null;
            state.generation++;

            if (!state.visible) {
                playerStates.remove(playerId, state);
                return;
            }

            cancelTask(state.pulseTask);
            state.pulseTask = null;
            state.visible = false;
            scheduleFinish(player, playerId, state, state.generation);
        }
    }

    private void scheduleDelayedStart(Player player, UUID playerId, PlayerIndicatorState state, long generation) {
        GenericRunnable delayedStart = new GenericRunnable() {
            @Override
            protected void execute() {
                PlayerIndicatorState currentState = playerStates.get(playerId);
                if (currentState == null) {
                    return;
                }

                synchronized (currentState) {
                    if (currentState.generation != generation || currentState.activeCount <= 0) {
                        return;
                    }

                    currentState.visible = true;
                    sendFrame(player, currentState);
                    schedulePulse(player, playerId, currentState, generation);
                }
            }
        };
        state.delayedStartTask = delayedStart;
        wwcHelper.runSync(true, START_DELAY_TICKS, delayedStart, ENTITY, new Object[]{player});
    }

    private void schedulePulse(Player player, UUID playerId, PlayerIndicatorState state, long generation) {
        GenericRunnable pulse = new GenericRunnable() {
            @Override
            protected void execute() {
                PlayerIndicatorState currentState = playerStates.get(playerId);
                if (currentState == null) {
                    cancel();
                    return;
                }

                synchronized (currentState) {
                    if (currentState.generation != generation || currentState.activeCount <= 0 || !currentState.visible) {
                        cancel();
                        return;
                    }

                    sendFrame(player, currentState);
                }
            }
        };
        state.pulseTask = pulse;
        wwcHelper.runSyncRepeating(true, PULSE_INTERVAL_TICKS, PULSE_INTERVAL_TICKS, pulse, ENTITY, new Object[]{player});
    }

    private void scheduleFinish(Player player, UUID playerId, PlayerIndicatorState state, long generation) {
        GenericRunnable finish = new GenericRunnable() {
            @Override
            protected void execute() {
                PlayerIndicatorState currentState = playerStates.get(playerId);
                if (currentState == null) {
                    return;
                }

                synchronized (currentState) {
                    if (currentState.generation != generation || currentState.activeCount > 0) {
                        return;
                    }

                    sendActionBar(player, refs.getCompMsg("wwctTranslationFinishActionBar", null, "&o&a", player));
                    scheduleClear(player, playerId, currentState, generation);
                }
            }
        };
        state.clearTask = finish;
        wwcHelper.runSync(true, 0, finish, ENTITY, new Object[]{player});
    }

    private void scheduleClear(Player player, UUID playerId, PlayerIndicatorState state, long generation) {
        GenericRunnable clear = new GenericRunnable() {
            @Override
            protected void execute() {
                PlayerIndicatorState currentState = playerStates.get(playerId);
                if (currentState == null) {
                    return;
                }

                synchronized (currentState) {
                    if (currentState.generation != generation || currentState.activeCount > 0) {
                        return;
                    }

                    sendActionBar(player, Component.empty());
                    playerStates.remove(playerId, currentState);
                }
            }
        };
        state.clearTask = clear;
        wwcHelper.runSync(true, FINISH_CLEAR_TICKS, clear, ENTITY, new Object[]{player});
    }

    private void sendFrame(Player player, PlayerIndicatorState state) {
        String frame = SPINNER_FRAMES[state.frameIndex % SPINNER_FRAMES.length];
        state.frameIndex++;
        Component message = Component.text(frame + " ")
                .append(refs.getCompMsg("wwctTranslationInitActionBar", null, "&o", player));
        sendActionBar(player, message);
    }

    private void sendActionBar(Player player, Component message) {
        if (!canDisplay(player)) {
            return;
        }
        wwcHelper.sendActionBar(message, player);
    }

    private boolean canDisplay(Player player) {
        return player != null
                && player.isOnline()
                && main.isEnabled()
                && main.getConfigManager() != null
                && main.getSendActionBar()
                && main.getConfigManager().getMainConfig().getBoolean("Chat.sendActionBar");
    }

    private void cancelTask(GenericRunnable task) {
        if (task != null) {
            task.cancel();
        }
    }

    public static final class Handle implements AutoCloseable {

        private final TranslationProgressIndicator indicator;
        private final UUID playerId;
        private final Player player;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private Handle(TranslationProgressIndicator indicator, UUID playerId, Player player) {
            this.indicator = indicator;
            this.playerId = playerId;
            this.player = player;
        }

        private static Handle noop() {
            return new Handle(null, null, null);
        }

        @Override
        public void close() {
            if (indicator != null && closed.compareAndSet(false, true)) {
                indicator.finish(playerId, player);
            }
        }
    }

    private static final class PlayerIndicatorState {
        private int activeCount = 0;
        private int frameIndex = 0;
        private boolean visible = false;
        private long generation = 0;
        private GenericRunnable delayedStartTask;
        private GenericRunnable pulseTask;
        private GenericRunnable clearTask;
    }
}
