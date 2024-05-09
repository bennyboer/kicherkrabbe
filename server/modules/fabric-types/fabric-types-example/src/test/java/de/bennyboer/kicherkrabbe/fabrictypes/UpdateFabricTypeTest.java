package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateFabricTypeTest extends FabricTypesModuleTest {

    @Test
    void shouldUpdateFabricType() {
        // given: a fabric type
        allowUserToCreateFabricTypes("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var fabricTypeId = createFabricType("Jersey", agent);

        // when: the user updates the fabric type
        updateFabricType(fabricTypeId, 0L, "French-Terry", agent);

        // then: the fabric type is updated
        var fabricTypes = getFabricTypes(agent);
        assertThat(fabricTypes).hasSize(1);
        var fabricType = fabricTypes.getFirst();
        assertThat(fabricType.getId()).isEqualTo(FabricTypeId.of(fabricTypeId));
        assertThat(fabricType.getName()).isEqualTo(FabricTypeName.of("French-Terry"));
    }

    @Test
    void shouldNotUpdateFabricTypeIfNotHavingPermission() {
        // given: a fabric type
        allowUserToCreateFabricTypes("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        var fabricTypeId = createFabricType("Jersey", agent);

        // when: another user tries to update the fabric type; then: an error is raised
        assertThatThrownBy(() -> updateFabricType(
                fabricTypeId,
                0L,
                "French-Terry",
                Agent.user(AgentId.of("OTHER_USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
