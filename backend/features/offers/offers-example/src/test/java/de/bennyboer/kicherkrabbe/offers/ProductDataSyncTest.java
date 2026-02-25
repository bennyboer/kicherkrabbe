package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOffer;
import de.bennyboer.kicherkrabbe.offers.samples.SampleProductForLookup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductDataSyncTest extends OffersModuleTest {

    @Test
    void shouldUpdateOfferWhenProductLinkIsAdded() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var patternLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern Name"));
        setUpProduct(SampleProductForLookup.builder()
                .links(Set.of(patternLink))
                .build());
        String offerId = createOffer(SampleOffer.builder().build(), agent);

        var fabricLink = Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Cotton Fabric"));
        addProductLink("PRODUCT_ID", 1L, fabricLink);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getLinks().getLinks()).containsExactlyInAnyOrder(patternLink, fabricLink);
    }

    @Test
    void shouldUpdateOfferWhenProductLinkIsRemoved() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var patternLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Pattern Name"));
        var fabricLink = Link.of(LinkType.FABRIC, LinkId.of("FABRIC_ID"), LinkName.of("Cotton Fabric"));
        setUpProduct(SampleProductForLookup.builder()
                .links(Set.of(patternLink, fabricLink))
                .build());
        String offerId = createOffer(SampleOffer.builder().build(), agent);

        removeProductLink("PRODUCT_ID", 1L, LinkType.FABRIC, "FABRIC_ID");

        var offer = getOffer(offerId, agent);
        assertThat(offer.getLinks().getLinks()).containsExactly(patternLink);
    }

    @Test
    void shouldUpdateOfferWhenProductLinkIsRenamed() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var originalLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Old Name"));
        setUpProduct(SampleProductForLookup.builder()
                .links(Set.of(originalLink))
                .build());
        String offerId = createOffer(SampleOffer.builder().build(), agent);

        var renamedLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("New Name"));
        updateProductLink("PRODUCT_ID", 1L, renamedLink);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getLinks().getLinks()).containsExactly(renamedLink);
    }

    @Test
    void shouldUpdateOfferWhenProductFabricCompositionChanges() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder()
                .fabricCompositionItems(Set.of(
                        FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
                ))
                .build());
        String offerId = createOffer(SampleOffer.builder().build(), agent);

        var updatedComposition = FabricComposition.of(Set.of(
                FabricCompositionItem.of(FabricType.POLYESTER, LowPrecisionFloat.of(6000L)),
                FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(4000L))
        ));
        updateProductFabricComposition("PRODUCT_ID", 1L, updatedComposition);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getFabricComposition().getItems()).containsExactlyInAnyOrderElementsOf(
                updatedComposition.getItems()
        );
    }

    @Test
    void shouldUpdateOfferWhenProductNumberChanges() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder()
                .number(ProductNumber.of("P-001"))
                .build());
        String offerId = createOffer(SampleOffer.builder().build(), agent);

        updateProductNumber("PRODUCT_ID", 1L, ProductNumber.of("P-999"));

        var offer = getOffer(offerId, agent);
        assertThat(offer.getProduct().getNumber()).isEqualTo(ProductNumber.of("P-999"));
    }

    @Test
    void shouldUpdateProductLookupWhenProductImagesChange() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpProduct(SampleProductForLookup.builder()
                .images(List.of(ImageId.of("IMG_1"), ImageId.of("IMG_2")))
                .build());

        var productsPage = getProductsForOfferCreation("", 0, 10, agent);
        assertThat(productsPage.getResults().getFirst().getImages())
                .containsExactly(ImageId.of("IMG_1"), ImageId.of("IMG_2"));

        updateProductImages("PRODUCT_ID", 1L, List.of(ImageId.of("IMG_3")));

        productsPage = getProductsForOfferCreation("", 0, 10, agent);
        assertThat(productsPage.getResults().getFirst().getImages())
                .containsExactly(ImageId.of("IMG_3"));
    }

    @Test
    void shouldUpdateMultipleOffersForSameProduct() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var originalLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Old Name"));
        setUpProduct(SampleProductForLookup.builder()
                .links(Set.of(originalLink))
                .build());
        String offerId1 = createOffer(SampleOffer.builder().title("Offer 1").build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().title("Offer 2").build(), agent);

        var renamedLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("New Name"));
        updateProductLink("PRODUCT_ID", 1L, renamedLink);

        var offer1 = getOffer(offerId1, agent);
        var offer2 = getOffer(offerId2, agent);
        assertThat(offer1.getLinks().getLinks()).containsExactly(renamedLink);
        assertThat(offer2.getLinks().getLinks()).containsExactly(renamedLink);
    }

}
