package de.bennyboer.kicherkrabbe.embroideries.create;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.ImageId;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    EmbroideryName name;

    ImageId image;

    Set<EmbroideryCategoryId> categories;

    public static CreatedEvent of(EmbroideryName name, ImageId image, Set<EmbroideryCategoryId> categories) {
        notNull(name, "Embroidery name must be given");
        notNull(image, "Image must be given");
        notNull(categories, "Categories must be given");

        return new CreatedEvent(name, image, categories);
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
