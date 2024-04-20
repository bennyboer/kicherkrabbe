package de.bennyboer.kicherkrabbe.messaging.outbox;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@AllArgsConstructor
public class MessagingOutboxTasks {

    private final MessagingOutbox outbox;

    @Scheduled(fixedRate = 10000)
    public void retryPublishingOutboxEntries() {
        outbox.publishNextUnpublishedEntries()
                .onErrorResume(e -> {
                    log.error("Failed to execute retry publishing entries task", e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void findStaleFailedOutboxEntries() {
        outbox.findStaleFailedEntries()
                .collectList()
                .doOnNext(entries -> log.warn("Found {} stale failed entries", entries.size()))
                .onErrorResume(e -> {
                    log.error("Failed to find stale failed outbox entries", e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void unlockStaleOutboxEntries() {
        outbox.unlockStaleEntries()
                .onErrorResume(e -> {
                    log.error("Failed to unlock stale outbox entries", e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cleanupOldAcknowledgedOutboxEntries() {
        outbox.cleanupOldAcknowledgedEntries()
                .onErrorResume(e -> {
                    log.error("Failed to cleanup old acknowledged outbox entries", e);
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

}
