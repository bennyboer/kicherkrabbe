package de.bennyboer.kicherkrabbe.products.counter;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.command.SnapshotCmd;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementCmd;
import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementedEvent;
import de.bennyboer.kicherkrabbe.products.counter.init.InitCmd;
import de.bennyboer.kicherkrabbe.products.counter.init.InitEvent;
import de.bennyboer.kicherkrabbe.products.counter.snapshot.SnapshottedEvent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Counter implements Aggregate {

    public static AggregateType TYPE = AggregateType.of("PRODUCT_COUNTER");

    CounterId id;

    Version version;

    long value;

    public static Counter init() {
        return new Counter(
                null,
                Version.zero(),
                0L
        );
    }

    @Override
    public ApplyCommandResult apply(Command cmd, Agent agent) {
        return switch (cmd) {
            case SnapshotCmd ignored -> ApplyCommandResult.of(SnapshottedEvent.of(getValue()));
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
            case SnapshottedEvent e -> withId(id).withValue(e.getValue());
            case InitEvent ignored -> withId(id);
            case IncrementedEvent ignored -> withId(id).withValue(value + 1);
            default -> throw new IllegalArgumentException("Unknown event " + event.getClass().getSimpleName());
        }).withVersion(version);
    }

}
