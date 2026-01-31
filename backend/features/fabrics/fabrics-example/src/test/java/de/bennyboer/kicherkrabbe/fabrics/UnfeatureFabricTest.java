package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.unfeature.AlreadyUnfeaturedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UnfeatureFabricTest extends FabricsModuleTest {

    @Test
    void shouldUnfeatureFabricAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        );

        // and: the user features the fabric
        featureFabric(fabricId, 0L, agent);

        // when: the user unfeatures the fabric
        unfeatureFabric(fabricId, 1L, agent);

        // then: the fabric is not featured anymore
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(2L));
        assertThat(fabric.isFeatured()).isFalse();
    }

    @Test
    void shouldNotBeAbleToUnfeatureFabricIfItIsAlreadyUnfeatured() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        );

        // when: the user tries to unfeature the fabric; then: an error is raised
        assertThatThrownBy(() -> unfeatureFabric(
                fabricId,
                0L,
                agent
        )).isInstanceOf(AlreadyUnfeaturedError.class);
    }

    @Test
    void shouldNotUnfeatureFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to unfeature a fabric tries to unfeature a fabric; then: an error is raised
        assertThatThrownBy(() -> unfeatureFabric(
                "FABRIC_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUnfeatureFabricGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        );

        // and: the fabric is featured
        featureFabric(fabricId, 0L, agent);

        // and: the fabric is renamed
        renameFabric(fabricId, 1L, "Test", agent);

        // when: the user tries to unfeature the fabric with an outdated version
        assertThatThrownBy(() -> unfeatureFabric(
                fabricId,
                1L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
