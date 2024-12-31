package de.bennyboer.kicherkrabbe.mailing.settings;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.init.InitEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.ClearMailgunApiTokenCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear.MailgunApiTokenClearedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.MailgunApiTokenUpdatedEvent;
import de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.update.UpdateMailgunApiTokenCmd;
import de.bennyboer.kicherkrabbe.mailing.settings.snapshot.SnapshottedEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Settings implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("MAILING_SETTINGS");

    SettingsId id;

    Version version;

    MailgunSettings mailgun;

    public static Settings init() {
        return new Settings(
                null,
                Version.zero(),
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(getMailgun()));
            case InitCmd c -> ApplyCommandResult.of(InitEvent.of(c.getMailgun()));
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
            case SnapshottedEvent e -> withId(id)
                    .withMailgun(e.getMailgun());
            case InitEvent e -> withId(id)
                    .withMailgun(e.getMailgun());
            case MailgunApiTokenUpdatedEvent e -> withMailgun(getMailgun().updateApiToken(e.getApiToken()));
            case MailgunApiTokenClearedEvent ignored -> withMailgun(getMailgun().clearApiToken());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

}
