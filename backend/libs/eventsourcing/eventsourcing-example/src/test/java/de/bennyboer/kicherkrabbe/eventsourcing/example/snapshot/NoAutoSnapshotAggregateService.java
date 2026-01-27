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

public class NoAutoSnapshotAggregateService extends EventSourcingService<NoAutoSnapshotAggregate> {

    public NoAutoSnapshotAggregateService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(
                NoAutoSnapshotAggregate.TYPE,
                NoAutoSnapshotAggregate.init(),
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

    public Mono<NoAutoSnapshotAggregate> get(String id) {
        return aggregateLatest(AggregateId.of(id));
    }

}
