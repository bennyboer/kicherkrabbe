package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.CreateCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class ManualSnapshotAggregateService extends EventSourcingService<ManualSnapshotAggregate> {

    public ManualSnapshotAggregateService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(
                ManualSnapshotAggregate.TYPE,
                ManualSnapshotAggregate.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        );
    }

    public Mono<Version> create(String id, String title, String description, Agent agent) {
        var aggregateId = AggregateId.of(id);

        return dispatchCommandToLatest(aggregateId, CreateCmd.of(title, description, null), agent);
    }

    public Mono<Version> updateTitle(String id, Version version, String title, Agent agent) {
        var aggregateId = AggregateId.of(id);

        return dispatchCommand(aggregateId, version, UpdateTitleCmd.of(title), agent);
    }

    public Mono<Version> snapshot(String id, Version version, Agent agent) {
        var aggregateId = AggregateId.of(id);

        return dispatchCommand(aggregateId, version, SnapshotCmd.of(), agent);
    }

    public Mono<ManualSnapshotAggregate> get(String id) {
        return aggregateLatest(AggregateId.of(id));
    }

}
