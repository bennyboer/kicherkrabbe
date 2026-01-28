package de.bennyboer.kicherkrabbe.eventsourcing;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.*;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventWithMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotDeserializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.patch.EventPatcher;
import de.bennyboer.kicherkrabbe.eventsourcing.patch.Patch;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class EventSourcingService<A extends Aggregate> {

    private final AggregateType aggregateType;

    private final A initialState;

    private final EventSourcingRepo repo;

    private final EventPublisher eventPublisher;

    private final EventPatcher patcher;

    private final Clock clock;

    private final AggregateSnapshotSerializer snapshotSerializer;

    private final AggregateSnapshotDeserializer snapshotDeserializer;

    public EventSourcingService(
            AggregateType aggregateType,
            A initialState,
            EventSourcingRepo repo,
            EventPublisher eventPublisher,
            List<Patch> patches,
            Clock clock
    ) {
        this.aggregateType = aggregateType;
        this.initialState = initialState;
        this.repo = repo;
        this.eventPublisher = eventPublisher;
        this.patcher = EventPatcher.fromPatches(patches);
        this.clock = clock;
        this.snapshotSerializer = new AggregateSnapshotSerializer();
        this.snapshotDeserializer = new AggregateSnapshotDeserializer();
    }

    @SuppressWarnings("unchecked")
    public Mono<A> aggregateLatest(AggregateId id) {
        return aggregateLatestInContainer(id)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    @SuppressWarnings("unchecked")
    public Mono<A> aggregate(AggregateId id, Version version) {
        return aggregateInContainer(id, version)
                .filter(AggregateContainer::hasSeenEvents)
                .map(container -> (A) container.getAggregate());
    }

    /**
     * Dispatches a command to the aggregate with the given id in its latest version.
     */
    public Mono<Version> dispatchCommandToLatest(AggregateId aggregateId, Command cmd, Agent agent) {
        return aggregateLatestInContainer(aggregateId)
                .flatMap(container -> handleCommandInAggregate(aggregateId, container, cmd, agent));
    }

    /**
     * Dispatch a command to the aggregate with the given id and version.
     * If the passed version is not the latest version of the aggregate, the command will be rejected.
     */
    public Mono<Version> dispatchCommand(AggregateId aggregateId, Version version, Command cmd, Agent agent) {
        return aggregateInContainer(aggregateId, version)
                .flatMap(container -> handleCommandInAggregate(aggregateId, container, cmd, agent));
    }

    /**
     * Collapses all events of the aggregate with the given id into a snapshot event.
     * The resulting snapshot event will be the sole event of the aggregate in the event store.
     * This may be useful to either reduce the number of events in the event store or to fulfill
     * privacy requirements like cleaning up personal data.
     */
    public Mono<Version> collapseEvents(AggregateId aggregateId, Version version, Agent agent) {
        return aggregateInContainer(aggregateId, version)
                .flatMap(container -> snapshot(aggregateId, agent, container))
                .flatMap(snapshotVersion -> repo.removeEventsByAggregateIdAndTypeUntilVersion(
                        aggregateId,
                        aggregateType,
                        snapshotVersion.decrement()
                ).thenReturn(snapshotVersion));
    }

    private Mono<Version> handleCommandInAggregate(
            AggregateId aggregateId,
            AggregateContainer container,
            Command cmd,
            Agent agent
    ) {
        ApplyCommandResult result = container.getAggregate().apply(cmd, agent);

        return saveAndPublishEvents(aggregateId, container, agent, result)
                .collectList()
                .flatMap(events -> snapshotIfNecessary(aggregateId, agent, container, events));
    }

    private Mono<Version> snapshotIfNecessary(
            AggregateId aggregateId,
            Agent agent,
            AggregateContainer container,
            List<EventWithMetadata> newEvents
    ) {
        for (EventWithMetadata event : newEvents) {
            container = container.apply(event.getEvent(), event.getMetadata());
        }

        int snapshotThreshold = container.getCountOfEventsToSnapshotAfter();
        boolean autoSnapshotDisabled = snapshotThreshold <= 0;
        if (autoSnapshotDisabled) {
            return Mono.just(container.getVersion());
        }

        if (container.getVersionCountFromLastSnapshot() >= snapshotThreshold) {
            return snapshot(aggregateId, agent, container);
        }

        return Mono.just(container.getVersion());
    }

    private Mono<Version> snapshot(AggregateId aggregateId, Agent agent, AggregateContainer container) {
        var snapshotEvent = snapshotSerializer.serialize(container.getAggregate());
        var result = ApplyCommandResult.of(snapshotEvent);

        return saveAndPublishEvents(aggregateId, container, agent, result)
                .last()
                .map(event -> event.getMetadata().getAggregateVersion());
    }

    private Mono<AggregateContainer> aggregateLatestInContainer(AggregateId id) {
        return repo.findLatestSnapshotEventByAggregateIdAndType(id, aggregateType)
                .map(event -> event.getMetadata().getAggregateVersion())
                .defaultIfEmpty(Version.zero())
                .flatMapMany(fromVersion -> repo.findEventsByAggregateIdAndType(id, aggregateType, fromVersion))
                .reduce(
                        AggregateContainer.init(initialState),
                        this::applyEvent
                );
    }

    private Mono<AggregateContainer> aggregateInContainer(AggregateId id, Version version) {
        return repo.findNearestSnapshotEventByAggregateIdAndType(id, aggregateType, version)
                .map(event -> event.getMetadata().getAggregateVersion())
                .defaultIfEmpty(Version.zero())
                .flatMapMany(fromVersion -> repo.findEventsByAggregateIdAndTypeUntilVersion(
                        id,
                        aggregateType,
                        fromVersion,
                        version
                ))
                .reduce(
                        AggregateContainer.init(initialState),
                        this::applyEvent
                );
    }

    private AggregateContainer applyEvent(AggregateContainer container, EventWithMetadata event) {
        Event patchedEvent = patcher.patch(event.getEvent(), event.getMetadata());
        EventMetadata metadata = event.getMetadata();

        if (patchedEvent instanceof SnapshotEvent snapshotEvent) {
            A restoredAggregate = snapshotDeserializer.deserialize(snapshotEvent, initialState, metadata);
            return AggregateContainer.init(restoredAggregate)
                    .apply(snapshotEvent, metadata);
        }

        return container.apply(patchedEvent, metadata);
    }

    private Flux<EventWithMetadata> saveAndPublishEvents(
            AggregateId aggregateId,
            AggregateContainer container,
            Agent agent,
            ApplyCommandResult result
    ) {
        Instant now = clock.instant();

        var events = result.getEvents();
        if (events.isEmpty()) {
            return Flux.empty();
        }

        var eventsWithMetadata = new ArrayList<EventWithMetadata>();
        var currentVersion = container.hasSeenEvents() ? container.getVersion().increment() : Version.zero();
        for (Event event : events) {
            var metadata = EventMetadata.of(
                    aggregateId,
                    aggregateType,
                    currentVersion,
                    agent,
                    now,
                    event.isSnapshot()
            );

            eventsWithMetadata.add(EventWithMetadata.of(event, metadata));

            currentVersion = currentVersion.increment();
        }

        return Flux.fromIterable(eventsWithMetadata)
                .concatMap(this::insertEventInRepoAndPublish);
    }

    private Mono<EventWithMetadata> insertEventInRepoAndPublish(EventWithMetadata event) {
        return repo.insert(event)
                .flatMap(e -> {
                    if (e.getMetadata().isSnapshot()) {
                        return Mono.just(e);
                    }
                    return eventPublisher.publish(e).thenReturn(e);
                });
    }

}
