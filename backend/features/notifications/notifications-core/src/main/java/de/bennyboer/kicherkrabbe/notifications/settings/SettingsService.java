package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSourcingService;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateIdAndVersion;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.EventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.EventSourcingRepo;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.channel.ChannelType;
import de.bennyboer.kicherkrabbe.notifications.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.activate.ActivateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.deactivate.DeactivateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update.UpdateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.disable.DisableSystemNotificationsCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.enable.EnableSystemNotificationsCmd;
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
        return dispatchCommandToLatest(id, agent, InitCmd.of(SystemSettings.init()))
                .map(version -> AggregateIdAndVersion.of(id, version));
    }

    public Mono<Version> enableSystemNotifications(SettingsId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                EnableSystemNotificationsCmd.of()
        );
    }

    public Mono<Version> disableSystemNotifications(SettingsId id, Version version, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                DisableSystemNotificationsCmd.of()
        );
    }

    public Mono<Version> updateSystemChannel(SettingsId id, Version version, Channel channel, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                UpdateSystemChannelCmd.of(channel)
        );
    }

    public Mono<Version> activateSystemChannel(SettingsId id, Version version, ChannelType channelType, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                ActivateSystemChannelCmd.of(channelType)
        );
    }

    public Mono<Version> deactivateSystemChannel(SettingsId id, Version version, ChannelType channelType, Agent agent) {
        return dispatchCommand(
                id,
                version,
                agent,
                DeactivateSystemChannelCmd.of(channelType)
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
