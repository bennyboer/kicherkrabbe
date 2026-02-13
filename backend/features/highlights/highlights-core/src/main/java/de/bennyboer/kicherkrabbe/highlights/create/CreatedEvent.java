package de.bennyboer.kicherkrabbe.highlights.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.highlights.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    ImageId imageId;

    long sortOrder;

    public static CreatedEvent of(ImageId imageId, long sortOrder) {
        notNull(imageId, "Image ID must be given");

        return new CreatedEvent(imageId, sortOrder);
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
