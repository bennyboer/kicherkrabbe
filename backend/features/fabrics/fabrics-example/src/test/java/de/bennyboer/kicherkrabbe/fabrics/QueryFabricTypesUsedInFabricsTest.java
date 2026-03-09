package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeName;
import de.bennyboer.kicherkrabbe.fabrics.samples.SampleFabric;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFabricTypesUsedInFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryFabricTypesUsedInFabrics() {
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

        // when: querying the fabric types used in fabrics with the user agent
        var fabricTypes = getFabricTypesUsedInFabrics(agent);

        // then: the fabric types are returned
        assertThat(fabricTypes).containsExactlyInAnyOrder(
                FabricType.of(FabricTypeId.of("JERSEY_ID"), FabricTypeName.of("Jersey")),
                FabricType.of(FabricTypeId.of("COTTON_ID"), FabricTypeName.of("Cotton"))
        );

        // when: querying the fabric types used in fabrics with an anonymous agent
        fabricTypes = getFabricTypesUsedInFabrics(Agent.anonymous());

        // then: the fabric types are returned
        assertThat(fabricTypes).containsExactlyInAnyOrder(
                FabricType.of(FabricTypeId.of("JERSEY_ID"), FabricTypeName.of("Jersey")),
                FabricType.of(FabricTypeId.of("COTTON_ID"), FabricTypeName.of("Cotton"))
        );

        // when: querying the fabric types used in fabrics with a system agent
        fabricTypes = getFabricTypesUsedInFabrics(Agent.system());

        // then: the fabric types are returned
        assertThat(fabricTypes).containsExactlyInAnyOrder(
                FabricType.of(FabricTypeId.of("JERSEY_ID"), FabricTypeName.of("Jersey")),
                FabricType.of(FabricTypeId.of("COTTON_ID"), FabricTypeName.of("Cotton"))
        );
    }

}
