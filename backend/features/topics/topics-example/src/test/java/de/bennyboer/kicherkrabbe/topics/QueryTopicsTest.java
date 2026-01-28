package de.bennyboer.kicherkrabbe.topics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryTopicsTest extends TopicsModuleTest {

    @Test
    void shouldGetAllAccessibleTopics() {
        // given: some topics for different users
        allowUserToCreateTopics("USER_ID_1");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        allowUserToCreateTopics("USER_ID_2");
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        var topicId1 = createTopic("Winter", agent1);
        var topicId2 = createTopic("Fall", agent2);
        var topicId3 = createTopic("Spring", agent1);

        // when: getting all topics for the first user
        var topics1 = getTopics(agent1);

        // then: the topics for the first user are returned
        assertThat(topics1).hasSize(2);
        var topicIds1 = topics1.stream()
                .map(TopicDetails::getId)
                .map(TopicId::getValue)
                .toList();
        assertThat(topicIds1).containsExactlyInAnyOrder(topicId1, topicId3);

        // when: getting all topics for the second user
        var topics2 = getTopics(agent2);

        // then: the topics for the second user are returned
        assertThat(topics2).hasSize(1);
        var topicIds2 = topics2.stream()
                .map(TopicDetails::getId)
                .map(TopicId::getValue)
                .toList();
        assertThat(topicIds2).containsExactlyInAnyOrder(topicId2);
    }

}
