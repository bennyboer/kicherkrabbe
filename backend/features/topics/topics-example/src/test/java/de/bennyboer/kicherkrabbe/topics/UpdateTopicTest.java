package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateTopicTest extends TopicsModuleTest {

    @Test
    void shouldUpdateTopic() {
        // given: a topic
        allowUserToCreateTopics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var topicId = createTopic("Winter", agent);

        // when: the user updates the topic
        updateTopic(topicId, 0L, "Fall", agent);

        // then: the topic is updated
        var topics = getTopics(agent);
        assertThat(topics).hasSize(1);
        var topic = topics.getFirst();
        assertThat(topic.getId()).isEqualTo(TopicId.of(topicId));
        assertThat(topic.getName()).isEqualTo(TopicName.of("Fall"));
    }

    @Test
    void shouldNotUpdateTopicIfNotHavingPermission() {
        // given: a topic
        allowUserToCreateTopics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var topicId = createTopic("Winter", agent);

        // when: another user tries to update the topic; then: an error is raised
        assertThatThrownBy(() -> updateTopic(
                topicId,
                0L,
                "Fall",
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
