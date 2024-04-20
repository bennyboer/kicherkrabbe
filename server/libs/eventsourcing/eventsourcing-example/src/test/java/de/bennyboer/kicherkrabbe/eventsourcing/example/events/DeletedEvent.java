package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.Value;

@Value
public class DeletedEvent implements Event {

    public static final EventName NAME = EventName.of("DELETED");

    public static final Version VERSION = Version.zero();

    public static DeletedEvent of() {
        return new DeletedEvent();
    }

    @Override
    public EventName getEventName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

}
