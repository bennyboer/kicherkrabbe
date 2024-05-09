package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFabricTypesTest extends FabricTypesModuleTest {

    @Test
    void shouldGetAllAccessibleFabricTypes() {
        // given: some fabric types for different users
        allowUserToCreateFabricTypes("USER_ID_1");
        var agent1 = Agent.user(AgentId.of("USER_ID_1"));
        allowUserToCreateFabricTypes("USER_ID_2");
        var agent2 = Agent.user(AgentId.of("USER_ID_2"));

        var fabricTypeId1 = createFabricType("Jersey", agent1);
        var fabricTypeId2 = createFabricType("French-Terry", agent2);
        var fabricTypeId3 = createFabricType("Silk", agent1);

        // when: getting all fabric types for the first user
        var fabricTypes1 = getFabricTypes(agent1);

        // then: the fabric types for the first user are returned
        assertThat(fabricTypes1).hasSize(2);
        var fabricTypeIds1 = fabricTypes1.stream()
                .map(FabricTypeDetails::getId)
                .map(FabricTypeId::getValue)
                .toList();
        assertThat(fabricTypeIds1).containsExactlyInAnyOrder(fabricTypeId1, fabricTypeId3);

        // when: getting all fabric types for the second user
        var fabricTypes2 = getFabricTypes(agent2);

        // then: the fabric types for the second user are returned
        assertThat(fabricTypes2).hasSize(1);
        var fabricTypeIds2 = fabricTypes2.stream()
                .map(FabricTypeDetails::getId)
                .map(FabricTypeId::getValue)
                .toList();
        assertThat(fabricTypeIds2).containsExactlyInAnyOrder(fabricTypeId2);
    }

}
