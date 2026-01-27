package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.CreateCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.DeleteCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateDescriptionCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.CreatedEvent2;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.DeletedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.DescriptionUpdatedEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.TitleUpdatedEvent;
import jakarta.annotation.Nullable;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
public class ManualSnapshotAggregate implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("MANUAL_SNAPSHOT");

    @SnapshotExclude
    String id;

    @SnapshotExclude
    Version version;

    String title;

    String description;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static ManualSnapshotAggregate init() {
        return new ManualSnapshotAggregate(null, Version.zero(), null, null, null, null);
    }

    @Override
    public int getCountOfEventsToSnapshotAfter() {
        return 0;
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent ignoredAgent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        if (deletedAt != null) {
            throw new IllegalStateException("Cannot apply command to deleted aggregate");
        }

        return switch (cmd) {
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent2.of(
                    c.getTitle(),
                    c.getDescription(),
                    c.getDeletedAt().orElse(null)
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            case UpdateTitleCmd c -> ApplyCommandResult.of(TitleUpdatedEvent.of(c.getTitle()));
            case UpdateDescriptionCmd c -> ApplyCommandResult.of(DescriptionUpdatedEvent.of(c.getDescription()));
            case SnapshotCmd ignored -> ApplyCommandResult.of(createManualSnapshotEvent());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return (switch (event) {
            case CreatedEvent2 e -> withId(metadata.getAggregateId().getValue())
                    .withTitle(e.getTitle())
                    .withDescription(e.getDescription())
                    .withDeletedAt(e.getDeletedAt().orElse(null))
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case TitleUpdatedEvent e -> withTitle(e.getTitle());
            case DescriptionUpdatedEvent e -> withDescription(e.getDescription());
            case SnapshotEvent e -> restoreFromSnapshot(e, metadata);
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(metadata.getAggregateVersion());
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isCreated() {
        return createdAt != null;
    }

    private SnapshotEvent createManualSnapshotEvent() {
        Map<String, Object> state = new HashMap<>();
        state.put("title", title);
        state.put("description", description);
        state.put("createdAt", createdAt.toString());
        if (deletedAt != null) {
            state.put("deletedAt", deletedAt.toString());
        }
        return SnapshotEvent.of(state);
    }

    private ManualSnapshotAggregate restoreFromSnapshot(SnapshotEvent event, EventMetadata metadata) {
        Map<String, Object> state = event.getState();
        return withId(metadata.getAggregateId().getValue())
                .withTitle((String) state.get("title"))
                .withDescription((String) state.get("description"))
                .withCreatedAt(Instant.parse((String) state.get("createdAt")))
                .withDeletedAt(state.containsKey("deletedAt") ? Instant.parse((String) state.get("deletedAt")) : null);
    }

}
