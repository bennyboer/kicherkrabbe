package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateCategoriesTest extends OffersModuleTest {

    @Test
    void shouldUpdateCategories() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String offerId = createSampleOffer(agent);

        long newVersion = updateOfferCategories(offerId, 0L, Set.of("CAT_1", "CAT_2"), agent);
        assertThat(newVersion).isEqualTo(1L);

        var offer = getOffer(offerId, agent);
        assertThat(offer.getCategories()).containsExactlyInAnyOrder(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryId.of("CAT_2")
        );
    }

    @Test
    void shouldNotUpdateCategoriesGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        updateOfferCategories(offerId, 0L, Set.of("CAT_1"), agent);

        assertThatThrownBy(() -> updateOfferCategories(offerId, 0L, Set.of("CAT_2"), agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdateCategoriesWhenNotAllowed() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        assertThatThrownBy(() -> updateOfferCategories(offerId, 0L, Set.of("CAT_1"), otherAgent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
