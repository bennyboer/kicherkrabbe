package de.bennyboer.kicherkrabbe.products.counter;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementCmd;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementedEvent;
import de.bennyboer.kicherkrabbe.products.counter.init.InitCmd;
import de.bennyboer.kicherkrabbe.products.counter.init.InitEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Counter implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("PRODUCT_COUNTER");

    @SnapshotExclude
    CounterId id;

    @SnapshotExclude
    Version version;

    long value;

    Instant initAt;

    public static Counter init() {
        return new Counter(
                null,
                Version.zero(),
                0L,
                null
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        check(isInitialized() || cmd instanceof InitCmd, "Cannot apply command to not yet initialized counter");

        return switch (cmd) {
            case InitCmd ignored -> ApplyCommandResult.of(InitEvent.of());
            case IncrementCmd ignored -> ApplyCommandResult.of(IncrementedEvent.of());
            default -> throw new IllegalArgumentException("Unknown command " + cmd.getClass().getSimpleName());
        };
    }

    @Override
    public Aggregate apply(Event event, EventMetadata metadata) {
        var id = CounterId.of(metadata.getAggregateId().getValue());
        Version version = metadata.getAggregateVersion();

        return (switch (event) {
            case InitEvent ignored -> withId(id)
                    .withInitAt(metadata.getDate());
            case IncrementedEvent ignored -> withId(id).withValue(value + 1);
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

    private boolean isInitialized() {
        return initAt != null;
    }

}
