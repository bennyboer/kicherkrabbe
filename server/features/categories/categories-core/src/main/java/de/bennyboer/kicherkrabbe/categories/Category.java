package de.bennyboer.kicherkrabbe.categories;

import de.bennyboer.kicherkrabbe.categories.create.CreateCmd;
import de.bennyboer.kicherkrabbe.categories.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.categories.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.categories.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.categories.regroup.RegroupCmd;
import de.bennyboer.kicherkrabbe.categories.regroup.RegroupedEvent;
import de.bennyboer.kicherkrabbe.categories.rename.RenameCmd;
import de.bennyboer.kicherkrabbe.categories.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
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
public class Category implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("CATEGORY");

    @SnapshotExclude
    CategoryId id;

    @SnapshotExclude
    Version version;

    CategoryName name;

    CategoryGroup group;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Category init() {
        return new Category(null, Version.zero(), null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c.getName(), c.getGroup()));
            case RenameCmd c -> ApplyCommandResult.of(RenamedEvent.of(c.getName()));
            case RegroupCmd c -> ApplyCommandResult.of(RegroupedEvent.of(getName(), c.getGroup()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = CategoryId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withGroup(e.getGroup())
                    .withCreatedAt(metadata.getDate());
            case RenamedEvent e -> withName(e.getName());
            case RegroupedEvent e -> withGroup(e.getGroup());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
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
