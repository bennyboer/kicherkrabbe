package de.bennyboer.kicherkrabbe.fabrics.unpublish;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UnpublishedEvent implements Event {

    public static final EventName NAME = EventName.of("UNPUBLISHED");

    public static final Version VERSION = Version.zero();

    public static UnpublishedEvent of() {
        return new UnpublishedEvent();
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
