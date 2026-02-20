package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.offers.unpublish.CannotUnpublishReservedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnpublishOfferTest extends OffersModuleTest {

    @Test
    void shouldUnpublishOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        unpublishOffer(offerId, 1L, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId));
        assertThat(offer.getVersion()).isEqualTo(Version.of(2));
        assertThat(offer.isPublished()).isFalse();
    }

    @Test
    void shouldNotUnpublishAlreadyUnpublishedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        assertThatThrownBy(() -> unpublishOffer(offerId, 0L, agent))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotUnpublishReservedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);

        assertThatThrownBy(() -> unpublishOffer(offerId, 2L, agent))
                .isInstanceOf(CannotUnpublishReservedError.class);
    }

    @Test
    void shouldNotUnpublishOfferGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        unpublishOffer(offerId, 1L, agent);

        assertThatThrownBy(() -> unpublishOffer(offerId, 1L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUnpublishOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> unpublishOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
