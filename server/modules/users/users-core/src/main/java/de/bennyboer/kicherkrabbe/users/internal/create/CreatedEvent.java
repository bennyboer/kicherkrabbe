package de.bennyboer.kicherkrabbe.users.internal.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.users.internal.FullName;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    FullName name;

    Mail mail;

    public static CreatedEvent of(FullName name, Mail mail) {
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new CreatedEvent(name, mail);
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
