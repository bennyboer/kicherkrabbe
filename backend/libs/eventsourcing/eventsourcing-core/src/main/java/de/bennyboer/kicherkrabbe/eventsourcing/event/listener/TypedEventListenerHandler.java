package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface TypedEventListenerHandler<T> {

    Mono<Void> handle(EventMetadata metadata, T event);

}
