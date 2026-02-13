package de.bennyboer.kicherkrabbe.highlights.image;

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
public class ImageUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("IMAGE_UPDATED");

    public static final Version VERSION = Version.zero();

    ImageId imageId;

    public static ImageUpdatedEvent of(ImageId imageId) {
        notNull(imageId, "Image ID must be given");

        return new ImageUpdatedEvent(imageId);
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
