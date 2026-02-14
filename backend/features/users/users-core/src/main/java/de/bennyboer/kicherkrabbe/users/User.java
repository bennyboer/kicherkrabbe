package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.users.create.CreateCmd;
import de.bennyboer.kicherkrabbe.users.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.users.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.users.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.users.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.users.rename.RenamedEvent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class User implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("USER");

    @SnapshotExclude
    UserId id;

    @SnapshotExclude
    Version version;

    FullName name;

    Mail mail;

    Instant createdAt;

    @Nullable
    @Getter(NONE)
    Instant deletedAt;

    public static User init() {
        return new User(
                null,
                Version.zero(),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getMail()
            ));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        UserId id = UserId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withMail(e.getMail())
                    .withCreatedAt(metadata.getDate());
            case RenamedEvent e -> withName(e.getName());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate())
                    .withName(name.anonymize())
                    .withMail(mail.anonymize());
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

    private boolean isCreated() {
        return createdAt != null;
    }

}
