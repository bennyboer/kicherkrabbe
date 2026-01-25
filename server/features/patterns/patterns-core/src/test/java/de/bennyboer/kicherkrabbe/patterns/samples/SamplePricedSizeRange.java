package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.PricedSizeRange;
import lombok.Builder;

@Builder
public class SamplePricedSizeRange {

    @Builder.Default
    private long from = 80L;

    @Builder.Default
    private Long to = 86L;

    @Builder.Default
    private String unit = "EU";

    @Builder.Default
    private Money price = Money.euro(2900);

    public PricedSizeRange toValue() {
        return PricedSizeRange.of(from, to, unit, price);
    }

}
