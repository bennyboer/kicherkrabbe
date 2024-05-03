package de.bennyboer.kicherkrabbe.fabrics.types;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.types.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.types.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.types.update.UpdateCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class FabricTypeService extends AggregateService<FabricType, FabricTypeId> {

    public FabricTypeService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                FabricType.TYPE,
                FabricType.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<FabricTypeId>> create(FabricTypeName name, Agent agent) {
        FabricTypeId id = FabricTypeId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> update(FabricTypeId id, FabricTypeName name, Agent agent) {
        return dispatchCommandToLatest(id, agent, UpdateCmd.of(name));
    }

    public Mono<Version> delete(FabricTypeId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(FabricTypeId fabricTypeId) {
        return AggregateId.of(fabricTypeId.getValue());
    }

    @Override
    protected boolean isRemoved(FabricType aggregate) {
        return aggregate.isDeleted();
    }

}
