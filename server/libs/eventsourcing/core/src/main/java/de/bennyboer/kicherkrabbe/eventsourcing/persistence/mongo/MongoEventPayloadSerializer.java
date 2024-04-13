package de.bennyboer.kicherkrabbe.eventsourcing.persistence.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

import java.util.Map;

public interface MongoEventPayloadSerializer {

    Map<String, Object> serialize(Event event);

    Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload);

}
