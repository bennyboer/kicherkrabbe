package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteFabricTypeTest extends FabricTypesModuleTest {

    @Test
    void shouldDeleteFabricType() {
        // given: a fabric type
        allowUserToCreateFabricTypes("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var fabricTypeId1 = createFabricType("Jersey", agent);
        var fabricTypeId2 = createFabricType("French-Terry", agent);

        // when: the user deletes the first fabric type
        deleteFabricType(fabricTypeId1, 0L, agent);

        // then: the first fabric type is deleted
        var fabricTypes = getFabricTypes(agent);
        assertThat(fabricTypes).hasSize(1);
        var fabricType = fabricTypes.getFirst();
        assertThat(fabricType.getId()).isEqualTo(FabricTypeId.of(fabricTypeId2));
    }

    @Test
    void shouldNotDeleteFabricTypeIfNotHavingPermission() {
        // given: a fabric type
        allowUserToCreateFabricTypes("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var fabricTypeId = createFabricType("Jersey", agent);

        // when: another user tries to delete the fabric type; then: an error is raised
        assertThatThrownBy(() -> deleteFabricType(fabricTypeId, 0L, Agent.user(AgentId.of("OTHER_USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
