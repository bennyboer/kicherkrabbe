package de.bennyboer.kicherkrabbe.notifications.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.notifications.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.activate.ActivateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.activate.SystemChannelActivatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.deactivate.DeactivateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.deactivate.SystemChannelDeactivatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update.SystemChannelUpdatedEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.channels.update.UpdateSystemChannelCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.disable.DisableSystemNotificationsCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.disable.SystemNotificationsDisabledEvent;
import de.bennyboer.kicherkrabbe.notifications.settings.system.enable.EnableSystemNotificationsCmd;
import de.bennyboer.kicherkrabbe.notifications.settings.system.enable.SystemNotificationsEnabledEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Settings implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("NOTIFICATIONS_SETTINGS");

    @SnapshotExclude
    SettingsId id;

    @SnapshotExclude
    Version version;

    SystemSettings systemSettings;

    Instant initAt;

    public static Settings init() {
        return new Settings(
                null,
                Version.zero(),
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isInit() || cmd instanceof InitCmd, "Cannot apply command to not yet initialized aggregate");

        return switch (cmd) {
            case InitCmd c -> ApplyCommandResult.of(InitEvent.of(c.getSystemSettings()));
            case EnableSystemNotificationsCmd ignored -> ApplyCommandResult.of(SystemNotificationsEnabledEvent.of());
            case DisableSystemNotificationsCmd ignored -> ApplyCommandResult.of(SystemNotificationsDisabledEvent.of());
            case UpdateSystemChannelCmd c -> ApplyCommandResult.of(SystemChannelUpdatedEvent.of(c.getChannel()));
            case ActivateSystemChannelCmd c ->
                    ApplyCommandResult.of(SystemChannelActivatedEvent.of(c.getChannelType()));
            case DeactivateSystemChannelCmd c ->
                    ApplyCommandResult.of(SystemChannelDeactivatedEvent.of(c.getChannelType()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = SettingsId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case InitEvent e -> withId(id)
                    .withSystemSettings(e.getSystemSettings())
                    .withInitAt(metadata.getDate());
            case SystemNotificationsEnabledEvent ignored -> withSystemSettings(getSystemSettings().enable());
            case SystemNotificationsDisabledEvent ignored -> withSystemSettings(getSystemSettings().disable());
            case SystemChannelUpdatedEvent e -> withSystemSettings(getSystemSettings().updateChannel(e.getChannel()));
            case SystemChannelActivatedEvent e ->
                    withSystemSettings(getSystemSettings().activateChannel(e.getChannelType()));
            case SystemChannelDeactivatedEvent e ->
                    withSystemSettings(getSystemSettings().deactivateChannel(e.getChannelType()));
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    private boolean isInit() {
        return initAt != null;
    }

}
