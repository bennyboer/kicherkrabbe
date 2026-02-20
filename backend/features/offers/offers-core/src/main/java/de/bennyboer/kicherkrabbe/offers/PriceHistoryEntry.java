package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.money.Money;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PriceHistoryEntry {

    Money price;

    Instant timestamp;

    public static PriceHistoryEntry of(Money price, Instant timestamp) {
        notNull(price, "Price must be given");
        notNull(timestamp, "Timestamp must be given");

        return new PriceHistoryEntry(price, timestamp);
    }

}
