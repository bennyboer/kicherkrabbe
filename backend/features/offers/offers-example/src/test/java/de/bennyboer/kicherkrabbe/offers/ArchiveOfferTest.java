package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.offers.archive.NotReservedForArchiveError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArchiveOfferTest extends OffersModuleTest {

    @Test
    void shouldArchiveReservedOfferAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);
        archiveOffer(offerId, 2L, agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getArchivedAt()).isPresent();
        assertThat(offer.isPublished()).isFalse();
        assertThat(offer.isReserved()).isFalse();
    }

    @Test
    void shouldNotArchiveNotReservedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);

        assertThatThrownBy(() -> archiveOffer(offerId, 1L, agent))
                .isInstanceOf(NotReservedForArchiveError.class);
    }

    @Test
    void shouldNotArchiveDraftOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        assertThatThrownBy(() -> archiveOffer(offerId, 0L, agent))
                .isInstanceOf(NotReservedForArchiveError.class);
    }

    @Test
    void shouldNotArchiveOfferGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);
        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);
        updateOfferImages(offerId, 2L, List.of("NEW_IMAGE"), agent);

        assertThatThrownBy(() -> archiveOffer(offerId, 2L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotArchiveOfferWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> archiveOffer("OFFER_ID", 0L, Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
