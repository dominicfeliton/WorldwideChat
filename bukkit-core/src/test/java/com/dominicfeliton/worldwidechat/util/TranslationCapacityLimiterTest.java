package com.dominicfeliton.worldwidechat.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TranslationCapacityLimiterTest {

    @Test
    void autoLimitScalesFromAvailableProcessorsWithinBounds() {
        assertEquals(8, TranslationCapacityLimiter.resolveActiveLimit(0, 1));
        assertEquals(8, TranslationCapacityLimiter.resolveActiveLimit(0, 2));
        assertEquals(16, TranslationCapacityLimiter.resolveActiveLimit(0, 4));
        assertEquals(64, TranslationCapacityLimiter.resolveActiveLimit(0, 16));
        assertEquals(64, TranslationCapacityLimiter.resolveActiveLimit(0, 128));
    }

    @Test
    void manualLimitOverridesAuto() {
        assertEquals(96, TranslationCapacityLimiter.resolveActiveLimit(96, 2));
    }

    @Test
    void queueLimitDerivesFromActiveLimit() {
        assertEquals(32, TranslationCapacityLimiter.resolveQueueLimit(8));
    }

    @Test
    void queueFullRejectsWhenNoQueuedCapacity() throws Exception {
        TranslationCapacityLimiter limiter = TranslationCapacityLimiter.forLimits(1, 0);

        try (TranslationCapacityLimiter.Permit firstPermit = limiter.acquire(1, TimeUnit.MILLISECONDS);
             TranslationCapacityLimiter.Permit secondPermit = limiter.acquire(1, TimeUnit.MILLISECONDS)) {
            assertTrue(firstPermit.acquired());
            assertFalse(secondPermit.acquired());
            assertEquals(TranslationCapacityLimiter.RejectionReason.QUEUE_FULL, secondPermit.rejectionReason());
        }
    }

    @Test
    void queuedAcquireRunsWhenPermitIsReleased() throws Exception {
        TranslationCapacityLimiter limiter = TranslationCapacityLimiter.forLimits(1, 1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try (TranslationCapacityLimiter.Permit firstPermit = limiter.acquire(1, TimeUnit.MILLISECONDS)) {
            assertTrue(firstPermit.acquired());
            Future<TranslationCapacityLimiter.Permit> waitingPermit =
                    executor.submit(() -> limiter.acquire(1, TimeUnit.SECONDS));
            waitForQueuedWaiter(limiter);

            firstPermit.close();

            try (TranslationCapacityLimiter.Permit secondPermit = waitingPermit.get(2, TimeUnit.SECONDS)) {
                assertTrue(secondPermit.acquired());
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void queuedAcquireTimesOutAndLeavesQueue() throws Exception {
        TranslationCapacityLimiter limiter = TranslationCapacityLimiter.forLimits(1, 1);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try (TranslationCapacityLimiter.Permit firstPermit = limiter.acquire(1, TimeUnit.MILLISECONDS)) {
            assertTrue(firstPermit.acquired());
            Future<TranslationCapacityLimiter.Permit> waitingPermit =
                    executor.submit(() -> limiter.acquire(50, TimeUnit.MILLISECONDS));
            waitForQueuedWaiter(limiter);

            try (TranslationCapacityLimiter.Permit secondPermit = waitingPermit.get(1, TimeUnit.SECONDS)) {
                assertFalse(secondPermit.acquired());
                assertEquals(TranslationCapacityLimiter.RejectionReason.TIMED_OUT, secondPermit.rejectionReason());
            }

            assertEquals(0, limiter.getQueuedWaiters());
            assertEquals(0, limiter.getAvailablePermits());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }

        assertEquals(1, limiter.getAvailablePermits());
    }

    private void waitForQueuedWaiter(TranslationCapacityLimiter limiter) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 1000;
        while (limiter.getQueuedWaiters() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(1, limiter.getQueuedWaiters());
    }
}
