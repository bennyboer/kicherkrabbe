package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupTopicsTest extends FabricsModuleTest {

    @Test
    void shouldCleanupDeletedTopicFromFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user created some fabrics referencing some topics
        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        String fabricId2 = createFabric(
                "Polar bear party",
                "POLAR_BEAR_IMAGE_ID",
                Set.of("WHITE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        String fabricId3 = createFabric(
                "Cat brawl",
                "CAT_IMAGE_ID",
                Set.of("BLACK_ID", "WHITE_ID"),
                Set.of("ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: a topic is removed from all fabrics
        removeTopicFromFabrics("WINTER_ID");

        // then: the topic is removed from all fabrics
        var fabric1 = getFabric(fabricId1, agent);
        assertThat(fabric1.getTopics()).containsExactlyInAnyOrder(TopicId.of("ANIMALS_ID"));

        var fabric2 = getFabric(fabricId2, agent);
        assertThat(fabric2.getTopics()).isEmpty();

        var fabric3 = getFabric(fabricId3, agent);
        assertThat(fabric3.getTopics()).containsExactlyInAnyOrder(TopicId.of("ANIMALS_ID"));
    }

}
