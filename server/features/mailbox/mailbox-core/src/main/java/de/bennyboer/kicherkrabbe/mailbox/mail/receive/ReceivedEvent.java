package de.bennyboer.kicherkrabbe.mailbox.mail.receive;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.mailbox.mail.Content;
import de.bennyboer.kicherkrabbe.mailbox.mail.Origin;
import de.bennyboer.kicherkrabbe.mailbox.mail.Sender;
import de.bennyboer.kicherkrabbe.mailbox.mail.Subject;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ReceivedEvent implements Event {

    public static final EventName NAME = EventName.of("RECEIVED");

    public static final Version VERSION = Version.zero();

    Origin origin;

    Sender sender;

    Subject subject;

    Content content;

    public static ReceivedEvent of(
            Origin origin,
            Sender sender,
            Subject subject,
            Content content
    ) {
        notNull(origin, "Origin must be given");
        notNull(sender, "Sender must be given");
        notNull(subject, "Subject must be given");
        notNull(content, "Content must be given");

        return new ReceivedEvent(
                origin,
                sender,
                subject,
                content
        );
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
