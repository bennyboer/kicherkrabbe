package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import org.junit.jupiter.api.Test;

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
}
