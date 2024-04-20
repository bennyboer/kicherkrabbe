package de.bennyboer.kicherkrabbe.messaging.outbox.publisher;

import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface MessagingOutboxEntryPublisher {

    Mono<Void> publishAll(Collection<MessagingOutboxEntry> entries);

}
