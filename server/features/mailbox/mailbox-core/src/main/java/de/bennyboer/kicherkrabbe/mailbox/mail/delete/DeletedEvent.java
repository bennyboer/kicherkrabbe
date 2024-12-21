package de.bennyboer.kicherkrabbe.mailbox.mail.delete;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailbox.mail.Origin;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DeletedEvent implements Event {

    public static final EventName NAME = EventName.of("DELETED");

    public static final Version VERSION = Version.zero();

    /*
     * The origin of the mail that was deleted.
     * We include it in the event to be able to delete the mail in its origin as well.
     */
    Origin origin;

    public static DeletedEvent of(Origin origin) {
        notNull(origin, "Origin must be given");

        return new DeletedEvent(origin);
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
