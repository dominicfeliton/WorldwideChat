package com.dominicfeliton.worldwidechat.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TranslationCapacityLimiter {
    public static final int AUTO_CONFIG_VALUE = 0;
    private static final int AUTO_MIN_ACTIVE_LIMIT = 8;
    private static final int AUTO_MAX_ACTIVE_LIMIT = 64;
    private static final int AUTO_ACTIVE_PER_PROCESSOR = 4;
    private static final int QUEUE_MULTIPLIER = 4;

    private final int configuredLimit;
    private final int availableProcessors;
    private final int activeLimit;
    private final int queueLimit;
    private final Semaphore permits;
    private int queuedWaiters;

    private TranslationCapacityLimiter(int configuredLimit, int availableProcessors, int activeLimit, int queueLimit) {
        this.configuredLimit = Math.max(AUTO_CONFIG_VALUE, configuredLimit);
        this.availableProcessors = Math.max(1, availableProcessors);
        this.activeLimit = Math.max(1, activeLimit);
        this.queueLimit = Math.max(0, queueLimit);
        this.permits = new Semaphore(this.activeLimit, true);
    }

    public static TranslationCapacityLimiter fromConfiguredLimit(int configuredLimit) {
        return fromConfiguredLimit(configuredLimit, Runtime.getRuntime().availableProcessors());
    }

    public static TranslationCapacityLimiter fromConfiguredLimit(int configuredLimit, int availableProcessors) {
        int activeLimit = resolveActiveLimit(configuredLimit, availableProcessors);
        return forLimits(configuredLimit, availableProcessors, activeLimit, resolveQueueLimit(activeLimit));
    }

    public static TranslationCapacityLimiter forLimits(int activeLimit, int queueLimit) {
        return forLimits(activeLimit, Runtime.getRuntime().availableProcessors(), activeLimit, queueLimit);
    }

    public static TranslationCapacityLimiter forLimits(int configuredLimit, int availableProcessors, int activeLimit, int queueLimit) {
        return new TranslationCapacityLimiter(configuredLimit, availableProcessors, activeLimit, queueLimit);
    }

    public static int resolveActiveLimit(int configuredLimit, int availableProcessors) {
        if (configuredLimit > AUTO_CONFIG_VALUE) {
            return configuredLimit;
        }
        int processors = Math.max(1, availableProcessors);
        long autoLimit = (long) processors * AUTO_ACTIVE_PER_PROCESSOR;
        long boundedLimit = Math.max(AUTO_MIN_ACTIVE_LIMIT, Math.min(AUTO_MAX_ACTIVE_LIMIT, autoLimit));
        return (int) boundedLimit;
    }

    public static int resolveQueueLimit(int activeLimit) {
        long queueLimit = Math.max(0, activeLimit) * (long) QUEUE_MULTIPLIER;
        return queueLimit > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) queueLimit;
    }

    public Permit acquire(long waitTime, TimeUnit unit) throws InterruptedException {
        if (permits.tryAcquire()) {
            return Permit.acquired(this);
        }
        if (!tryEnterQueue()) {
            return Permit.rejected(RejectionReason.QUEUE_FULL);
        }
        try {
            if (permits.tryAcquire(waitTime, unit)) {
                return Permit.acquired(this);
            }
            return Permit.rejected(RejectionReason.TIMED_OUT);
        } finally {
            exitQueue();
        }
    }

    private synchronized boolean tryEnterQueue() {
        if (queuedWaiters >= queueLimit) {
            return false;
        }
        queuedWaiters++;
        return true;
    }

    private synchronized void exitQueue() {
        queuedWaiters--;
    }

    private void release() {
        permits.release();
    }

    public int getConfiguredLimit() {
        return configuredLimit;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public int getActiveLimit() {
        return activeLimit;
    }

    public int getQueueLimit() {
        return queueLimit;
    }

    public synchronized int getQueuedWaiters() {
        return queuedWaiters;
    }

    public int getAvailablePermits() {
        return permits.availablePermits();
    }

    public enum RejectionReason {
        QUEUE_FULL,
        TIMED_OUT
    }

    public static final class Permit implements AutoCloseable {
        private final TranslationCapacityLimiter limiter;
        private final boolean acquired;
        private final RejectionReason rejectionReason;
        private boolean closed;

        private Permit(TranslationCapacityLimiter limiter, boolean acquired, RejectionReason rejectionReason) {
            this.limiter = limiter;
            this.acquired = acquired;
            this.rejectionReason = rejectionReason;
        }

        private static Permit acquired(TranslationCapacityLimiter limiter) {
            return new Permit(limiter, true, null);
        }

        private static Permit rejected(RejectionReason rejectionReason) {
            return new Permit(null, false, rejectionReason);
        }

        public boolean acquired() {
            return acquired;
        }

        public RejectionReason rejectionReason() {
            return rejectionReason;
        }

        @Override
        public void close() {
            if (acquired && !closed) {
                closed = true;
                limiter.release();
            }
        }
    }
}
