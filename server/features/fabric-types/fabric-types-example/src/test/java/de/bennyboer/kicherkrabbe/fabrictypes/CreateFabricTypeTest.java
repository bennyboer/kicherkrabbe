package de.bennyboer.kicherkrabbe.fabrictypes;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateFabricTypeTest extends FabricTypesModuleTest {

    @Test
    void shouldCreateFabricTypeAsUser() {
        // given: a user is allowed to create fabric types
        allowUserToCreateFabricTypes("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a fabric type
        String fabricTypeId = createFabricType("Jersey", agent);

        // then: the fabric type is created
        var fabricTypes = getFabricTypes(agent);
        assertThat(fabricTypes).hasSize(1);
        var fabricType = fabricTypes.getFirst();
        assertThat(fabricType.getId()).isEqualTo(FabricTypeId.of(fabricTypeId));
        assertThat(fabricType.getName()).isEqualTo(FabricTypeName.of("Jersey"));
    }

    @Test
    void shouldNotBeAbleToCreateFabricTypeGivenAnInvalidFabricType() {
        // given: a user is allowed to create fabric types
        allowUserToCreateFabricTypes("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a fabric type with an invalid fabric type; then: an error is raised
        assertThatThrownBy(() -> createFabricType("", agent))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreateFabricTypeWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create a fabric type tries to create a fabric type; then: an error is
        // raised
        assertThatThrownBy(() -> createFabricType("Jersey", Agent.user(AgentId.of("USER_ID"))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleFabricTypes() {
        // given: a user is allowed to create fabric type
        allowUserToCreateFabricTypes("USER_ID");
        Agent agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple fabric types
        createFabricType("Jersey", agent);
        createFabricType("French-Terry", agent);
        createFabricType("Silk", agent);

        // then: the fabric types are created
        var fabricTypes = getFabricTypes(agent);
        assertThat(fabricTypes).hasSize(3);
    }

}
