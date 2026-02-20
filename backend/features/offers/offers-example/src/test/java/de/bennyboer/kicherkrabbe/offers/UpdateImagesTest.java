package de.bennyboer.kicherkrabbe.offers;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateImagesTest extends OffersModuleTest {

    @Test
    void shouldUpdateOfferImagesAsUser() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);

        updateOfferImages(offerId, 0L, List.of("NEW_IMAGE_1", "NEW_IMAGE_2"), agent);

        var offers = getOffers(agent);
        assertThat(offers).hasSize(1);
        var offer = offers.getFirst();
        assertThat(offer.getVersion()).isEqualTo(Version.of(1));
        assertThat(offer.getImages()).containsExactly(ImageId.of("NEW_IMAGE_1"), ImageId.of("NEW_IMAGE_2"));
    }

    @Test
    void shouldNotUpdateImagesGivenAnOutdatedVersion() {
        allowUserToCreateOffers("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        String offerId = createSampleOffer(agent);
        updateOfferImages(offerId, 0L, List.of("NEW_IMAGE"), agent);

        assertThatThrownBy(() -> updateOfferImages(offerId, 0L, List.of("OTHER"), agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotUpdateImagesWhenUserIsNotAllowed() {
        assertThatThrownBy(() -> updateOfferImages("OFFER_ID", 0L, List.of("IMAGE"), Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
