package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleOffer {

    @Builder.Default
    private String title = "Sample Offer";

    @Builder.Default
    private String size = "M";

    @Singular
    private Set<String> categoryIds;

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

    public OfferTitle getTitle() {
        return OfferTitle.of(title);
    }

    public OfferSize getSize() {
        return OfferSize.of(size);
    }

    public Set<OfferCategoryId> getCategories() {
        return categoryIds.stream().map(OfferCategoryId::of).collect(Collectors.toSet());
    }

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
