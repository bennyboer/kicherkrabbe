package de.bennyboer.kicherkrabbe.messaging.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MessageListenerContainerManager implements SmartLifecycle {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final List<SimpleMessageListenerContainer> containers = new CopyOnWriteArrayList<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    public void register(SimpleMessageListenerContainer container) {
        containers.add(container);
    }

    @Override
    public void start() {
        running.set(true);
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }

        log.info("Stopping {} message listener containers in parallel", containers.size());

        CountDownLatch latch = new CountDownLatch(containers.size());

        for (SimpleMessageListenerContainer container : containers) {
            Thread.startVirtualThread(() -> {
                try {
                    container.stop();
                } catch (Exception e) {
                    log.warn("Error stopping container for queue {}", container.getQueueNames(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            boolean finished = latch.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("Timeout waiting for message listener containers to stop");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for message listener containers to stop");
        }

        log.info("All message listener containers stopped");
        containers.clear();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

}
