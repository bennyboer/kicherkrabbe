package de.bennyboer.kicherkrabbe.inquiries.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.UpdateRateLimitsCmd;
import reactor.core.publisher.Mono;

import java.util.List;

public class SettingsService extends AggregateService<Settings, SettingsId> {

    public SettingsService(EventSourcingRepo repo, EventPublisher eventPublisher) {
        super(new EventSourcingService<>(
                Settings.TYPE,
                Settings.init(),
                repo,
                eventPublisher,
                List.of()
        ));
    }

    public Mono<AggregateIdAndVersion<SettingsId>> init(Agent agent) {
        var id = SettingsId.create();

        return dispatchCommandToLatest(id, agent, InitCmd.of())
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> enable(SettingsId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, EnableCmd.of());
    }

    public Mono<Version> disable(SettingsId id, Version version, Agent agent) {
        return dispatchCommand(id, version, agent, DisableCmd.of());
    }

    public Mono<Version> updateRateLimits(
            SettingsId id,
            Version version,
            RateLimits rateLimits,
            Agent agent
    ) {
        return dispatchCommand(id, version, agent, UpdateRateLimitsCmd.of(rateLimits));
    }

    @Override
    protected AggregateType getAggregateType() {
        return Settings.TYPE;
    }

    @Override
    protected AggregateId toAggregateId(SettingsId id) {
        return AggregateId.of(id.getValue());
    }

    @Override
    protected boolean isRemoved(Settings aggregate) {
        return false;
    }

}
