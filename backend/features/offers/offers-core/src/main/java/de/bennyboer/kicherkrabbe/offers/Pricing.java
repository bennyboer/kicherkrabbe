package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.money.Money;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class Pricing {

    Money price;

    @Nullable
    Money discountedPrice;

    List<PriceHistoryEntry> priceHistory;

    public static Pricing of(Money price) {
        notNull(price, "Price must be given");

        return new Pricing(price, null, List.of());
    }

    public static Pricing of(Money price, @Nullable Money discountedPrice, List<PriceHistoryEntry> priceHistory) {
        notNull(price, "Price must be given");
        notNull(priceHistory, "Price history must be given");

        if (discountedPrice != null) {
            check(
                    discountedPrice.getCurrency().equals(price.getCurrency()),
                    "Discounted price currency must match price currency"
            );
            check(discountedPrice.getAmount() < price.getAmount(), "Discounted price must be less than price");
        }

        return new Pricing(price, discountedPrice, priceHistory);
    }

    public Optional<Money> getDiscountedPrice() {
        return Optional.ofNullable(discountedPrice);
    }

    public Pricing withUpdatedPrice(Money newPrice, Instant timestamp) {
        notNull(newPrice, "New price must be given");
        notNull(timestamp, "Timestamp must be given");

        var updatedHistory = new ArrayList<>(priceHistory);
        updatedHistory.add(PriceHistoryEntry.of(price, timestamp));

        return withPrice(newPrice)
                .withDiscountedPrice(null)
                .withPriceHistory(List.copyOf(updatedHistory));
    }

    public Pricing withDiscount(Money discountedPrice) {
        notNull(discountedPrice, "Discounted price must be given");
        check(
                discountedPrice.getCurrency().equals(price.getCurrency()),
                "Discounted price currency must match price currency"
        );
        check(discountedPrice.getAmount() < price.getAmount(), "Discounted price must be less than price");

        return withDiscountedPrice(discountedPrice);
    }

    public Pricing withoutDiscount() {
        return withDiscountedPrice(null);
    }

}
