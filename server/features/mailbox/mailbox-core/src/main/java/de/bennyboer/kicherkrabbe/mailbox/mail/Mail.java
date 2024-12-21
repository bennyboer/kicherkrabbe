package de.bennyboer.kicherkrabbe.mailbox.mail;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailbox.mail.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.read.MarkAsReadCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.read.MarkedAsReadEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.receive.ReceiveCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.receive.ReceivedEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.mailbox.mail.unread.MarkAsUnreadCmd;
import de.bennyboer.kicherkrabbe.mailbox.mail.unread.MarkedAsUnreadEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Mail implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("MAIL");

    MailId id;

    Version version;

    Origin origin;

    Sender sender;

    Subject subject;

    Content content;

    Instant receivedAt;

    @Nullable
    Instant readAt;

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
                Instant.now(),
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getOrigin(),
                    getSender(),
                    getSubject(),
                    getContent(),
                    getReceivedAt(),
                    getReadAt().orElse(null),
                    getDeletedAt().orElse(null)
            ));
            case ReceiveCmd c -> ApplyCommandResult.of(ReceivedEvent.of(
                    c.getOrigin(),
                    c.getSender(),
                    c.getSubject(),
                    c.getContent()
            ));
            case MarkAsReadCmd ignored -> ApplyCommandResult.of(MarkedAsReadEvent.of());
            case MarkAsUnreadCmd ignored -> ApplyCommandResult.of(MarkedAsUnreadEvent.of());
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of(getOrigin()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = MailId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withOrigin(e.getOrigin())
                    .withSender(e.getSender())
                    .withSubject(e.getSubject())
                    .withContent(e.getContent())
                    .withReceivedAt(e.getReceivedAt())
                    .withReadAt(e.getReadAt())
                    .withDeletedAt(e.getDeletedAt());
            case ReceivedEvent e -> withId(id)
                    .withOrigin(e.getOrigin())
                    .withSender(e.getSender())
                    .withSubject(e.getSubject())
                    .withContent(e.getContent())
                    .withReceivedAt(metadata.getDate());
            case MarkedAsReadEvent ignored -> {
                if (isRead()) {
                    throw new MailNotUnreadException();
                }

                yield withReadAt(metadata.getDate());
            }
            case MarkedAsUnreadEvent ignored -> {
                if (isUnread()) {
                    throw new MailNotReadException();
                }

                yield withReadAt(null);
            }
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate())
                    .withSender(getSender().anonymize())
                    .withSubject(getSubject().anonymize())
                    .withContent(getContent().anonymize());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    public Optional<Instant> getReadAt() {
        return Optional.ofNullable(readAt);
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

    public boolean isRead() {
        return getReadAt().isPresent();
    }

    public boolean isUnread() {
        return !isRead();
    }

}
