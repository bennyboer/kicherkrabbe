package de.bennyboer.kicherkrabbe.inquiries.send;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.Message;
import de.bennyboer.kicherkrabbe.inquiries.Sender;
import de.bennyboer.kicherkrabbe.inquiries.Subject;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SentEvent implements Event {

    public static final EventName NAME = EventName.of("SENT");

    public static final Version VERSION = Version.zero();

    Sender sender;

    Subject subject;

    Message message;

    public static SentEvent of(Sender sender, Subject subject, Message message) {
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");

        return new SentEvent(sender, subject, message);
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
