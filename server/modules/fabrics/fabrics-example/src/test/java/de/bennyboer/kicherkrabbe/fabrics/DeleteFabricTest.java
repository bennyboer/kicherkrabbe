package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DeleteFabricTest extends FabricsModuleTest {

    @Test
    void shouldDeleteFabricAsUser() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates some fabrics
        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        String fabricId2 = createFabric(
                "Summer",
                "SUMMER_IMAGE_ID",
                Set.of("RED_ID", "YELLOW_ID"),
                Set.of("SUMMER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: the user deletes the first fabric
        deleteFabric(fabricId1, 0L, agent);

        // then: the first fabric is deleted
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId2));
    }

    @Test
    void shouldNotDeleteFabricGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // and: the fabric is renamed
        renameFabric(fabricId, 0L, "New name", agent);

        // when: the user tries to delete the fabric with an outdated version
        assertThatThrownBy(() -> deleteFabric(
                fabricId,
                0L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldNotDeleteFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to delete a fabric tries to delete a fabric; then: an error is raised
        assertThatThrownBy(() -> deleteFabric(
                "FABRIC_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}
