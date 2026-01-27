package de.bennyboer.kicherkrabbe.messaging.outbox.publisher;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoggingMessagingOutboxEntryPublisher implements MessagingOutboxEntryPublisher {

    private final List<MessagingOutboxEntry> entries = new ArrayList<>();

    @Override
    public Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries) {
        return Mono.fromRunnable(() -> this.entries.addAll(entries));
    }

    public List<MessagingOutboxEntry> getEntries() {
        return entries;
    }

}

