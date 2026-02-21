package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import lombok.Builder;

import java.util.List;
import java.util.Set;

@Builder
public class SamplePublishedOffer {

    @Builder.Default
    private String id = "OFFER_ID";

    @Builder.Default
    private String title = "Sample Offer";

    @Builder.Default
    private String size = "M";

    @Builder.Default
    private Set<String> categoryIds = Set.of();

    @Builder.Default
    private List<String> imageIds = List.of("IMAGE_ID");

    @Builder.Default
    private Set<Link> links = Set.of(Link.of(LinkType.PATTERN, LinkId.of("LINK_ID"), LinkName.of("Link")));

    @Builder.Default
    private Set<FabricCompositionItem> fabricCompositionItems = Set.of(
            FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
    );

    @Builder.Default
    private Pricing pricing = Pricing.of(Money.of(1999L, Currency.euro()));

    @Builder.Default
    private Notes notes = Notes.of(Note.of("Description"), null, null, null);

    public PublishedOffer toModel() {
        return PublishedOffer.of(
                OfferId.of(id),
                OfferTitle.of(title),
                OfferSize.of(size),
                categoryIds.stream().map(OfferCategoryId::of).collect(java.util.stream.Collectors.toSet()),
                imageIds.stream().map(ImageId::of).toList(),
                Links.of(links),
                FabricComposition.of(fabricCompositionItems),
                pricing,
                notes
        );
    }

}
