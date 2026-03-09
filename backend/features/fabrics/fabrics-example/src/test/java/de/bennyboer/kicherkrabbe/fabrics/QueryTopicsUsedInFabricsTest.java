package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicName;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryTopicsUsedInFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryTopicsUsedInFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");
        markTopicAsAvailable("BIRDS_ID", "Birds");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("BLACK_ID", "Black", 0, 0, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates some fabrics
        createFabric(
                SampleFabric.builder()
                        .name("Ice bear party")
                        .imageId("ICE_BEAR_IMAGE_ID")
                        .colorId("BLUE_ID").colorId("WHITE_ID")
                        .topicId("WINTER_ID").topicId("ANIMALS_ID")
                        .availability(sampleJerseyAvailability).availability(sampleCottonAvailability)
                        .build(),
                agent
        );
        createFabric(
                SampleFabric.builder()
                        .name("Penguin party")
                        .imageId("PENGUIN_IMAGE_ID")
                        .colorId("BLACK_ID").colorId("WHITE_ID")
                        .topicId("WINTER_ID").topicId("ANIMALS_ID").topicId("BIRDS_ID")
                        .availability(sampleJerseyAvailability).availability(sampleCottonAvailability)
                        .build(),
                agent
        );

        // when: querying the topics used in fabrics with the user agent
        var topics = getTopicsUsedInFabrics(agent);

        // then: the topics are returned
        assertThat(topics).containsExactlyInAnyOrder(
                Topic.of(TopicId.of("WINTER_ID"), TopicName.of("Winter")),
                Topic.of(TopicId.of("ANIMALS_ID"), TopicName.of("Animals")),
                Topic.of(TopicId.of("BIRDS_ID"), TopicName.of("Birds"))
        );

        // when: querying the topics used in fabrics with an anonymous agent
        topics = getTopicsUsedInFabrics(Agent.anonymous());

        // then: the topics are returned
        assertThat(topics).containsExactlyInAnyOrder(
                Topic.of(TopicId.of("WINTER_ID"), TopicName.of("Winter")),
                Topic.of(TopicId.of("ANIMALS_ID"), TopicName.of("Animals")),
                Topic.of(TopicId.of("BIRDS_ID"), TopicName.of("Birds"))
        );

        // when: querying the topics used in fabrics with a system agent
        topics = getTopicsUsedInFabrics(Agent.system());

        // then: the topics are returned
        assertThat(topics).containsExactlyInAnyOrder(
                Topic.of(TopicId.of("WINTER_ID"), TopicName.of("Winter")),
                Topic.of(TopicId.of("ANIMALS_ID"), TopicName.of("Animals")),
                Topic.of(TopicId.of("BIRDS_ID"), TopicName.of("Birds"))
        );
    }

}
