package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RenameFabricTest extends FabricsModuleTest {

    @Test
    void shouldRenameFabricAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // when: the user renames the fabric
        renameFabric(fabricId, 0L, "New name", agent);

        // then: the fabric is renamed
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getName()).isEqualTo(FabricName.of("New name"));
    }

    @Test
    void shouldNotBeAbleToRenameFabricGivenAnInvalidName() {
        String invalidName1 = "";
        String invalidName2 = null;

        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // when: the user tries to rename the fabric with an invalid name; then: an error is raised
        assertThatThrownBy(() -> renameFabric(
                fabricId,
                0L,
                invalidName1,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to rename the fabric with an invalid name; then: an error is raised
        assertThatThrownBy(() -> renameFabric(
                fabricId,
                0L,
                invalidName2,
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotRenameFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to rename a fabric tries to rename a fabric; then: an error is raised
        assertThatThrownBy(() -> renameFabric(
                "FABRIC_ID",
                0L,
                "Test",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotRenameFabricGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // and: the fabric is renamed
        renameFabric(fabricId, 0L, "New name", agent);

        // when: the user tries to rename the fabric with an outdated version
        assertThatThrownBy(() -> renameFabric(
                fabricId,
                0L,
                "New name",
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
