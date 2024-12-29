package de.bennyboer.kicherkrabbe.telegram.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.clear.ClearBotApiTokenCmd;
import de.bennyboer.kicherkrabbe.telegram.settings.bot.apitoken.update.UpdateBotApiTokenCmd;
import de.bennyboer.kicherkrabbe.telegram.settings.init.InitCmd;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

public class SettingsService extends AggregateService<Settings, SettingsId> {

    public SettingsService(EventSourcingRepo repo, EventPublisher eventPublisher, Clock clock) {
        super(new EventSourcingService<>(
                Settings.TYPE,
                Settings.init(),
                repo,
                eventPublisher,
                List.of(),
                clock
        ));
    }

    public Mono<AggregateIdAndVersion<SettingsId>> init(Agent agent) {
        return init(SettingsId.create(), agent);
    }

    public Mono<AggregateIdAndVersion<SettingsId>> init(SettingsId id, Agent agent) {
        return dispatchCommandToLatest(id, agent, InitCmd.of(BotSettings.init()))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> updateBotApiToken(SettingsId id, Version version, ApiToken token, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                UpdateBotApiTokenCmd.of(token)
        );
    }

    public Mono<Version> clearBotApiToken(SettingsId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                ClearBotApiTokenCmd.of()
        );
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
    protected boolean isRemoved(Settings settings) {
        return false;
    }

}
