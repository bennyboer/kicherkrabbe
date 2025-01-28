package de.bennyboer.kicherkrabbe.eventsourcing.example;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.CreateCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.DeleteCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateDescriptionCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.commands.UpdateTitleCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.example.events.*;
import jakarta.annotation.Nullable;
import lombok.Value;
import lombok.With;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
public class SampleAggregate implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("SAMPLE");

    String id;

    String title;

    String description;

    Instant createdAt;

    @Nullable
    Instant deletedAt;

    public static SampleAggregate init() {
        return new SampleAggregate(null, null, null, null, null);
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent ignoredAgent) {
        check(isCreated() || cmd instanceof CreateCmd, "Cannot apply command to not yet created aggregate");
        if (deletedAt != null) {
            throw new IllegalStateException("Cannot apply command to deleted aggregate");
        }

        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(
                    getTitle(),
                    getDescription(),
                    getCreatedAt(),
                    getDeletedAt()
            ));
            case CreateCmd c -> ApplyCommandResult.of(CreatedEvent2.of(
                    c.getTitle(),
                    c.getDescription(),
                    c.getDeletedAt().orElse(null)
            ));
            case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
            case UpdateTitleCmd c -> ApplyCommandResult.of(TitleUpdatedEvent.of(c.getTitle()));
            case UpdateDescriptionCmd c -> ApplyCommandResult.of(DescriptionUpdatedEvent.of(c.getDescription()));
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        return switch (event) {
            case SnapshottedEvent e -> withId(metadata.getAggregateId().getValue())
                    .withTitle(e.getTitle())
                    .withDescription(e.getDescription())
                    .withCreatedAt(e.getCreatedAt())
                    .withDeletedAt(e.getDeletedAt().orElse(null));
            case CreatedEvent2 e -> withId(metadata.getAggregateId().getValue())
                    .withTitle(e.getTitle())
                    .withDescription(e.getDescription())
                    .withDeletedAt(e.getDeletedAt().orElse(null))
                    .withCreatedAt(metadata.getDate());
            case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
            case TitleUpdatedEvent e -> withTitle(e.getTitle());
            case DescriptionUpdatedEvent e -> withDescription(e.getDescription());
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        };
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isCreated() {
        return createdAt != null;
    }

}
