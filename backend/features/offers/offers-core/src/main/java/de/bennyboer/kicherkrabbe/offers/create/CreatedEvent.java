package de.bennyboer.kicherkrabbe.offers.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.ImageId;
import de.bennyboer.kicherkrabbe.offers.Notes;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    ProductId productId;

    List<ImageId> images;

    Notes notes;

    Money price;

    public static CreatedEvent of(
            ProductId productId,
            List<ImageId> images,
            Notes notes,
            Money price
    ) {
        notNull(productId, "Product ID must be given");
        notNull(images, "Images must be given");
        notNull(notes, "Notes must be given");
        notNull(price, "Price must be given");

        return new CreatedEvent(productId, images, notes, price);
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
