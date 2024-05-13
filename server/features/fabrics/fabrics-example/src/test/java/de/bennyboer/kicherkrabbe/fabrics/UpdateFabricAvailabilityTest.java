package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateFabricAvailabilityTest extends FabricsModuleTest {

    @Test
    void shouldUpdateFabricAvailabilityAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        var jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = false;

        var cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = false;
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: the user updates the availability of the fabric
        jerseyAvailability = new FabricTypeAvailabilityDTO();
        jerseyAvailability.typeId = "JERSEY_ID";
        jerseyAvailability.inStock = true;

        cottonAvailability = new FabricTypeAvailabilityDTO();
        cottonAvailability.typeId = "COTTON_ID";
        cottonAvailability.inStock = true;

        var silkAvailability = new FabricTypeAvailabilityDTO();
        silkAvailability.typeId = "SILK_ID";
        silkAvailability.inStock = true;

        updateFabricAvailability(fabricId, 0L, Set.of(jerseyAvailability, cottonAvailability, silkAvailability), agent);

        // then: the fabric has the new availability
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("SILK_ID"), true)
        );
    }

    @Test
    void shouldNotUpdateFabricAvailabilityWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a fabrics availability tries to update the availability; then:
        // an error is raised
        assertThatThrownBy(() -> updateFabricAvailability(
                "FABRIC_ID",
                0L,
                Set.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateFabricAvailabilityGivenAnOutdatedVersion() {
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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: the user tries to update the fabric availability with an outdated version
        assertThatThrownBy(() -> updateFabricAvailability(
                fabricId,
                0L,
                Set.of(),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseIllegalArgumentExceptionWhenTopicIsBlank() {
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

        // when: the user tries to update the fabric availability with an invalid availability
        var invalidAvailability = new FabricTypeAvailabilityDTO();
        invalidAvailability.typeId = "";
        invalidAvailability.inStock = true;

        assertThatThrownBy(() -> updateFabricAvailability(
                fabricId,
                0L,
                Set.of(invalidAvailability),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

}
