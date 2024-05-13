package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateFabricImageTest extends FabricsModuleTest {

    @Test
    void shouldUpdateFabricImageAsUser() {
        // given: a user is allowed to create fabrics
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

        // when: the user updates the image of the fabric
        updateFabricImage(fabricId, 0L, "NEW_IMAGE_ID", agent);

        // then: the fabric has the new image
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("NEW_IMAGE_ID"));
    }

    @Test
    void shouldNotUpdateFabricImageWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a fabric image tries to update a fabric image; then: an error
        // is raised
        assertThatThrownBy(() -> updateFabricImage(
                "FABRIC_ID",
                0L,
                "NEW_IMAGE_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateFabricImageGivenAnOutdatedVersion() {
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

        // when: the user tries to update the fabric image with an outdated version
        assertThatThrownBy(() -> updateFabricImage(
                fabricId,
                0L,
                "NEW_IMAGE_ID",
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
