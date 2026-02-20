package de.bennyboer.kicherkrabbe.offers.archive;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ArchivedEvent implements Event {

    public static final EventName NAME = EventName.of("ARCHIVED");

    public static final Version VERSION = Version.zero();

    public static ArchivedEvent of() {
        return new ArchivedEvent();
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
