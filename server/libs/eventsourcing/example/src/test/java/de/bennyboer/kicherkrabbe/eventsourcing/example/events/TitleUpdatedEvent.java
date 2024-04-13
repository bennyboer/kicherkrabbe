package de.bennyboer.kicherkrabbe.eventsourcing.example.events;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.Value;

@Value
public class TitleUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("TITLE_UPDATED");

    public static final Version VERSION = Version.zero();

    String title;

    private TitleUpdatedEvent(String title) {
        this.title = title;
    }

    public static TitleUpdatedEvent of(String title) {
        return new TitleUpdatedEvent(title);
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
