package de.bennyboer.kicherkrabbe.fabrics.themes;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.fabrics.themes.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.themes.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.themes.update.UpdateCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class ThemeService extends AggregateService<Theme, ThemeId> {

    public ThemeService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Theme.TYPE,
                Theme.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<ThemeId>> create(ThemeName name, Agent agent) {
        ThemeId id = ThemeId.create();

        return dispatchCommandToLatest(id, agent, CreateCmd.of(name))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> update(ThemeId id, Version version, ThemeName name, Agent agent) {
        return dispatchCommand(id, version, agent, UpdateCmd.of(name));
    }

    public Mono<Version> delete(ThemeId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DeleteCmd.of());
    }

    @Override
    protected AggregateId toAggregateId(ThemeId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Theme aggregate) {
        return aggregate.isDeleted();
    }

}
