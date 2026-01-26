package de.bennyboer.kicherkrabbe.mailing.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.ClearMailgunApiTokenCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.MailgunApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.MailgunApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.UpdateMailgunApiTokenCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update.RateLimitUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.ratelimit.update.UpdateRateLimitCmd;
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

    public static AggregateType TYPE = AggregateType.of("MAILING_SETTINGS");

    @SnapshotExclude
    SettingsId id;

    @SnapshotExclude
    Version version;

    RateLimitSettings rateLimit;

    MailgunSettings mailgun;

    Instant initAt;

    public static Settings init() {
        return new Settings(
                null,
                Version.zero(),
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isInit() || cmd instanceof InitCmd, "Cannot apply command to not yet initialized aggregate");

        return switch (cmd) {
            case InitCmd c -> ApplyCommandResult.of(InitEvent.of(c.getRateLimit(), c.getMailgun()));
            case UpdateRateLimitCmd c -> ApplyCommandResult.of(RateLimitUpdatedEvent.of(c.getDuration(), c.getLimit()));
            case UpdateMailgunApiTokenCmd c -> ApplyCommandResult.of(MailgunApiTokenUpdatedEvent.of(c.getApiToken()));
            case ClearMailgunApiTokenCmd ignored -> ApplyCommandResult.of(MailgunApiTokenClearedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = SettingsId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case InitEvent e -> withId(id)
                    .withRateLimit(e.getRateLimit())
                    .withMailgun(e.getMailgun())
                    .withInitAt(metadata.getDate());
            case RateLimitUpdatedEvent e -> withRateLimit(getRateLimit().update(e.getDuration(), e.getLimit()));
            case MailgunApiTokenUpdatedEvent e -> withMailgun(getMailgun().updateApiToken(e.getApiToken()));
            case MailgunApiTokenClearedEvent ignored -> withMailgun(getMailgun().clearApiToken());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    private boolean isInit() {
        return initAt != null;
    }

}
