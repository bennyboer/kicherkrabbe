package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CleanupFabricTypesTest extends FabricsModuleTest {

    @Test
    void shouldCleanupDeletedFabricTypeFromFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user created some fabrics referencing some fabric types
        var fabricType1Availability = new FabricTypeAvailabilityDTO();
        fabricType1Availability.typeId = "JERSEY_ID";
        fabricType1Availability.inStock = true;

        var fabricType2Availability = new FabricTypeAvailabilityDTO();
        fabricType2Availability.typeId = "COTTON_ID";
        fabricType2Availability.inStock = false;

        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(fabricType1Availability, fabricType2Availability),
                agent
        );
        String fabricId2 = createFabric(
                "Polar bear party",
                "POLAR_BEAR_IMAGE_ID",
                Set.of("WHITE_ID"),
                Set.of("WINTER_ID"),
                Set.of(fabricType1Availability),
                agent
        );
        String fabricId3 = createFabric(
                "Cat brawl",
                "CAT_IMAGE_ID",
                Set.of("BLACK_ID", "WHITE_ID"),
                Set.of("ANIMALS_ID"),
                Set.of(fabricType2Availability),
                agent
        );

        // when: a fabric type is removed from all fabrics
        removeFabricTypeFromFabrics(fabricType2Availability.typeId);

        // then: the fabric type availability is removed from all fabrics
        var fabric1 = getFabric(fabricId1, agent);
        assertThat(fabric1.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true)
        );

        var fabric2 = getFabric(fabricId2, agent);
        assertThat(fabric2.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true)
        );

        var fabric3 = getFabric(fabricId3, agent);
        assertThat(fabric3.getAvailability()).isEmpty();
    }

}
