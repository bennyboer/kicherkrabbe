package de.bennyboer.kicherkrabbe.embroideries;

import de.bennyboer.kicherkrabbe.embroideries.feature.AlreadyFeaturedError;
import de.bennyboer.kicherkrabbe.embroideries.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FeatureEmbroideryTest extends EmbroideriesModuleTest {

    @Test
    void shouldFeatureEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user features the embroidery
        featureEmbroidery(embroideryId, 0L, agent);

        // then: the embroidery is featured
        assertThat(getEmbroidery(embroideryId, agent).isFeatured()).isTrue();
    }

    @Test
    void shouldUnfeatureEmbroidery() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and features an embroidery
        String embroideryId = createSampleEmbroidery(agent);
        featureEmbroidery(embroideryId, 0L, agent);

        // when: the user unfeatures the embroidery
        unfeatureEmbroidery(embroideryId, 1L, agent);

        // then: the embroidery is no longer featured
        assertThat(getEmbroidery(embroideryId, agent).isFeatured()).isFalse();
    }

    @Test
    void shouldNotFeatureEmbroideryTwice() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and features an embroidery
        String embroideryId = createSampleEmbroidery(agent);
        featureEmbroidery(embroideryId, 0L, agent);

        // when: the user tries to feature the embroidery again; then: an error is raised
        assertThatThrownBy(() -> featureEmbroidery(embroideryId, 1L, agent))
                .isInstanceOf(AlreadyFeaturedError.class);
    }

    @Test
    void shouldNotUnfeatureEmbroideryThatIsNotFeatured() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates an embroidery
        String embroideryId = createSampleEmbroidery(agent);

        // when: the user tries to unfeature the embroidery; then: an error is raised
        assertThatThrownBy(() -> unfeatureEmbroidery(embroideryId, 0L, agent))
                .isInstanceOf(AlreadyUnfeaturedError.class);
    }

    @Test
    void shouldNotFeatureOrUnfeatureEmbroideryWhenUserIsNotAllowed() {
        // given: an embroidery created by another user
        allowUserToCreateEmbroideries("OTHER_USER_ID");
        var otherAgent = Agent.user(AgentId.of("OTHER_USER_ID"));
        String embroideryId = createSampleEmbroidery(otherAgent);
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: a user that is not allowed tries to feature the embroidery; then: an error is raised
        assertThatThrownBy(() -> featureEmbroidery(embroideryId, 0L, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: a user that is not allowed tries to unfeature the embroidery; then: an error is raised
        featureEmbroidery(embroideryId, 0L, otherAgent);
        assertThatThrownBy(() -> unfeatureEmbroidery(embroideryId, 1L, agent))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotFeatureEmbroideryGivenAnOutdatedVersion() {
        // given: a user is allowed to create embroideries
        allowUserToCreateEmbroideries("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates and renames an embroidery
        String embroideryId = createSampleEmbroidery(agent, "Crab");
        renameEmbroidery(embroideryId, 0L, "Fox", agent);

        // when: the user tries to feature the embroidery with an outdated version; then: an error is raised
        assertThatThrownBy(() -> featureEmbroidery(embroideryId, 0L, agent))
                .matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
