package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublishOfferTest extends OffersModuleTest {

    @Test
    void shouldPublishOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getId()).isEqualTo(OfferId.of(offerId));
        assertThat(offer.getVersion()).isEqualTo(Version.of(1));
        assertThat(offer.isPublished()).isTrue();
    }

    @Test
    void shouldNotPublishAlreadyPublishedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        assertThatThrownBy(() -> publishOffer(offerId, 1L, agent))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldNotPublishOfferGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        assertThatThrownBy(() -> publishOffer(offerId, 0L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotPublishArchivedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);
        archiveOffer(offerId, 2L, agent);

        assertThatThrownBy(() -> publishOffer(offerId, 3L, agent));
    }

    @Test
    void shouldNotPublishOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> publishOffer(
                "OFFER_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
