package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.embroideries.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublishEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldPublishEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user publishes the embroidery
        publishEmbroidery(embroideryId, 0L, agent);

        // then: the embroidery is published
        assertThat(getEmbroidery(embroideryId, agent).isPublished()).isTrue();

        // and: anonymous users can read the published embroidery
        assertThat(getPublishedEmbroidery(embroideryId, Agent.anonymous())).isNotNull();
    }

    @Test
    void shouldUnpublishEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and publishes an embroidery
        String embroideryId = createSampleEmbroidery(agent);
        publishEmbroidery(embroideryId, 0L, agent);

        // when: the user unpublishes the embroidery
        unpublishEmbroidery(embroideryId, 1L, agent);

        // then: the embroidery is unpublished
        assertThat(getEmbroidery(embroideryId, agent).isPublished()).isFalse();

        // and: anonymous users can no longer read the embroidery
        assertThat(getPublishedEmbroidery(embroideryId, Agent.anonymous())).isNull();
    }

    @Test
    void shouldNotPublishEmbroideryTwice() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and publishes an embroidery
        String embroideryId = createSampleEmbroidery(agent);
        publishEmbroidery(embroideryId, 0L, agent);

        // when: the user tries to publish the embroidery again; then: an error is raised
        assertThatThrownBy(() -> publishEmbroidery(embroideryId, 1L, agent))
                .isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldNotUnpublishEmbroideryThatIsNotPublished() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to unpublish the unpublished embroidery; then: an error is raised
        assertThatThrownBy(() -> unpublishEmbroidery(embroideryId, 0L, agent))
                .isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotPublishOrUnpublishEmbroideryWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        String embroideryId = createSampleEmbroidery(otherAgent);
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: a user that is not allowed tries to publish the embroidery; then: an error is raised
        assertThatThrownBy(() -> publishEmbroidery(embroideryId, 0L, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: a user that is not allowed tries to unpublish the embroidery; then: an error is raised
        publishEmbroidery(embroideryId, 0L, otherAgent);
        assertThatThrownBy(() -> unpublishEmbroidery(embroideryId, 1L, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotPublishEmbroideryGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and renames an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");
        renameEmbroidery(embroideryId, 0L, "Fox", agent);

        // when: the user tries to publish the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> publishEmbroidery(embroideryId, 0L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
