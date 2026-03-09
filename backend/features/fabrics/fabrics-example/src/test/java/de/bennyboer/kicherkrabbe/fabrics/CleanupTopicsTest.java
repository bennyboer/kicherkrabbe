package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupTopicsTest extends FabricsModuleTest {

    @Test
    void shouldCleanupDeletedTopicFromFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("BLACK_ID", "Black", 0, 0, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user created some fabrics referencing some topics
        String fabricId1 = createFabric(
                SampleFabric.builder()
                        .name("Ice bear party")
                        .imageId("ICE_BEAR_IMAGE_ID")
                        .colorId("BLUE_ID").colorId("WHITE_ID")
                        .topicId("WINTER_ID").topicId("ANIMALS_ID")
                        .availability(sampleJerseyAvailability).availability(sampleCottonAvailability)
                        .build(),
                agent
        );
        String fabricId2 = createFabric(
                SampleFabric.builder()
                        .name("Polar bear party")
                        .imageId("POLAR_BEAR_IMAGE_ID")
                        .colorId("WHITE_ID")
                        .topicId("WINTER_ID")
                        .availability(sampleJerseyAvailability).availability(sampleCottonAvailability)
                        .build(),
                agent
        );
        String fabricId3 = createFabric(
                SampleFabric.builder()
                        .name("Cat brawl")
                        .imageId("CAT_IMAGE_ID")
                        .colorId("BLACK_ID").colorId("WHITE_ID")
                        .topicId("ANIMALS_ID")
                        .availability(sampleJerseyAvailability).availability(sampleCottonAvailability)
                        .build(),
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
