package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.CreateCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.DeleteCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateDescriptionCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.patches.CreatedEventPatch1;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class SampleAggregateService extends AggregateService<SampleAggregate, String> {

    public SampleAggregateService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                SampleAggregate.TYPE,
                SampleAggregate.init(),
                repo,
                eventPublisher,
                List.of(new CreatedEventPatch1())
        ));
    }

    public Mono<Version> create(String id, String title, String description, Agent agent) {
        return dispatchCommandToLatest(id, agent, CreateCmd.of(title, description, null));
    }

    public Mono<Version> updateTitle(String id, Version version, String title, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateTitleCmd.of(title));
    }

    public Mono<Version> updateDescription(String id, Version version, String description, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateDescriptionCmd.of(description));
    }

    public Mono<Version> delete(String id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return SampleAggregate.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(String id) {
        return AggregateId.of(id);
    }

    @Override
    protected boolean isRemoved(SampleAggregate aggregate) {
        return aggregate.isDeleted();
    }

}
