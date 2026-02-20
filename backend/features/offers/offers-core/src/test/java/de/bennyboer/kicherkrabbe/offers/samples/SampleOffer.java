package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.ImageId;
import de.bennyboer.kicherkrabbe.offers.Note;
import de.bennyboer.kicherkrabbe.offers.Notes;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
public class SampleOffer {

    @Builder.Default
    private String productId = "PRODUCT_ID";

    @Singular
    private List<String> imageIds;

    @Builder.Default
    private String description = "description";

    private String contains;

    private String care;

    private String safety;

    @Builder.Default
    private long priceAmount = 1999L;

    @Builder.Default
    private Currency priceCurrency = Currency.euro();

    public ProductId getProductId() {
        return ProductId.of(productId);
    }

    public List<ImageId> getImageIds() {
        if (imageIds.isEmpty()) {
            return List.of(ImageId.of("IMAGE_ID"));
        }
        return imageIds.stream().map(ImageId::of).toList();
    }

    public Notes getNotes() {
        return Notes.of(
                Note.of(description),
                contains != null ? Note.of(contains) : null,
                care != null ? Note.of(care) : null,
                safety != null ? Note.of(safety) : null
        );
    }

    public Money getPrice() {
        return Money.of(priceAmount, priceCurrency);
    }

}
