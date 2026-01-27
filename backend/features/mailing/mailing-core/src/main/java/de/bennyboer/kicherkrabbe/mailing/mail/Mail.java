package de.bennyboer.kicherkrabbe.mailing.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.mailing.MailingService;
import de.bennyboer.kicherkrabbe.mailing.mail.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.mailing.mail.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.mailing.mail.send.SendCmd;
import de.bennyboer.kicherkrabbe.mailing.mail.send.SentEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Mail implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("MAILING_MAIL");

    @SnapshotExclude
    MailId id;

    @SnapshotExclude
    Version version;

    Sender sender;

    Set<Receiver> receivers;

    Subject subject;

    Text text;

    MailingService mailingService;

    Instant sentAt;

    @Nullable
    Instant deletedAt;

    public static Mail init() {
        return new Mail(
                null,
                Version.zero(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isSent() || cmd instanceof SendCmd, "Cannot apply command to not yet sent aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SendCmd c -> ApplyCommandResult.of(SentEvent.of(
                    c.getSender(),
                    c.getReceivers(),
                    c.getSubject(),
                    c.getText(),
                    c.getMailingService()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = MailId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SentEvent e -> withId(id)
                    .withSender(e.getSender())
                    .withReceivers(e.getReceivers())
                    .withSubject(e.getSubject())
                    .withText(e.getText())
                    .withMailingService(e.getMailingService())
                    .withSentAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate())
                    .withSender(getSender().anonymized())
                    .withReceivers(getReceivers().stream().map(Receiver::anonymized).collect(Collectors.toSet()))
                    .withSubject(getSubject().anonymized())
                    .withText(getText().anonymized());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    public boolean isDeleted() {
        return getDeletedAt().isPresent();
    }

    public boolean isNotDeleted() {
        return !isDeleted();
    }

    private boolean isSent() {
        return sentAt != null;
    }

}
