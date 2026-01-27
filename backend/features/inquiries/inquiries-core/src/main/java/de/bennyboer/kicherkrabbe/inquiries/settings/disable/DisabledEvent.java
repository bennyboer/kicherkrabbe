package de.bennyboer.kicherkrabbe.inquiries.settings.disable;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DisabledEvent implements Event {

    public static final EventName NAME = EventName.of("DISABLED");

    public static final Version VERSION = Version.zero();

    public static DisabledEvent of() {
        return new DisabledEvent();
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
