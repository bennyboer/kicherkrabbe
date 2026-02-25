package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateSizeTest extends OffersModuleTest {

    @Test
    void shouldUpdateSize() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        long newVersion = updateOfferSize(offerId, 0L, "XL", agent);
        assertThat(newVersion).isEqualTo(1L);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getSize()).isEqualTo(OfferSize.of("XL"));
    }

    @Test
    void shouldNotUpdateSizeGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        updateOfferSize(offerId, 0L, "XL", agent);

        assertThatThrownBy(() -> updateOfferSize(offerId, 0L, "XXL", agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdateSizeWhenNotAllowed() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> updateOfferSize(offerId, 0L, "XL", otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
