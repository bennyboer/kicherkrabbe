package de.bennyboer.kicherkrabbe.notifications.notification;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.notifications.channel.Channel;
import de.bennyboer.kicherkrabbe.notifications.notification.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.notifications.notification.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.notifications.notification.send.SendCmd;
import de.bennyboer.kicherkrabbe.notifications.notification.send.SentEvent;
import de.bennyboer.kicherkrabbe.notifications.notification.snapshot.SnapshottedEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Notification implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("NOTIFICATION");

    NotificationId id;

    Version version;

    Origin origin;

    Target target;

    Set<Channel> channels;

    Title title;

    Message message;

    Instant sentAt;

    @Nullable
    Instant deletedAt;

    public static Notification init() {
        return new Notification(
                null,
                Version.zero(),
                null,
                null,
                Set.of(),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isSent() || cmd instanceof SendCmd, "Cannot apply command to not yet sent aggregate");
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getOrigin(),
                    getTarget(),
                    getChannels(),
                    getTitle(),
                    getMessage(),
                    getSentAt(),
                    getDeletedAt().orElse(null)
            ));
            case SendCmd c -> ApplyCommandResult.of(SentEvent.of(
                    c.getOrigin(),
                    c.getTarget(),
                    c.getChannels(),
                    c.getTitle(),
                    c.getMessage()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = NotificationId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withOrigin(e.getOrigin())
                    .withTarget(e.getTarget())
                    .withChannels(e.getChannels())
                    .withTitle(e.getTitle())
                    .withMessage(e.getMessage())
                    .withSentAt(e.getSentAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case SentEvent e -> withId(id)
                    .withOrigin(e.getOrigin())
                    .withTarget(e.getTarget())
                    .withChannels(e.getChannels())
                    .withTitle(e.getTitle())
                    .withMessage(e.getMessage())
                    .withSentAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate())
                    .withTitle(getTitle().anonymize())
                    .withMessage(getMessage().anonymize());
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
