package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.reserve.AlreadyReservedError;
import de.bennyboer.kicherkrabbe.offers.reserve.NotPublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReserveOfferTest extends OffersModuleTest {

    @Test
    void shouldReservePublishedOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getVersion()).isEqualTo(Version.of(2));
        assertThat(offer.isPublished()).isTrue();
        assertThat(offer.isReserved()).isTrue();
    }

    @Test
    void shouldNotReserveUnpublishedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        assertThatThrownBy(() -> reserveOffer(offerId, 0L, agent))
                .isInstanceOf(NotPublishedError.class);
    }

    @Test
    void shouldNotReserveAlreadyReservedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);

        assertThatThrownBy(() -> reserveOffer(offerId, 2L, agent))
                .isInstanceOf(AlreadyReservedError.class);
    }

    @Test
    void shouldNotReserveOfferGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        updateOfferImages(offerId, 1L, List.of("NEW_IMAGE"), agent);

        assertThatThrownBy(() -> reserveOffer(offerId, 1L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotReserveOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> reserveOffer("OFFER_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
