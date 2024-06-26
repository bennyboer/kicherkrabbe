package de.bennyboer.kicherkrabbe.eventsourcing.event.publish;

import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import reactor.core.publisher.Mono;

/**
 * Any event that is created by any aggregate is publishable by an event publisher.
 * You may want to use a messaging system to publish events.
 */
public interface EventPublisher {

    Mono<Void> publish(EventWithMetadata event);

}
