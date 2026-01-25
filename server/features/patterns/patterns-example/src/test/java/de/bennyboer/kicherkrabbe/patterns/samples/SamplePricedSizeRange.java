package de.bennyboer.kicherkrabbe.patterns.samples;

import de.bennyboer.kicherkrabbe.patterns.http.api.PricedSizeRangeDTO;
import lombok.Builder;

@Builder
public class SamplePricedSizeRange {

    @Builder.Default
    private long from = 80;

    @Builder.Default
    private Long to = 86L;

    @Builder.Default
    private String unit = null;

    @Builder.Default
    private SampleMoney price = SampleMoney.builder().build();

    public PricedSizeRangeDTO toDTO() {
        var dto = new PricedSizeRangeDTO();
        dto.from = from;
        dto.to = to;
        dto.unit = unit;
        dto.price = price.toDTO();
        return dto;
    }

}
