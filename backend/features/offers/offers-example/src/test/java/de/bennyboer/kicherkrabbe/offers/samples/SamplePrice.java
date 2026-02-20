package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import lombok.Builder;

@Builder
public class SamplePrice {

    @Builder.Default
    private long amount = 1999L;

    @Builder.Default
    private String currency = "EUR";

    public MoneyDTO toDTO() {
        var dto = new MoneyDTO();
        dto.amount = amount;
        dto.currency = currency;
        return dto;
    }

}
