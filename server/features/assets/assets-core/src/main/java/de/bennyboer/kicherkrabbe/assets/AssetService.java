package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.create.CreateCmd;
import de.bennyboer.kicherkrabbe.assets.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import reactor.core.publisher.Mono;

import java.util.List;

public class AssetService extends AggregateService<Asset, AssetId> {

    public AssetService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Asset.TYPE,
                Asset.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<AssetId>> create(
            AssetId id,
            ContentType contentType,
            Location location,
            Agent agent
    ) {
        return dispatchCommandToLatest(id, agent, CreateCmd.of(contentType, location))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> delete(AssetId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Asset.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(AssetId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Asset aggregate) {
        return aggregate.isDeleted();
    }

}
