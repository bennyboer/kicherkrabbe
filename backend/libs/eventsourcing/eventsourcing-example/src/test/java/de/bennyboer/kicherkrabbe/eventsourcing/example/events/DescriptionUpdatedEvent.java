package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.Value;

@Value
public class DescriptionUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("DESCRIPTION_UPDATED");

    public static final Version VERSION = Version.zero();

    String description;

    private DescriptionUpdatedEvent(String description) {
        this.description = description;
    }

    public static DescriptionUpdatedEvent of(String description) {
        return new DescriptionUpdatedEvent(description);
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
