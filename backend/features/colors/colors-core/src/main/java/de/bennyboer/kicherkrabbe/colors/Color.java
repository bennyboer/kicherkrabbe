package de.bennyboer.kicherkrabbe.colors;

import de.bennyboer.kicherkrabbe.colors.create.CreateCmd;
import de.bennyboer.kicherkrabbe.colors.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.colors.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.colors.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.colors.update.UpdateCmd;
import de.bennyboer.kicherkrabbe.colors.update.UpdatedEvent;
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
public class Color implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("COLOR");

    @SnapshotExclude
    ColorId id;

    @SnapshotExclude
    Version version;

    ColorName name;

    int red;

    int green;

    int blue;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Color init() {
        return new Color(null, Version.zero(), null, 0, 0, 0, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        check(isNotDeleted(), "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(
                    c.getName(),
                    c.getRed(),
                    c.getGreen(),
                    c.getBlue()
            ));
            case UpdateCmd c -> ApplyCommandResult.of(UpdatedEvent.of(
                    c.getName(),
                    c.getRed(),
                    c.getGreen(),
                    c.getBlue()
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        ColorId id = ColorId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case CreatedEvent e -> withId(id)
                    .withName(e.getName())
                    .withRed(e.getRed())
                    .withGreen(e.getGreen())
                    .withBlue(e.getBlue())
                    .withCreatedAt(metadata.getDate());
            case UpdatedEvent e -> withName(e.getName())
                    .withRed(e.getRed())
                    .withGreen(e.getGreen())
                    .withBlue(e.getBlue());
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
