package de.bennyboer.kicherkrabbe.inquiries.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.disable.DisabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnableCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.enable.EnabledEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.inquiries.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.RateLimitsUpdatedEvent;
import de.bennyboer.kicherkrabbe.inquiries.settings.update.ratelimits.UpdateRateLimitsCmd;
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

    public static AggregateType TYPE = AggregateType.of("INQUIRIES_SETTINGS");

    @SnapshotExclude
    SettingsId id;

    @SnapshotExclude
    Version version;

    boolean enabled;

    RateLimits rateLimits;

    Instant initAt;

    public static Settings init() {
        return new Settings(null, Version.zero(), false, RateLimits.init(), null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isInit() || cmd instanceof InitCmd, "Cannot apply command to not yet initialized aggregate");

        return switch (cmd) {
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
            case InitEvent e -> withId(id)
                    .withEnabled(e.isEnabled())
                    .withRateLimits(e.getRateLimits())
                    .withInitAt(metadata.getDate());
            case EnabledEvent ignored -> withEnabled(true);
            case DisabledEvent ignored -> withEnabled(false);
            case RateLimitsUpdatedEvent e -> withRateLimits(e.getRateLimits());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    private boolean isInit() {
        return initAt != null;
    }

}
