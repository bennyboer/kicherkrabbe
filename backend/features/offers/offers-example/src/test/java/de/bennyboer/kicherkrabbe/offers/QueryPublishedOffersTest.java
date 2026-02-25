package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.api.*;
import de.bennyboer.kicherkrabbe.offers.samples.SampleOffer;
import de.bennyboer.kicherkrabbe.offers.samples.SamplePrice;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedOffersTest extends OffersModuleTest {

    @Test
    void shouldQueryPublishedOffers() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId1 = createSampleOffer(agent);
        String offerId2 = createSampleOffer(agent);

        var page = getPublishedOffers("", 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getResults()).isEmpty();

        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);

        page = getPublishedOffers("", 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults()).hasSize(2);
    }

    @Test
    void shouldQueryPublishedOffersAsAnonymousUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        var page = getPublishedOffers("", 0, 100, Agent.anonymous());
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).hasSize(1);
    }

    @Test
    void shouldNotSeeUnpublishedOffersInPublishedQuery() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId1 = createSampleOffer(agent);
        String offerId2 = createSampleOffer(agent);
        publishOffer(offerId1, 0L, agent);

        var page = getPublishedOffers("", 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getId()).isEqualTo(OfferId.of(offerId1));
    }

    @Test
    void shouldNotSeeArchivedOffersInPublishedQuery() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);
        archiveOffer(offerId, 2L, agent);

        var page = getPublishedOffers("", 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(0);
        assertThat(page.getResults()).isEmpty();
    }

    @Test
    void shouldGetPublishedOfferById() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        var offer = getPublishedOffer(offerId, Agent.anonymous());
        assertThat(offer).isNotNull();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId));
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(1999L, Currency.euro()));
    }

    @Test
    void shouldGetPublishedOfferByAlias() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId = createOffer(SampleOffer.builder().title("Baby Strampler").build(), agent);
        publishOffer(offerId, 0L, agent);

        var offer = getPublishedOffer("baby-strampler", Agent.anonymous());
        assertThat(offer).isNotNull();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId));
        assertThat(offer.getAlias()).isEqualTo(OfferAlias.of("baby-strampler"));
    }

    @Test
    void shouldReturnAliasInPublishedOffers() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId = createOffer(SampleOffer.builder().title("Baby Strampler").build(), agent);
        publishOffer(offerId, 0L, agent);

        var page = getPublishedOffers("", 0, 100, agent);
        assertThat(page.getResults()).hasSize(1);
        assertThat(page.getResults().getFirst().getAlias()).isEqualTo(OfferAlias.of("baby-strampler"));
    }

    @Test
    void shouldQueryPublishedOffersWithPaging() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId1 = createSampleOffer(agent);
        String offerId2 = createSampleOffer(agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);

        var page = getPublishedOffers("", 0, 1, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults()).hasSize(1);

        page = getPublishedOffers("", 1, 1, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults()).hasSize(1);
    }

    @Test
    void shouldFilterPublishedOffersByCategory() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        markCategoryAsAvailable("CAT_1", "Category 1");
        markCategoryAsAvailable("CAT_2", "Category 2");

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().categoryIds(Set.of("CAT_1")).build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().categoryIds(Set.of("CAT_2")).build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().categoryIds(Set.of("CAT_1", "CAT_2")).build(), agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var page = getPublishedOffers("", Set.of("CAT_1"), null, null, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactlyInAnyOrder(offerId1, offerId3);

        page = getPublishedOffers("", Set.of("CAT_2"), null, null, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactlyInAnyOrder(offerId2, offerId3);

        page = getPublishedOffers("", Set.of("CAT_1", "CAT_2"), null, null, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(3);
    }

    @Test
    void shouldFilterPublishedOffersBySize() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().size("S").build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().size("M").build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().size("L").build(), agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var page = getPublishedOffers("", null, Set.of("S"), null, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults().getFirst().getId().getValue()).isEqualTo(offerId1);

        page = getPublishedOffers("", null, Set.of("S", "L"), null, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactlyInAnyOrder(offerId1, offerId3);
    }

    @Test
    void shouldFilterPublishedOffersByPriceRange() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(1000L).build()).build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(2000L).build()).build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(3000L).build()).build(), agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var priceRange = new PriceRangeDTO();
        priceRange.minPrice = 1500L;
        priceRange.maxPrice = 2500L;
        var page = getPublishedOffers("", null, null, priceRange, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getResults().getFirst().getId().getValue()).isEqualTo(offerId2);

        var minOnly = new PriceRangeDTO();
        minOnly.minPrice = 2000L;
        page = getPublishedOffers("", null, null, minOnly, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);

        var maxOnly = new PriceRangeDTO();
        maxOnly.maxPrice = 2000L;
        page = getPublishedOffers("", null, null, maxOnly, null, 0, 100, agent);
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldSortPublishedOffersByAlphabetical() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().title("Banana").build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().title("Apple").build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().title("Cherry").build(), agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var sort = new OffersSortDTO();
        sort.property = OffersSortPropertyDTO.ALPHABETICAL;
        sort.direction = OffersSortDirectionDTO.ASCENDING;
        var page = getPublishedOffers("", null, null, null, sort, 0, 100, agent);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactly(offerId2, offerId1, offerId3);

        sort.direction = OffersSortDirectionDTO.DESCENDING;
        page = getPublishedOffers("", null, null, null, sort, 0, 100, agent);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactly(offerId3, offerId1, offerId2);
    }

    @Test
    void shouldSortPublishedOffersByPrice() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(3000L).build()).build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(1000L).build()).build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().price(SamplePrice.builder().amount(2000L).build()).build(), agent);
        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var sort = new OffersSortDTO();
        sort.property = OffersSortPropertyDTO.PRICE;
        sort.direction = OffersSortDirectionDTO.ASCENDING;
        var page = getPublishedOffers("", null, null, null, sort, 0, 100, agent);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactly(offerId2, offerId3, offerId1);

        sort.direction = OffersSortDirectionDTO.DESCENDING;
        page = getPublishedOffers("", null, null, null, sort, 0, 100, agent);
        assertThat(page.getResults().stream().map(o -> o.getId().getValue()).toList())
                .containsExactly(offerId1, offerId3, offerId2);
    }

    @Test
    void shouldGetAvailableSizesForOffers() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().size("M").build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().size("S").build(), agent);
        String offerId3 = createOffer(SampleOffer.builder().size("L").build(), agent);
        String offerId4 = createOffer(SampleOffer.builder().size("M").build(), agent);

        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        publishOffer(offerId3, 0L, agent);

        var sizes = getAvailableSizesForOffers(agent);
        assertThat(sizes).containsExactly("L", "M", "S");
    }

    @Test
    void shouldNotIncludeArchivedOfferSizesInAvailableSizes() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        setUpDefaultProduct();
        String offerId1 = createOffer(SampleOffer.builder().size("M").build(), agent);
        String offerId2 = createOffer(SampleOffer.builder().size("XL").build(), agent);

        publishOffer(offerId1, 0L, agent);
        publishOffer(offerId2, 0L, agent);
        reserveOffer(offerId2, 1L, agent);
        archiveOffer(offerId2, 2L, agent);

        var sizes = getAvailableSizesForOffers(agent);
        assertThat(sizes).containsExactly("M");
    }

}
