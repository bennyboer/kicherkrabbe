package de.bennyboer.kicherkrabbe.eventsourcing.event;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EventWithMetadata {

    Event event;

    EventMetadata metadata;

    public static EventWithMetadata of(Event event, EventMetadata metadata) {
        notNull(event, "Event must be given");
        notNull(metadata, "EventMetadata must be given");

        return new EventWithMetadata(event, metadata);
    }

}
