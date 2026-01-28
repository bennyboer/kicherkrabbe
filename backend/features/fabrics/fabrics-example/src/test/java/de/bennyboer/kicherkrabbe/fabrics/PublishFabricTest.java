package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.publish.AlreadyPublishedError;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PublishFabricTest extends FabricsModuleTest {

    @Test
    void shouldPublishFabricAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // when: the user publishes the fabric
        publishFabric(fabricId, 0L, agent);

        // then: the fabric is published
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.isPublished()).isTrue();
    }

    @Test
    void shouldNotBeAbleToPublishFabricIfItIsAlreadyPublished() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: the user tries to publish the fabric again; then: an error is raised
        assertThatThrownBy(() -> publishFabric(
                fabricId,
                1L,
                agent
        )).isInstanceOf(AlreadyPublishedError.class);
    }

    @Test
    void shouldNotPublishFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to publish a fabric tries to publish a fabric; then: an error is raised
        assertThatThrownBy(() -> publishFabric(
                "FABRIC_ID",
                0L,
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotPublishFabricGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric
        String fabricId = createSampleFabric(agent);

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: the user tries to publish the fabric with an outdated version
        assertThatThrownBy(() -> publishFabric(
                fabricId,
                0L,
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
