package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface EventListenerHandler {

    Mono<Void> handle(EventMetadata metadata, Version eventVersion, Map<String, Object> event);

}
