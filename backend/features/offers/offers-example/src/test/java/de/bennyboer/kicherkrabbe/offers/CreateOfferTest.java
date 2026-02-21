package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import de.bennyboer.kicherkrabbe.offers.samples.SampleProductForLookup;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateOfferTest extends OffersModuleTest {

    @Test
    void shouldCreateOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var patternLink = Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Test Pattern"));
        setUpProduct(SampleProductForLookup.builder()
                .links(Set.of(patternLink))
                .build());

        var notes = new NotesDTO();
        notes.description = "A nice product";
        notes.contains = "100% Cotton";
        notes.care = "Machine wash 30°C";
        notes.safety = "Keep away from fire";

        var price = new MoneyDTO();
        price.amount = 2999L;
        price.currency = "EUR";

        String offerId = createOffer(
                "Test Offer",
                "L",
                Set.of(),
                "PRODUCT_ID",
                List.of("IMAGE_1", "IMAGE_2"),
                notes,
                price,
                agent
        );

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId));
        assertThat(offer.getVersion()).isEqualTo(Version.zero());
        assertThat(offer.getTitle()).isEqualTo(OfferTitle.of("Test Offer"));
        assertThat(offer.getSize()).isEqualTo(OfferSize.of("L"));
        assertThat(offer.getCategories()).isEmpty();
        assertThat(offer.getProduct().getId()).isEqualTo(ProductId.of("PRODUCT_ID"));
        assertThat(offer.getProduct().getNumber()).isEqualTo(ProductNumber.of("P-001"));
        assertThat(offer.getImages()).containsExactly(ImageId.of("IMAGE_1"), ImageId.of("IMAGE_2"));
        assertThat(offer.getLinks().getLinks()).containsExactly(
                Link.of(LinkType.PATTERN, LinkId.of("PATTERN_ID"), LinkName.of("Test Pattern"))
        );
        assertThat(offer.getFabricComposition().getItems()).containsExactly(
                FabricCompositionItem.of(FabricType.COTTON, LowPrecisionFloat.of(10000L))
        );
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(2999L, Currency.euro()));
        assertThat(offer.getPricing().getDiscountedPrice()).isEmpty();
        assertThat(offer.getPricing().getPriceHistory()).isEmpty();
        assertThat(offer.getNotes().getDescription()).isEqualTo(Note.of("A nice product"));
        assertThat(offer.getNotes().getContains()).contains(Note.of("100% Cotton"));
        assertThat(offer.getNotes().getCare()).contains(Note.of("Machine wash 30°C"));
        assertThat(offer.getNotes().getSafety()).contains(Note.of("Keep away from fire"));
        assertThat(offer.isPublished()).isFalse();
        assertThat(offer.isReserved()).isFalse();
    }

    @Test
    void shouldNotCreateOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> createSampleOffer(Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleOffers() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();

        createSampleOffer(agent);
        createSampleOffer(agent);
        createSampleOffer(agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(3);
        var offerIds = offers.stream().map(OfferDetails::getId).toList();
        assertThat(offerIds).doesNotHaveDuplicates();
    }

}
