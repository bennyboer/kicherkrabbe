package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.create.CreateCmd;
import de.bennyboer.kicherkrabbe.colors.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.colors.update.UpdateCmd;
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

import java.time.Clock;
import java.util.List;

public class ColorService extends AggregateService<Color, ColorId> {

    public ColorService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Color.TYPE,
                Color.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<ColorId>> create(ColorName name, int red, int green, int blue, Agent agent) {
        ColorId id = ColorId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name, red, green, blue))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> update(
            ColorId id,
            Version version,
            ColorName name,
            int red,
            int green,
            int blue,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateCmd.of(name, red, green, blue));
    }

    public Mono<Version> delete(ColorId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateType getAggregateType() {
        return Color.TYPE;
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
