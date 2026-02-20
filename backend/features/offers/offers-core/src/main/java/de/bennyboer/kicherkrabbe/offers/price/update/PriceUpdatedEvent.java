package de.bennyboer.kicherkrabbe.offers.price.update;

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
public class PriceUpdatedEvent implements Event {

    public static final EventName NAME = EventName.of("PRICE_UPDATED");

    public static final Version VERSION = Version.zero();

    Money price;

    public static PriceUpdatedEvent of(Money price) {
        notNull(price, "Price must be given");

        return new PriceUpdatedEvent(price);
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
