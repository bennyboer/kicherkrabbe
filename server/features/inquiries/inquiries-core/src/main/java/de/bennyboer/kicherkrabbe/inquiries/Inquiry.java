package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.inquiries.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.inquiries.send.SendCmd;
import de.bennyboer.kicherkrabbe.inquiries.send.SentEvent;
import de.bennyboer.kicherkrabbe.inquiries.snapshot.SnapshottedEvent;
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
public class Inquiry implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("INQUIRY");

    InquiryId id;

    Version version;

    RequestId requestId;

    Sender sender;

    Subject subject;

    Message message;

    Fingerprint fingerprint;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Inquiry init() {
        return new Inquiry(
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
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getRequestId(),
                    getSender(),
                    getSubject(),
                    getMessage(),
                    getFingerprint(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case SendCmd c -> ApplyCommandResult.of(SentEvent.of(
                    c.getRequestId(),
                    c.getSender(),
                    c.getSubject(),
                    c.getMessage(),
                    c.getFingerprint()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = InquiryId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withRequestId(e.getRequestId())
                    .withSender(e.getSender())
                    .withSubject(e.getSubject())
                    .withMessage(e.getMessage())
                    .withFingerprint(e.getFingerprint())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case SentEvent e -> withId(id)
                    .withRequestId(e.getRequestId())
                    .withSender(e.getSender())
                    .withSubject(e.getSubject())
                    .withMessage(e.getMessage())
                    .withFingerprint(e.getFingerprint())
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withSender(getSender().anonymize())
                    .withSubject(getSubject().anonymize())
                    .withMessage(getMessage().anonymize())
                    .withFingerprint(getFingerprint().anonymize())
                    .withDeletedAt(metadata.getDate());
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
        return createdAt != null;
    }

}
