package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import reactor.core.publisher.Mono;

public interface EventListenerHandler {

    Mono<Void> handle(HandleableEvent event);

}
