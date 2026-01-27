package de.bennyboer.kicherkrabbe.eventsourcing.event.listener;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Map;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class HandleableEvent {

    EventMetadata metadata;

    EventName eventName;

    Version eventVersion;

    Map<String, Object> event;

    public static HandleableEvent of(
            EventMetadata metadata,
            EventName eventName,
            Version eventVersion,
            Map<String, Object> event
    ) {
        notNull(metadata, "Metadata must be given");
        notNull(eventName, "Event name must be given");
        notNull(eventVersion, "Event version must be given");
        notNull(event, "Event must be given");

        return new HandleableEvent(metadata, eventName, eventVersion, event);
    }

}
