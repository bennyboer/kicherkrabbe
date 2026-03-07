package de.bennyboer.kicherkrabbe.fabrics.update.images;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.fabrics.ImageId;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ImagesUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("IMAGES_UPDATED");

    public static final Version VERSION = Version.zero();

    @Nullable
    ImageId image;

    List<ImageId> exampleImages;

    public static ImagesUpdatedEvent of(@Nullable ImageId image, List<ImageId> exampleImages) {
        notNull(exampleImages, "Example images must be given");

        return new ImagesUpdatedEvent(image, exampleImages);
    }

    public Optional<ImageId> getImage() {
        return Optional.ofNullable(image);
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
