package de.bennyboer.kicherkrabbe.messaging.listener;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MessageListenerConcurrencyLimiter {

    private static final Duration DEFAULT_ACQUIRE_TIMEOUT = Duration.ofMinutes(2);

    private final Semaphore semaphore;

    private final Duration acquireTimeout;

    public MessageListenerConcurrencyLimiter(int maxConcurrency) {
        this(maxConcurrency, DEFAULT_ACQUIRE_TIMEOUT);
    }

    public MessageListenerConcurrencyLimiter(int maxConcurrency, Duration acquireTimeout) {
        this.semaphore = new Semaphore(maxConcurrency);
        this.acquireTimeout = acquireTimeout;
    }

    public boolean tryAcquire() {
        try {
            return semaphore.tryAcquire(acquireTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void release() {
        semaphore.release();
    }

}
