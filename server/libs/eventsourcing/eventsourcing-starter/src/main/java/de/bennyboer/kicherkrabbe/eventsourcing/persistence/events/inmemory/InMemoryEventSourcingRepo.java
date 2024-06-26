package de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An in-memory implementation of {@link EventSourcingRepo}.
 * This may be useful for testing purposes.
 */
public class InMemoryEventSourcingRepo implements EventSourcingRepo {

    private final Map<AggregateIdAndType, CopyOnWriteArrayList<EventWithMetadata>> eventsLookup =
            new ConcurrentHashMap<>();

    @Override
    public Mono<EventWithMetadata> insert(EventWithMetadata event) {
        AggregateIdAndType aggregateIdAndType = toAggregateIdAndType(event);

        return Mono.just(event)
                .flatMap(e -> {
                    var events = eventsLookup.computeIfAbsent(
                            aggregateIdAndType,
                            key -> new CopyOnWriteArrayList<>()
                    );

                    if (events.isEmpty()) {
                        events.add(event);
                        return Mono.just(e);
                    }

                    var lastEvent = events.get(events.size() - 1);

                    Version lastVersion = lastEvent.getMetadata().getAggregateVersion();
                    Version newVersion = event.getMetadata().getAggregateVersion();

                    if (!lastVersion.isPreviousTo(newVersion)) {
                        return Mono.error(new AggregateVersionOutdatedError(
                                event.getMetadata().getAggregateType(),
                                event.getMetadata().getAggregateId(),
                                newVersion
                        ));
                    }

                    events.add(e);

                    return Mono.just(e);
                });
    }

    @Override
    public Mono<EventWithMetadata> findNearestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version version
    ) {
        return Mono.justOrEmpty(getEvents(AggregateIdAndType.of(aggregateId, type))
                .flatMap(events -> {
                    for (int i = Math.min(events.size() - 1, (int) version.getValue()); i >= 0; i--) {
                        var event = events.get(i);
                        if (event.getMetadata().isSnapshot()) {
                            return Optional.of(event);
                        }
                    }

                    return Optional.empty();
                }));
    }

    @Override
    public Mono<EventWithMetadata> findLatestSnapshotEventByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        return Mono.justOrEmpty(getEvents(AggregateIdAndType.of(aggregateId, type))
                .flatMap(events -> {
                    for (int i = events.size() - 1; i >= 0; i--) {
                        var event = events.get(i);
                        if (event.getMetadata().isSnapshot()) {
                            return Optional.of(event);
                        }
                    }

                    return Optional.empty();
                }));
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion
    ) {
        return findEventsByAggregateIdAndType(aggregateId, type)
                .filter(event -> event.getMetadata().getAggregateVersion().compareTo(fromVersion) >= 0);
    }

    @Override
    public Flux<EventWithMetadata> findEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType type,
            Version fromVersion,
            Version version
    ) {
        return findEventsByAggregateIdAndType(aggregateId, type, fromVersion)
                .filter(event -> event.getMetadata().getAggregateVersion().compareTo(version) <= 0);
    }

    @Override
    public Mono<Void> removeEventsByAggregateIdAndTypeUntilVersion(
            AggregateId aggregateId,
            AggregateType aggregateType,
            Version version
    ) {
        return Mono.fromRunnable(() -> {
            var events = eventsLookup.get(AggregateIdAndType.of(aggregateId, aggregateType));
            if (events == null) {
                return;
            }

            events.removeIf(event -> event.getMetadata().getAggregateVersion().compareTo(version) <= 0);
        });
    }

    private Flux<EventWithMetadata> findEventsByAggregateIdAndType(
            AggregateId aggregateId,
            AggregateType type
    ) {
        return Optional.ofNullable(eventsLookup.get(AggregateIdAndType.of(aggregateId, type)))
                .map(Flux::fromIterable)
                .orElse(Flux.empty());
    }

    private Optional<CopyOnWriteArrayList<EventWithMetadata>> getEvents(AggregateIdAndType aggregateIdAndType) {
        return Optional.ofNullable(eventsLookup.get(aggregateIdAndType));
    }

    private AggregateIdAndType toAggregateIdAndType(EventWithMetadata event) {
        EventMetadata metadata = event.getMetadata();
        return AggregateIdAndType.of(metadata.getAggregateId(), metadata.getAggregateType());
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class AggregateIdAndType {

        AggregateId aggregateId;

        AggregateType type;

        public static AggregateIdAndType of(AggregateId aggregateId, AggregateType type) {
            if (aggregateId == null) {
                throw new IllegalArgumentException("AggregateId must be given");
            }
            if (type == null) {
                throw new IllegalArgumentException("AggregateType must be given");
            }

            return new AggregateIdAndType(aggregateId, type);
        }

    }

}
