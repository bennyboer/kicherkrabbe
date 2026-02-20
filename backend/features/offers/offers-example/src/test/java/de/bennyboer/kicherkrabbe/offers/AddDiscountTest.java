package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AddDiscountTest extends OffersModuleTest {

    @Test
    void shouldAddDiscountAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        addOfferDiscount(offerId, 0L, discount, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getDiscountedPrice()).isPresent();
        assertThat(offer.getPricing().getDiscountedPrice().get()).isEqualTo(Money.of(1499L, Currency.euro()));
    }

    @Test
    void shouldNotAddDiscountHigherThanPrice() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 2999L;
        discount.currency = "EUR";
        assertThatThrownBy(() -> addOfferDiscount(offerId, 0L, discount, agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAddDiscountGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        addOfferDiscount(offerId, 0L, discount, agent);

        var outdatedDiscount = new MoneyDTO();
        outdatedDiscount.amount = 999L;
        outdatedDiscount.currency = "EUR";
        assertThatThrownBy(() -> addOfferDiscount(offerId, 0L, outdatedDiscount, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotAddDiscountWhenUserIsNotAllowed() {
        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        assertThatThrownBy(() -> addOfferDiscount("OFFER_ID", 0L, discount, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
