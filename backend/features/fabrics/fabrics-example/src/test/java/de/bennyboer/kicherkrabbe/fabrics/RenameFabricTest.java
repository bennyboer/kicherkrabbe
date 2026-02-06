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
    void shouldNotRenameFabricWhenAliasIsAlreadyInUse() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: two fabrics are created
        String fabricId1 = createSampleFabric(agent, "Ice bear party");
        String fabricId2 = createSampleFabric(agent, "Summer flowers");

        // when: the user tries to rename the second fabric to the same name as the first; then: an error is raised
        assertThatThrownBy(() -> renameFabric(
                fabricId2,
                0L,
                "Ice bear party",
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError
                && ((AliasAlreadyInUseError) e.getCause()).getConflictingFabricId().equals(FabricId.of(fabricId1))
                && ((AliasAlreadyInUseError) e.getCause()).getAlias().equals(FabricAlias.of("ice-bear-party")));

        // when: the user tries to rename to a different name that slugifies to the same alias; then: an error is raised
        assertThatThrownBy(() -> renameFabric(
                fabricId2,
                0L,
                "Ice-Bear-Party",
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError);

        // when: the user renames the fabric to a unique name; then: no error is raised
        renameFabric(fabricId2, 0L, "Winter wonderland", agent);

        // then: the fabric is renamed
        var fabrics = getFabrics(agent);
        var fabric2 = fabrics.stream().filter(f -> f.getId().equals(FabricId.of(fabricId2))).findFirst().orElseThrow();
        assertThat(fabric2.getName()).isEqualTo(FabricName.of("Winter wonderland"));
    }

    @Test
    void shouldAllowRenamingFabricToSameName() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a fabric is created
        String fabricId = createSampleFabric(agent, "Ice bear party");

        // when: the user renames the fabric to the same name (e.g., just fixing casing)
        renameFabric(fabricId, 0L, "Ice Bear Party", agent);

        // then: no error is raised (since the alias is for the same fabric)
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        assertThat(fabrics.getFirst().getName()).isEqualTo(FabricName.of("Ice Bear Party"));
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
