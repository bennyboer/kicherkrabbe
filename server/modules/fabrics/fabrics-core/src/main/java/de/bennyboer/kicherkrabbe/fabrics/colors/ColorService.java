package de.bennyboer.kicherkrabbe.fabrics.colors;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.colors.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.colors.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.colors.update.UpdateCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class ColorService extends AggregateService<Color, ColorId> {

    public ColorService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Color.TYPE,
                Color.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<ColorId>> create(ColorName name, int red, int green, int blue, Agent agent) {
        ColorId id = ColorId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, red, green, blue))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> update(ColorId id, ColorName name, int red, int green, int blue, Agent agent) {
        return dispatchCommandToLatest(id, agent, UpdateCmd.of(name, red, green, blue));
    }

    public Mono<Version> delete(ColorId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(ColorId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Color aggregate) {
        return aggregate.isDeleted();
    }

}
