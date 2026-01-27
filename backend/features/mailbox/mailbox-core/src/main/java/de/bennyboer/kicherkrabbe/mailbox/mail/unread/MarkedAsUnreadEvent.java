package de.bennyboer.kicherkrabbe.mailbox.mail.unread;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MarkedAsUnreadEvent implements Event {

    public static final EventName NAME = EventName.of("MARKED_AS_UNREAD");

    public static final Version VERSION = Version.zero();

    public static MarkedAsUnreadEvent of() {
        return new MarkedAsUnreadEvent();
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
