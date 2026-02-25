package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateTitleTest extends OffersModuleTest {

    @Test
    void shouldUpdateTitle() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        long newVersion = updateOfferTitle(offerId, 0L, "Updated Title", agent);
        assertThat(newVersion).isEqualTo(1L);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getTitle()).isEqualTo(OfferTitle.of("Updated Title"));
    }

    @Test
    void shouldNotUpdateTitleGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        updateOfferTitle(offerId, 0L, "Updated Title", agent);

        assertThatThrownBy(() -> updateOfferTitle(offerId, 0L, "Another Title", agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdateTitleOfArchivedOffer() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        publishOffer(offerId, 0L, agent);
        reserveOffer(offerId, 1L, agent);
        archiveOffer(offerId, 2L, agent);

        assertThatThrownBy(() -> updateOfferTitle(offerId, 3L, "Updated Title", agent));
    }

    @Test
    void shouldNotUpdateTitleWhenNotAllowed() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> updateOfferTitle(offerId, 0L, "Updated Title", otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
