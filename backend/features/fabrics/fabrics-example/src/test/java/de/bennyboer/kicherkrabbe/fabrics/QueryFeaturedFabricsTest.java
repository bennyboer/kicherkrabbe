package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFeaturedFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryFeaturedFabrics() {
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        String fabricId1 = createFabric(
                "Ice bear party",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(jerseyAvailability),
                agent
        );
        String fabricId2 = createFabric(
                "Dotted",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(jerseyAvailability),
                agent
        );

        var result = getFeaturedFabrics(agent);
        assertThat(result).isEmpty();

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        result = getFeaturedFabrics(agent);
        assertThat(result).isEmpty();

        featureFabric(fabricId1, 1L, agent);

        result = getFeaturedFabrics(agent);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(FabricId.of(fabricId1));
        assertThat(result.get(0).getName()).isEqualTo(FabricName.of("Ice bear party"));

        featureFabric(fabricId2, 1L, agent);

        result = getFeaturedFabrics(agent);
        assertThat(result).hasSize(2);

        unfeatureFabric(fabricId1, 2L, agent);

        result = getFeaturedFabrics(agent);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(FabricId.of(fabricId2));
    }

    @Test
    void shouldAllowAnonymousUserToQueryFeaturedFabrics() {
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        String fabricId = createFabric(
                "Ice bear party",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(jerseyAvailability),
                agent
        );

        publishFabric(fabricId, 0L, agent);
        featureFabric(fabricId, 1L, agent);

        var result = getFeaturedFabrics(Agent.anonymous());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(FabricId.of(fabricId));
    }

    @Test
    void shouldAllowSystemUserToQueryFeaturedFabrics() {
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        String fabricId = createFabric(
                "Ice bear party",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(jerseyAvailability),
                agent
        );

        publishFabric(fabricId, 0L, agent);
        featureFabric(fabricId, 1L, agent);

        var result = getFeaturedFabrics(Agent.system());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(FabricId.of(fabricId));
    }

    @Test
    void shouldNotReturnUnpublishedFeaturedFabrics() {
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        String fabricId = createFabric(
                "Ice bear party",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(jerseyAvailability),
                agent
        );

        publishFabric(fabricId, 0L, agent);
        featureFabric(fabricId, 1L, agent);

        var result = getFeaturedFabrics(agent);
        assertThat(result).hasSize(1);

        unpublishFabric(fabricId, 2L, agent);

        result = getFeaturedFabrics(agent);
        assertThat(result).isEmpty();
    }

}
