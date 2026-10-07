package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateEmbroideryImageTest extends EmbroideriesModuleTest {

    @Test
    void shouldUpdateEmbroideryImage() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user updates the image of the embroidery
        updateEmbroideryImage(embroideryId, 0L, "NEW_IMAGE_ID", agent);

        // then: the image is updated
        var embroidery = getEmbroidery(embroideryId, agent);
        assertThat(embroidery.getVersion()).isEqualTo(Version.of(1));
        assertThat(embroidery.getImage()).isEqualTo(ImageId.of("NEW_IMAGE_ID"));
    }

    @Test
    void shouldNotUpdateEmbroideryImageGivenAnInvalidImage() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to update the image to a blank image; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryImage(embroideryId, 0L, "", agent))
                .isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to update the image to no image; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryImage(embroideryId, 0L, null, agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotUpdateEmbroideryImageWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        String embroideryId = createSampleEmbroidery(Agent.user(AgentId.of("OTHER_USER_ID")));

        // when: a user that is not allowed tries to update the image; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryImage(
                embroideryId,
                0L,
                "NEW_IMAGE_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateEmbroideryImageGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery and updates its image
        String embroideryId = createSampleEmbroidery(agent);
        updateEmbroideryImage(embroideryId, 0L, "NEW_IMAGE_ID", agent);

        // when: the user tries to update the image with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateEmbroideryImage(embroideryId, 0L, "OTHER_IMAGE_ID", agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
