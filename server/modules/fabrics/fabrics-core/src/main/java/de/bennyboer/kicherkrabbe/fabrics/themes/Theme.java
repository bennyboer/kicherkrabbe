package de.bennyboer.kicherkrabbe.fabrics.themes;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.fabrics.themes.create.CreateCmd;
import de.bennyboer.kicherkrabbe.fabrics.themes.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.fabrics.themes.delete.DeleteCmd;
import de.bennyboer.kicherkrabbe.fabrics.themes.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.fabrics.themes.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.fabrics.themes.update.UpdateCmd;
import de.bennyboer.kicherkrabbe.fabrics.themes.update.UpdatedEvent;
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
public class Theme implements Aggregate {

    public static final AggregateType TYPE = AggregateType.of("THEME");

    ThemeId id;

    Version version;

    ThemeName name;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static Theme init() {
        return new Theme(null, Version.zero(), null, Instant.now(), null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isNotDeleted() || cmd instanceof SnapshotCmd, "Cannot apply command to deleted aggregate");

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getName(),
                    getCreatedAt(),
                    getDeletedAt().orElse(null)
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(c.getName()));
            case UpdateCmd c -> ApplyCommandResult.of(UpdatedEvent.of(c.getName()));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        ThemeId id = ThemeId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case SnapshottedEvent e -> withId(id)
                    .withName(e.getName())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent e -> withId(id)
                    .withName(e.getName());
            case UpdatedEvent e -> withName(e.getName());
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

}
