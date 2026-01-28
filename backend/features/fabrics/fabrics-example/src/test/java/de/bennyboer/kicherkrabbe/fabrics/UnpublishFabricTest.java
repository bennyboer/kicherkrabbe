package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.unpublish.AlreadyUnpublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnpublishFabricTest extends FabricsModuleTest {

    @Test
    void shouldUnpublishFabricAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // and: the user publishes the fabric
        publishFabric(fabricId, 0L, agent);

        // when: the user unpublishes the fabric
        unpublishFabric(fabricId, 1L, agent);

        // then: the fabric is not published anymore
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(2L));
        assertThat(fabric.isPublished()).isFalse();
    }

    @Test
    void shouldNotBeAbleToUnpublishFabricIfItIsAlreadyUnpublished() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: the user tries to unpublish the fabric; then: an error is raised
        assertThatThrownBy(() -> unpublishFabric(
                fabricId,
                0L,
                agent
        )).isInstanceOf(AlreadyUnpublishedError.class);
    }

    @Test
    void shouldNotUnpublishFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to unpublish a fabric tries to unpublish a fabric; then: an error is raised
        assertThatThrownBy(() -> unpublishFabric(
                "FABRIC_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUnpublishFabricGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

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

        // and: the fabric is renamed
        renameFabric(fabricId, 1L, "Test", agent);

        // when: the user tries to unpublish the fabric with an outdated version
        assertThatThrownBy(() -> unpublishFabric(
                fabricId,
                1L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
