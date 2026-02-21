package de.bennyboer.kicherkrabbe.offers.create;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreatedEvent implements Event {

    public static final EventName NAME = EventName.of("CREATED");

    public static final Version VERSION = Version.zero();

    OfferTitle title;

    OfferSize size;

    Set<OfferCategoryId> categories;

    ProductId productId;

    List<ImageId> images;

    Notes notes;

    Money price;

    public static CreatedEvent of(
            OfferTitle title,
            OfferSize size,
            Set<OfferCategoryId> categories,
            ProductId productId,
            List<ImageId> images,
            Notes notes,
            Money price
    ) {
        notNull(title, "Title must be given");
        notNull(size, "Size must be given");
        notNull(categories, "Categories must be given");
        notNull(productId, "Product ID must be given");
        notNull(images, "Images must be given");
        notNull(notes, "Notes must be given");
        notNull(price, "Price must be given");

        return new CreatedEvent(title, size, categories, productId, images, notes, price);
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
