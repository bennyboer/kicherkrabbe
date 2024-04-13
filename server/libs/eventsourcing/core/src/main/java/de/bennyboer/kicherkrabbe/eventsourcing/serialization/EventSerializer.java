package de.bennyboer.kicherkrabbe.eventsourcing.serialization;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;

public interface EventSerializer {

    String serialize(Event event);

    Event deserialize(String event, EventName eventName, Version eventVersion);

}
