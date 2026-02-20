package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.delete.CannotDeleteNonDraftError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteOfferTest extends OffersModuleTest {

    @Test
    void shouldDeleteOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId1 = createSampleOffer(agent);
        String offerId2 = createSampleOffer(agent);

        deleteOffer(offerId1, 0L, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId2));
    }

    @Test
    void shouldNotDeletePublishedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        assertThatThrownBy(() -> deleteOffer(offerId, 1L, agent))
                .isInstanceOf(CannotDeleteNonDraftError.class);
    }

    @Test
    void shouldNotDeleteReservedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);

        assertThatThrownBy(() -> deleteOffer(offerId, 2L, agent))
                .isInstanceOf(CannotDeleteNonDraftError.class);
    }

    @Test
    void shouldNotDeleteOfferGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        updateOfferImages(offerId, 0L, List.of("NEW_IMAGE"), agent);

        assertThatThrownBy(() -> deleteOffer(offerId, 0L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotDeleteOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> deleteOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
