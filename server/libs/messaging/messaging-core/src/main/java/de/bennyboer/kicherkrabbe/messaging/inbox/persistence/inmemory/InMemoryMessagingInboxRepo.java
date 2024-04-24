package de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory;

import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessage;
import de.bennyboer.kicherkrabbe.messaging.inbox.IncomingMessageId;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.IncomingMessageAlreadySeenException;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessagingInboxRepo implements MessagingInboxRepo {

    private final Map<IncomingMessageId, IncomingMessage> messages = new ConcurrentHashMap<>();

    private final boolean failable;

    public InMemoryMessagingInboxRepo() {
        this(true);
    }

    public InMemoryMessagingInboxRepo(boolean failable) {
        this.failable = failable;
    }

    @Override
    public Mono<Void> insert(IncomingMessage message) {
        return assertThatMessageHasNotBeenSeen(message.getId())
                .then(insertUnchecked(message));
    }

    public Flux<IncomingMessage> findAll() {
        return Flux.defer(() -> Flux.fromIterable(messages.values()));
    }

    private Mono<Void> insertUnchecked(IncomingMessage message) {
        return Mono.fromRunnable(() -> {
            messages.put(message.getId(), message);
        });
    }

    private Mono<Void> assertThatMessageHasNotBeenSeen(IncomingMessageId id) {
        return findAllAsMap()
                .filter(msgs -> failable)
                .flatMap(msgs -> {
                    if (msgs.containsKey(id)) {
                        return Mono.error(new IncomingMessageAlreadySeenException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Map<IncomingMessageId, IncomingMessage>> findAllAsMap() {
        return Mono.fromSupplier(() -> new HashMap<>(messages));
    }

}
