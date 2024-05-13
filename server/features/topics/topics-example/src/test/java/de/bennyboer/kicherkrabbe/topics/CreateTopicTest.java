package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateTopicTest extends TopicsModuleTest {

    @Test
    void shouldCreateTopicAsUser() {
        // given: a user is allowed to create topics
        allowUserToCreateTopics("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a topic
        String topicId = createTopic("Winter", agent);

        // then: the topic is created
        var topics = getTopics(agent);
        assertThat(topics).hasSize(1);
        var topic = topics.getFirst();
        assertThat(topic.getId()).isEqualTo(TopicId.of(topicId));
        assertThat(topic.getName()).isEqualTo(TopicName.of("Winter"));
    }

    @Test
    void shouldNotBeAbleToCreateTopicGivenAnInvalidTopic() {
        // given: a user is allowed to create topics
        allowUserToCreateTopics("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a topic with an invalid topic; then: an error is raised
        assertThatThrownBy(() -> createTopic("", agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreateTopicWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create a topic tries to create a topic; then: an error is raised
        assertThatThrownBy(() -> createTopic("Winter", Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleTopics() {
        // given: a user is allowed to create topics
        allowUserToCreateTopics("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple topics
        createTopic("Winter", agent);
        createTopic("Fall", agent);
        createTopic("Spring", agent);

        // then: the topics are created
        var topics = getTopics(agent);
        assertThat(topics).hasSize(3);
    }

}
