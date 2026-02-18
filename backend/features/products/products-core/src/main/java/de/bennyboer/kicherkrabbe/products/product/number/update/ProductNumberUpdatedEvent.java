package de.bennyboer.kicherkrabbe.products.product.number.update;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.products.product.ProductNumber;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ProductNumberUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("PRODUCT_NUMBER_UPDATED");

    public static final Version VERSION = Version.zero();

    ProductNumber number;

    public static ProductNumberUpdatedEvent of(ProductNumber number) {
        notNull(number, "Product number must be given");

        return new ProductNumberUpdatedEvent(number);
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
