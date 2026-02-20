package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Builder
public class SampleOfferDetails {

    @Builder.Default
    private String id = "OFFER_ID";

    @Builder.Default
    private long version = 0L;

    @Builder.Default
    private String productId = "PRODUCT_ID";

    @Builder.Default
    private String productNumber = "P-001";

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

    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private boolean reserved = false;

    @Builder.Default
    private Instant createdAt = Instant.parse("2024-03-12T12:00:00.00Z");

    private Instant archivedAt;

    public OfferDetails toModel() {
        return OfferDetails.of(
                OfferId.of(id),
                Version.of(version),
                Product.of(ProductId.of(productId), ProductNumber.of(productNumber)),
                imageIds.stream().map(ImageId::of).toList(),
                Links.of(links),
                FabricComposition.of(fabricCompositionItems),
                pricing,
                notes,
                published,
                reserved,
                createdAt,
                archivedAt
        );
    }

}
