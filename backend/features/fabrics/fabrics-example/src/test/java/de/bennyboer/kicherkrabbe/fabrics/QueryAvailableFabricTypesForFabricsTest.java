package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeName;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryAvailableFabricTypesForFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryFabricTypesUsedInFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some fabric types are available
        markFabricTypeAsAvailable("SILK_ID", "Silk");
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("FRENCH_TERRY_ID", "French Terry");

        // when: querying the available fabric types for fabrics with the user agent
        var topics = getAvailableFabricTypesForFabrics(agent);

        // then: the fabric types are returned
        assertThat(topics).containsExactlyInAnyOrder(
                FabricType.of(FabricTypeId.of("SILK_ID"), FabricTypeName.of("Silk")),
                FabricType.of(FabricTypeId.of("JERSEY_ID"), FabricTypeName.of("Jersey")),
                FabricType.of(FabricTypeId.of("FRENCH_TERRY_ID"), FabricTypeName.of("French Terry"))
        );

        // when: querying the fabric types used in fabrics with an anonymous agent; then: an error is raised
        assertThatThrownBy(() -> getAvailableFabricTypesForFabrics(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
