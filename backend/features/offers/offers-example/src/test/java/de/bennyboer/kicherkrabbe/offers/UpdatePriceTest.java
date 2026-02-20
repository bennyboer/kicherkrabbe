package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdatePriceTest extends OffersModuleTest {

    @Test
    void shouldUpdateOfferPriceAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var newPrice = new MoneyDTO();
        newPrice.amount = 2999L;
        newPrice.currency = "EUR";
        updateOfferPrice(offerId, 0L, newPrice, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getVersion()).isEqualTo(Version.of(1));
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(2999L, Currency.euro()));
        assertThat(offer.getPricing().getPriceHistory()).hasSize(1);
        assertThat(offer.getPricing().getPriceHistory().getFirst().getPrice()).isEqualTo(Money.of(1999L, Currency.euro()));
    }

    @Test
    void shouldTrackPriceHistory() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var firstUpdate = new MoneyDTO();
        firstUpdate.amount = 2999L;
        firstUpdate.currency = "EUR";
        updateOfferPrice(offerId, 0L, firstUpdate, agent);

        var secondUpdate = new MoneyDTO();
        secondUpdate.amount = 3999L;
        secondUpdate.currency = "EUR";
        updateOfferPrice(offerId, 1L, secondUpdate, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(3999L, Currency.euro()));
        assertThat(offer.getPricing().getPriceHistory()).hasSize(2);
        assertThat(offer.getPricing().getPriceHistory().get(0).getPrice()).isEqualTo(Money.of(1999L, Currency.euro()));
        assertThat(offer.getPricing().getPriceHistory().get(1).getPrice()).isEqualTo(Money.of(2999L, Currency.euro()));
    }

    @Test
    void shouldClearDiscountOnPriceUpdate() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var discount = new MoneyDTO();
        discount.amount = 1499L;
        discount.currency = "EUR";
        addOfferDiscount(offerId, 0L, discount, agent);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getDiscountedPrice()).isPresent();

        var newPrice = new MoneyDTO();
        newPrice.amount = 2999L;
        newPrice.currency = "EUR";
        updateOfferPrice(offerId, 1L, newPrice, agent);

        offer = getOffer(offerId, agent);
        assertThat(offer.getPricing().getPrice()).isEqualTo(Money.of(2999L, Currency.euro()));
        assertThat(offer.getPricing().getDiscountedPrice()).isEmpty();
    }

    @Test
    void shouldNotUpdatePriceGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var newPrice = new MoneyDTO();
        newPrice.amount = 2999L;
        newPrice.currency = "EUR";
        updateOfferPrice(offerId, 0L, newPrice, agent);

        var outdatedPrice = new MoneyDTO();
        outdatedPrice.amount = 3999L;
        outdatedPrice.currency = "EUR";
        assertThatThrownBy(() -> updateOfferPrice(offerId, 0L, outdatedPrice, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdatePriceWhenUserIsNotAllowed() {
        var newPrice = new MoneyDTO();
        newPrice.amount = 2999L;
        newPrice.currency = "EUR";
        assertThatThrownBy(() -> updateOfferPrice("OFFER_ID", 0L, newPrice, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
