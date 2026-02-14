package de.bennyboer.kicherkrabbe.credentials.create;

import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.credentials.EncodedPassword;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    Name name;

    EncodedPassword encodedPassword;

    UserId userId;

    public static CreatedEvent of(Name name, EncodedPassword encodedPassword, UserId userId) {
        notNull(name, "Name must be given");
        notNull(encodedPassword, "Encoded password must be given");
        notNull(userId, "User ID must be given");

        return new CreatedEvent(name, encodedPassword, userId);
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
