package de.bennyboer.kicherkrabbe.patterns;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternVariant {

    PatternVariantName name;

    Set<PricedSizeRange> pricedSizeRanges;

    public static PatternVariant of(PatternVariantName name, Set<PricedSizeRange> pricedSizeRanges) {
        notNull(name, "Pattern variant name must be given");
        notNull(pricedSizeRanges, "Priced size ranges must be given");
        check(!pricedSizeRanges.isEmpty(), "Priced size ranges must not be empty");

        return new PatternVariant(name, pricedSizeRanges);
    }

}
