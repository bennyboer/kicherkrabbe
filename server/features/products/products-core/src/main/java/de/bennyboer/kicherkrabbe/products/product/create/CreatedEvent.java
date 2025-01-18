package de.bennyboer.kicherkrabbe.products.product.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    ProductNumber number;

    List<ImageId> images;

    Links links;

    FabricComposition fabricComposition;

    Notes notes;

    Instant producedAt;

    public static CreatedEvent of(
            ProductNumber number,
            List<ImageId> images,
            Links links,
            FabricComposition fabricComposition,
            Notes notes,
            Instant producedAt
    ) {
        notNull(number, "Number must be given");
        notNull(images, "Images must be given");
        notNull(links, "Links must be given");
        notNull(fabricComposition, "Fabric composition must be given");
        notNull(notes, "Notes must be given");
        notNull(producedAt, "Produced at must be given");

        return new CreatedEvent(
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt
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
