package de.bennyboer.kicherkrabbe.eventsourcing.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventSourcingRepo {

    Mono<EventWithMetadata> insert(EventWithMetadata event);

    Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    );

    Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    );

    Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    );

    Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version untilVersion
    );

    /**
     * Removes all events of the aggregate with the given id and type until the given version (inclusive).
     */
    Mono<Void> removeEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType aggregateType,
            Version version
    );

}
