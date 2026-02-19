package de.bennyboer.kicherkrabbe.fabrics.update.images;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
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

    ImageId image;

    List<ImageId> exampleImages;

    public static ImagesUpdatedEvent of(ImageId image, List<ImageId> exampleImages) {
        notNull(image, "Image must be given");
        notNull(exampleImages, "Example images must be given");

        return new ImagesUpdatedEvent(image, exampleImages);
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
