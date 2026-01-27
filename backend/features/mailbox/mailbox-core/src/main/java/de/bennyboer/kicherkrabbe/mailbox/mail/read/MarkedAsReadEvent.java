package de.bennyboer.kicherkrabbe.mailbox.mail.read;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MarkedAsReadEvent implements Event {

    public static final EventName NAME = EventName.of("MARKED_AS_READ");

    public static final Version VERSION = Version.zero();

    public static MarkedAsReadEvent of() {
        return new MarkedAsReadEvent();
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
