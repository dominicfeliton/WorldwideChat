package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ActiveTranslatorTest {

    @Test
    void defaultsRepresentNewTranslationSession() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");

        assertEquals("player-uuid", translator.getUUID());
        assertEquals("en", translator.getInLangCode());
        assertEquals("es", translator.getOutLangCode());
        assertEquals(0, translator.getRateLimit());
        assertEquals("None", translator.getRateLimitPreviousTime());
        assertTrue(translator.getTranslatingChatOutgoing());
        assertFalse(translator.getTranslatingChatIncoming());
        assertFalse(translator.getTranslatingBook());
        assertFalse(translator.getTranslatingSign());
        assertFalse(translator.getTranslatingItem());
        assertFalse(translator.getTranslatingEntity());
        assertFalse(translator.getHasBeenSaved());
    }

    @Test
    void mutatingPersistedFieldsMarksTranslatorUnsaved() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");

        translator.setHasBeenSaved(true);
        translator.setRateLimit(10);
        assertFalse(translator.getHasBeenSaved());
        assertEquals(10, translator.getRateLimit());

        translator.setHasBeenSaved(true);
        translator.setTranslatingBook(true);
        assertFalse(translator.getHasBeenSaved());
        assertTrue(translator.getTranslatingBook());

        translator.setHasBeenSaved(true);
        Instant previousTime = Instant.parse("2026-01-02T03:04:05Z");
        translator.setRateLimitPreviousTime(previousTime);
        assertFalse(translator.getHasBeenSaved());
        assertEquals(previousTime.toString(), translator.getRateLimitPreviousTime());
    }

    @Test
    void rateLimitSlotAllowsFirstAcquireFromNoneAndStoresTimestamp() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        translator.setHasBeenSaved(true);
        Instant now = Instant.parse("2026-01-02T03:04:05Z");

        ActiveTranslator.RateLimitDecision decision = translator.tryAcquireRateLimitSlot(10, now);

        assertTrue(decision.allowed());
        assertEquals(0, decision.secondsRemaining());
        assertEquals(now.toString(), translator.getRateLimitPreviousTime());
        assertFalse(translator.getHasBeenSaved());
    }

    @Test
    void rateLimitSlotBlocksWithinDelayAndReportsRemainingSeconds() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        Instant first = Instant.parse("2026-01-02T03:04:05Z");
        translator.setRateLimitPreviousTime(first);
        translator.setHasBeenSaved(true);

        ActiveTranslator.RateLimitDecision decision = translator.tryAcquireRateLimitSlot(10, first.plusSeconds(4));

        assertFalse(decision.allowed());
        assertEquals(6, decision.secondsRemaining());
        assertEquals(first.toString(), translator.getRateLimitPreviousTime());
        assertTrue(translator.getHasBeenSaved());
    }

    @Test
    void rateLimitSlotAllowsAcquireAfterDelayAndUpdatesTimestamp() {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        Instant first = Instant.parse("2026-01-02T03:04:05Z");
        Instant next = first.plusSeconds(10);
        translator.setRateLimitPreviousTime(first);
        translator.setHasBeenSaved(true);

        ActiveTranslator.RateLimitDecision decision = translator.tryAcquireRateLimitSlot(10, next);

        assertTrue(decision.allowed());
        assertEquals(0, decision.secondsRemaining());
        assertEquals(next.toString(), translator.getRateLimitPreviousTime());
        assertFalse(translator.getHasBeenSaved());
    }

    @Test
    @SuppressWarnings("unchecked")
    void malformedStoredRateLimitTimestampIsTreatedAsAbsent() throws Exception {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        translator.setHasBeenSaved(true);
        var field = ActiveTranslator.class.getDeclaredField("rateLimitPreviousTime");
        field.setAccessible(true);
        ((java.util.concurrent.atomic.AtomicReference<String>) field.get(translator)).set("not-an-instant");
        translator.setHasBeenSaved(true);
        Instant now = Instant.parse("2026-01-02T03:04:05Z");

        ActiveTranslator.RateLimitDecision decision = translator.tryAcquireRateLimitSlot(10, now);

        assertTrue(decision.allowed());
        assertEquals(0, decision.secondsRemaining());
        assertEquals(now.toString(), translator.getRateLimitPreviousTime());
        assertFalse(translator.getHasBeenSaved());
    }

    @Test
    void concurrentRateLimitSlotAcquisitionFromNoneAllowsOneCaller() throws Exception {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        Instant now = Instant.parse("2026-01-02T03:04:05Z");

        assertEquals(1, countConcurrentAllowedAttempts(translator, 8, 10, now));
        assertEquals(now.toString(), translator.getRateLimitPreviousTime());
    }

    @Test
    void concurrentRateLimitSlotAcquisitionFromExpiredTimestampAllowsOneCaller() throws Exception {
        ActiveTranslator translator = new ActiveTranslator("player-uuid", "en", "es");
        Instant previous = Instant.parse("2026-01-02T03:04:05Z");
        Instant now = previous.plusSeconds(10);
        translator.setRateLimitPreviousTime(previous);

        assertEquals(1, countConcurrentAllowedAttempts(translator, 8, 10, now));
        assertEquals(now.toString(), translator.getRateLimitPreviousTime());
    }

    private long countConcurrentAllowedAttempts(ActiveTranslator translator,
                                                int attemptCount,
                                                int delaySeconds,
                                                Instant now) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(attemptCount);
        CountDownLatch ready = new CountDownLatch(attemptCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<ActiveTranslator.RateLimitDecision>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < attemptCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(2, TimeUnit.SECONDS));
                    return translator.tryAcquireRateLimitSlot(delaySeconds, now);
                }));
            }

            assertTrue(ready.await(2, TimeUnit.SECONDS));
            start.countDown();

            long allowed = 0;
            for (Future<ActiveTranslator.RateLimitDecision> future : futures) {
                if (future.get(2, TimeUnit.SECONDS).allowed()) {
                    allowed++;
                }
            }
            return allowed;
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }
}
