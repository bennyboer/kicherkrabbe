package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.PatternExtra;
import de.bennyboer.kicherkrabbe.patterns.PatternExtraName;
import lombok.Builder;

@Builder
public class SamplePatternExtra {

    @Builder.Default
    private String name = "Sample Extra";

    @Builder.Default
    private Money price = Money.euro(200);

    public PatternExtra toValue() {
        return PatternExtra.of(PatternExtraName.of(name), price);
    }

}
