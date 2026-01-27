package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.topics.create.CreateCmd;
import de.bennyboer.kicherkrabbe.topics.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.topics.update.UpdateCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class TopicService extends AggregateService<Topic, TopicId> {

    public TopicService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Topic.TYPE,
                Topic.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<TopicId>> create(TopicName name, Agent agent) {
        TopicId id = TopicId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> update(TopicId id, Version version, TopicName name, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateCmd.of(name));
    }

    public Mono<Version> delete(TopicId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Topic.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(TopicId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Topic aggregate) {
        return aggregate.isDeleted();
    }

}
