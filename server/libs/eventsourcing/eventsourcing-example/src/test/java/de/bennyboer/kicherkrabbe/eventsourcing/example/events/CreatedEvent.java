package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.Value;

@Value
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    String title;

    String description;

    private CreatedEvent(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static CreatedEvent of(String title, String description) {
        return new CreatedEvent(title, description);
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
