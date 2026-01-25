package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.MoneyDTO;
import lombok.Builder;

@Builder
public class SampleMoney {

    @Builder.Default
    private long amount = 1000;

    @Builder.Default
    private String currency = "EUR";

    public MoneyDTO toDTO() {
        var dto = new MoneyDTO();
        dto.amount = amount;
        dto.currency = currency;
        return dto;
    }

}
