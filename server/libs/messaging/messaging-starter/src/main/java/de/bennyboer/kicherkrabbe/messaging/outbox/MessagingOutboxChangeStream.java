package de.bennyboer.kicherkrabbe.messaging.outbox;

import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
public class MessagingOutboxChangeStream {

    private final MessagingOutboxRepo repo;

    private final MessagingOutbox outbox;

    @Nullable
    private Disposable disposable;

    public MessagingOutboxChangeStream(MessagingOutboxRepo repo, MessagingOutbox outbox) {
        this.repo = repo;
        this.outbox = outbox;
    }

    @PostConstruct
    public void start() {
        listen();
    }

    @PreDestroy
    public void destroy() {
        cleanup();
    }

    private void listen() {
        cleanup();

        disposable = repo.watchInserts()
                .concatMap(ignored -> outbox.publishNextUnpublishedEntries())
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofMinutes(5))
                        .doBeforeRetry(signal -> log.warn(
                                "Retrying change stream after error (attempt {})",
                                signal.totalRetries() + 1,
                                signal.failure()
                        )))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void cleanup() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

}
