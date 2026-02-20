package de.bennyboer.kicherkrabbe.offers.api;

import jakarta.annotation.Nullable;

import java.util.List;

public class PricingDTO {

    public MoneyDTO price;

    @Nullable
    public MoneyDTO discountedPrice;

    public List<PriceHistoryEntryDTO> priceHistory;

}
