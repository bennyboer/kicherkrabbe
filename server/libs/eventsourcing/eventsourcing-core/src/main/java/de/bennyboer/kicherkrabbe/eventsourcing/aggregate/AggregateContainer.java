package de.bennyboer.kicherkrabbe.eventsourcing.aggregate;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import reactor.util.annotation.Nullable;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class AggregateContainer implements Aggregate {

    Aggregate aggregate;

    Version lastSnapshotVersion;

    @Nullable
    EventMetadata lastEventMetadata;

    public static AggregateContainer init(Aggregate aggregate) {
        return new AggregateContainer(aggregate, Version.zero(), null);
    }

    @Override
    public ApplyCommandResult apply(Command command, Agent agent) {
        return aggregate.apply(command, agent);
    }

    @Override
    public AggregateContainer apply(Event event, EventMetadata metadata) {
        var updatedContainer = withAggregate(aggregate.apply(event, metadata))
                .withLastEventMetadata(metadata);

        if (metadata.isSnapshot()) {
            updatedContainer = updatedContainer.withLastSnapshotVersion(metadata.getAggregateVersion());
        }

        return updatedContainer;
    }

    @Override
    public int getCountOfEventsToSnapshotAfter() {
        return aggregate.getCountOfEventsToSnapshotAfter();
    }

    public Optional<EventMetadata> getLastEventMetadata() {
        return Optional.ofNullable(lastEventMetadata);
    }

    public boolean hasSeenEvents() {
        return getLastEventMetadata().isPresent();
    }

    public Version getVersion() {
        return getLastEventMetadata()
                .map(EventMetadata::getAggregateVersion)
                .orElse(Version.zero());
    }

    public long getVersionCountFromLastSnapshot() {
        return getVersion().getValue() - lastSnapshotVersion.getValue() + 1;
    }

}
