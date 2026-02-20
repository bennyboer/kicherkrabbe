package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RemoveDiscountTest extends OffersModuleTest {

    @Test
    void shouldRemoveDiscountAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        addOfferDiscount(offerId, 0L, discount, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getDiscountedPrice()).isPresent();

        removeOfferDiscount(offerId, 1L, agent);

        offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getDiscountedPrice()).isEmpty();
    }

    @Test
    void shouldNotRemoveDiscountGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        addOfferDiscount(offerId, 0L, discount, agent);
        removeOfferDiscount(offerId, 1L, agent);

        assertThatThrownBy(() -> removeOfferDiscount(offerId, 1L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotRemoveDiscountWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> removeOfferDiscount("OFFER_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
