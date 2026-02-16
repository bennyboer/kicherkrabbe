package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class InMemoryMessagingOutboxChangeStream {

    private final MessagingOutboxRepo repo;

    private final MessagingOutbox outbox;

    @Nullable
    private Disposable disposable;

    public InMemoryMessagingOutboxChangeStream(MessagingOutboxRepo repo, MessagingOutbox outbox) {
        this.repo = repo;
        this.outbox = outbox;
    }

    @PostConstruct
    public void start() {
        cleanup();

        disposable = Flux.defer(repo::watchInserts)
                .onBackpressureBuffer()
                .concatMap(ignored -> outbox.publishNextUnpublishedEntries())
                .subscribe();
    }

    @PreDestroy
    public void destroy() {
        cleanup();
    }

    private void cleanup() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

}
