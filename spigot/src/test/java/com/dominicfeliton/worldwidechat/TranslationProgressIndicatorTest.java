package com.dominicfeliton.worldwidechat;

import com.dominicfeliton.worldwidechat.util.TranslationProgressIndicator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationProgressIndicatorTest extends WWCIntegrationTest {

    @Test
    void fastTranslationFinishesBeforeDelayAndSendsNoActionBar() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("FastIndicator");
        player.performCommand("wwct en es");

        String translated = plugin().getServerFactory().getCommonRefs()
                .translateText("Hello, how are you?", player);
        WWCTestSupport.server().getScheduler().performTicks(20);

        assertEquals("Hola, como estas?", translated);
        assertTrue(drainActionBars(player).isEmpty());
        assertFalse(indicator.isTracking(player));
    }

    @Test
    void openHandleShowsSpinnerAfterDelay() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("SlowIndicator");
        TranslationProgressIndicator.Handle handle = indicator.begin(player);

        try {
            WWCTestSupport.server().getScheduler().performTicks(9);

            List<String> actionBars = drainActionBars(player);
            assertTrue(actionBars.stream().anyMatch(message -> message.contains("Translating message")));
            assertTrue(indicator.isTracking(player));
        } finally {
            handle.close();
            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
        }
    }

    @Test
    void customStatusHandleShowsObjectSpinnerAndFinishMessage() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("ObjectIndicator");
        TranslationProgressIndicator.Handle handle = indicator.begin(player, Component.text("Translating target sign..."));

        try {
            WWCTestSupport.server().getScheduler().performTicks(9);

            List<String> loadingBars = drainActionBars(player);
            assertTrue(loadingBars.stream().anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(loadingBars.stream().noneMatch(message -> message.contains("Translating message")));

            handle.close(Component.text("Sign translated successfully."));
            WWCTestSupport.server().getScheduler().performTicks(1);

            List<String> finishBars = drainActionBars(player);
            assertTrue(finishBars.stream().anyMatch(message -> message.contains("Sign translated successfully")));
            assertTrue(finishBars.stream().noneMatch(message -> message.contains("Done!")));
        } finally {
            handle.close();
            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
        }
    }

    @Test
    void immediateStatusHandleShowsObjectSpinnerWithoutDelay() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("ImmediateObjectIndicator");
        TranslationProgressIndicator.Handle handle = indicator.beginImmediately(player, Component.text("Translating target sign..."));

        try {
            List<String> loadingBars = drainActionBars(player);
            assertTrue(loadingBars.stream().anyMatch(message -> message.contains("Translating target sign")));
            assertTrue(loadingBars.stream().noneMatch(message -> message.contains("Translating message")));
            assertTrue(indicator.isTracking(player));
        } finally {
            handle.close();
            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
        }
    }

    @Test
    void customStatusFinishShowsEvenBeforeSpinnerDelay() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("FastObjectIndicator");
        TranslationProgressIndicator.Handle handle = indicator.begin(player, Component.text("Translating target sign..."));

        handle.close(Component.text("Sign translated successfully."));
        WWCTestSupport.server().getScheduler().performTicks(1);

        List<String> finishBars = drainActionBars(player);
        assertTrue(finishBars.stream().anyMatch(message -> message.contains("Sign translated successfully")));
        assertTrue(indicator.isTracking(player));
        WWCTestSupport.server().getScheduler().performTicks(20);
        drainActionBars(player);
        assertFalse(indicator.isTracking(player));
    }

    @Test
    void overlappingHandlesShareSpinnerUntilAllClose() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("OverlapIndicator");
        TranslationProgressIndicator.Handle first = indicator.begin(player);
        TranslationProgressIndicator.Handle second = indicator.begin(player);

        try {
            WWCTestSupport.server().getScheduler().performTicks(9);
            assertFalse(drainActionBars(player).isEmpty());

            first.close();
            WWCTestSupport.server().getScheduler().performTicks(10);

            assertTrue(drainActionBars(player).stream().noneMatch(message -> message.contains("Done!")));
            assertTrue(indicator.isTracking(player));

            second.close();
            WWCTestSupport.server().getScheduler().performTicks(1);

            assertTrue(drainActionBars(player).stream().anyMatch(message -> message.contains("Done!")));
        } finally {
            first.close();
            second.close();
            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
        }
    }

    @Test
    void disabledActionBarSkipsIndicator() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("DisabledIndicator");
        plugin().setSendActionBar(false);
        plugin().getConfigManager().getMainConfig().set("Chat.sendActionBar", false);

        TranslationProgressIndicator.Handle handle = indicator.begin(player);
        try {
            WWCTestSupport.server().getScheduler().performTicks(20);

            assertTrue(drainActionBars(player).isEmpty());
            assertFalse(indicator.isTracking(player));
        } finally {
            handle.close();
        }
    }

    @Test
    void visibleErrorShowsRedErrorInsteadOfDone() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("FailedIndicator");
        TranslationProgressIndicator.Handle handle = indicator.begin(player);
        WWCTestSupport.server().getScheduler().performTicks(9);
        drainActionBars(player);

        indicator.markError(player);
        handle.close();
        WWCTestSupport.server().getScheduler().performTicks(1);

        List<String> actionBars = drainActionBars(player);
        assertTrue(actionBars.stream().anyMatch(message ->
                message.contains(ChatColor.RED.toString()) && message.contains("Error")));
        assertTrue(actionBars.stream().noneMatch(message -> message.contains("Done!")));

        WWCTestSupport.server().getScheduler().performTicks(20);
        drainActionBars(player);
        assertFalse(indicator.isTracking(player));
    }

    @Test
    void closingWithoutErrorMarkClearsVisibleIndicator() {
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("ErrorIndicator");
        TranslationProgressIndicator.Handle handle = indicator.begin(player);
        WWCTestSupport.server().getScheduler().performTicks(9);
        drainActionBars(player);

        try {
            throw new IllegalStateException("simulated translation failure");
        } catch (IllegalStateException expected) {
        } finally {
            handle.close();
        }

        WWCTestSupport.server().getScheduler().performTicks(20);

        assertTrue(drainActionBars(player).stream().anyMatch(message -> message.contains("Done!")));
        assertFalse(indicator.isTracking(player));
    }

    @Test
    void indicatorHardTimeoutShowsErrorAndClears() {
        int originalFatalAbort = WorldwideChat.translatorFatalAbortSeconds;
        WorldwideChat.translatorFatalAbortSeconds = 1;
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("HardTimeoutIndicator");
        TranslationProgressIndicator.Handle handle = indicator.beginImmediately(player, Component.text("Translating target sign..."));
        drainActionBars(player);

        try {
            WWCTestSupport.server().getScheduler().performTicks(39);
            assertTrue(indicator.isTracking(player));
            assertTrue(drainActionBars(player).stream().noneMatch(message -> message.contains("Error")));

            WWCTestSupport.server().getScheduler().performTicks(2);
            assertTrue(drainActionBars(player).stream().anyMatch(message -> message.contains("Error")));
            assertTrue(indicator.isTracking(player));

            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
            assertFalse(indicator.isTracking(player));
        } finally {
            handle.close();
            WorldwideChat.translatorFatalAbortSeconds = originalFatalAbort;
        }
    }

    @Test
    void closingBeforeHardTimeoutCancelsTimeoutTask() {
        int originalFatalAbort = WorldwideChat.translatorFatalAbortSeconds;
        WorldwideChat.translatorFatalAbortSeconds = 1;
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("HardTimeoutClosedIndicator");
        TranslationProgressIndicator.Handle handle = indicator.beginImmediately(player, Component.text("Translating target sign..."));
        drainActionBars(player);

        try {
            handle.close(Component.text("Sign translated successfully."));
            WWCTestSupport.server().getScheduler().performTicks(1);
            assertTrue(drainActionBars(player).stream().anyMatch(message -> message.contains("Sign translated successfully")));

            WWCTestSupport.server().getScheduler().performTicks(50);
            assertTrue(drainActionBars(player).stream().noneMatch(message -> message.contains("Error")));
            assertFalse(indicator.isTracking(player));
        } finally {
            handle.close();
            WorldwideChat.translatorFatalAbortSeconds = originalFatalAbort;
        }
    }

    @Test
    void overlappingHandlesHardTimeoutTogether() {
        int originalFatalAbort = WorldwideChat.translatorFatalAbortSeconds;
        WorldwideChat.translatorFatalAbortSeconds = 1;
        TranslationProgressIndicator indicator = installRecordingIndicator();
        PlayerMock player = WWCTestSupport.addOpPlayer("HardTimeoutOverlapIndicator");
        TranslationProgressIndicator.Handle first = indicator.beginImmediately(player, Component.text("Translating target sign..."));
        TranslationProgressIndicator.Handle second = indicator.begin(player);
        drainActionBars(player);

        try {
            WWCTestSupport.server().getScheduler().performTicks(42);
            assertTrue(drainActionBars(player).stream().anyMatch(message -> message.contains("Error")));

            first.close();
            second.close();
            WWCTestSupport.server().getScheduler().performTicks(20);
            drainActionBars(player);
            assertFalse(indicator.isTracking(player));
        } finally {
            first.close();
            second.close();
            WorldwideChat.translatorFatalAbortSeconds = originalFatalAbort;
        }
    }

    private TranslationProgressIndicator installRecordingIndicator() {
        TranslationProgressIndicator indicator = new TranslationProgressIndicator(
                plugin(),
                plugin().getServerFactory().getCommonRefs(),
                new RecordingActionBarHelper());
        try {
            Field field = WorldwideChat.class.getDeclaredField("translationProgressIndicator");
            field.setAccessible(true);
            field.set(plugin(), indicator);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return indicator;
    }

    private List<String> drainActionBars(PlayerMock player) {
        List<String> actionBars = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Component actionBar = player.nextActionBar();
            if (actionBar == null) {
                return actionBars;
            }
            actionBars.add(PlainTextComponentSerializer.plainText().serialize(actionBar));
        }
        fail("Player still had queued action bars after draining 50 entries.");
        return actionBars;
    }

    private static final class RecordingActionBarHelper extends SpigotWorldwideChatHelper {
        @Override
        public void sendActionBar(Component message, CommandSender sender) {
            if (sender instanceof Player player) {
                player.sendActionBar(message);
            }
        }
    }
}
