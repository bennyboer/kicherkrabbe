package de.bennyboer.kicherkrabbe.inquiries.send;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.inquiries.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SentEvent implements Event {

    public static final EventName NAME = EventName.of("SENT");

    public static final Version VERSION = Version.zero();

    RequestId requestId;

    Sender sender;

    Subject subject;

    Message message;

    Fingerprint fingerprint;

    public static SentEvent of(
            RequestId requestId,
            Sender sender,
            Subject subject,
            Message message,
            Fingerprint fingerprint
    ) {
        notNull(requestId, "Request ID must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(message, "Message must be given");
        notNull(fingerprint, "Fingerprint must be given");

        return new SentEvent(requestId, sender, subject, message, fingerprint);
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
