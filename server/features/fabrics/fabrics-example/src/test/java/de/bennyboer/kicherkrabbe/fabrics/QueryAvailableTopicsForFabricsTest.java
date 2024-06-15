package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicName;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryAvailableTopicsForFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryTopicsUsedInFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");
        markTopicAsAvailable("BIRDS_ID", "Birds");

        // when: querying the available topics for fabrics with the user agent
        var topics = getAvailableTopicsForFabrics(agent);

        // then: the topics are returned
        assertThat(topics).containsExactlyInAnyOrder(
                Topic.of(TopicId.of("WINTER_ID"), TopicName.of("Winter")),
                Topic.of(TopicId.of("ANIMALS_ID"), TopicName.of("Animals")),
                Topic.of(TopicId.of("BIRDS_ID"), TopicName.of("Birds"))
        );

        // when: querying the topics used in fabrics with an anonymous agent; then: an error is raised
        assertThatThrownBy(() -> getAvailableTopicsForFabrics(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
