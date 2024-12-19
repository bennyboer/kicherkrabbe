package de.bennyboer.kicherkrabbe.inquiries.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.RateLimitsUpdatedEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.UpdateRateLimitsCmd;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Settings implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("INQUIRIES_SETTINGS");

    SettingsId id;

    Version version;

    boolean enabled;

    RateLimits rateLimits;

    public static Settings init() {
        return new Settings(null, Version.zero(), false, RateLimits.init());
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(isEnabled(), getRateLimits()));
            case InitCmd ignored -> ApplyCommandResult.of(InitEvent.of(isEnabled(), getRateLimits()));
            case EnableCmd ignored -> ApplyCommandResult.of(EnabledEvent.of());
            case DisableCmd ignored -> ApplyCommandResult.of(DisabledEvent.of());
            case UpdateRateLimitsCmd c -> ApplyCommandResult.of(RateLimitsUpdatedEvent.of(c.getRateLimits()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = SettingsId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withEnabled(e.isEnabled())
                    .withRateLimits(e.getRateLimits());
            case InitEvent e -> withId(id)
                    .withEnabled(e.isEnabled())
                    .withRateLimits(e.getRateLimits());
            case EnabledEvent ignored -> withEnabled(true);
            case DisabledEvent ignored -> withEnabled(false);
            case RateLimitsUpdatedEvent e -> withRateLimits(e.getRateLimits());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

}
