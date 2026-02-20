package de.bennyboer.kicherkrabbe.offers.images.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.offers.ImageId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ImagesUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("IMAGES_UPDATED");

    public static final Version VERSION = Version.zero();

    List<ImageId> images;

    public static ImagesUpdatedEvent of(List<ImageId> images) {
        notNull(images, "Images must be given");

        return new ImagesUpdatedEvent(images);
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
