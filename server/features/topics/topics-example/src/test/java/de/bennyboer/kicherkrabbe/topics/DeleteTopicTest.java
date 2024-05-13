package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteTopicTest extends TopicsModuleTest {

    @Test
    void shouldDeleteTopic() {
        // given: a topic
        allowUserToCreateTopics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var topicId1 = createTopic("Winter", agent);
        var topicId2 = createTopic("Fall", agent);

        // when: the user deletes the first topic
        deleteTopic(topicId1, 0L, agent);

        // then: the first topic is deleted
        var topics = getTopics(agent);
        assertThat(topics).hasSize(1);
        var topic = topics.getFirst();
        assertThat(topic.getId()).isEqualTo(TopicId.of(topicId2));
    }

    @Test
    void shouldNotDeleteTopicIfNotHavingPermission() {
        // given: a topic
        allowUserToCreateTopics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var topicId = createTopic("Winter", agent);

        // when: another user tries to delete the topic; then: an error is raised
        assertThatThrownBy(() -> deleteTopic(topicId, 0L, Agent.user(AgentId.of("OTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
