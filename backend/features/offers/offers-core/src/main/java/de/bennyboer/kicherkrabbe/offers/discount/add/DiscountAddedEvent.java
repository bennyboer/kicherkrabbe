package de.bennyboer.kicherkrabbe.offers.discount.add;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.money.Money;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DiscountAddedEvent implements Event {

    public static final EventName NAME = EventName.of("DISCOUNT_ADDED");

    public static final Version VERSION = Version.zero();

    Money discountedPrice;

    public static DiscountAddedEvent of(Money discountedPrice) {
        notNull(discountedPrice, "Discounted price must be given");

        return new DiscountAddedEvent(discountedPrice);
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
