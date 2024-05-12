package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedFabricTest extends FabricsModuleTest {

    @Test
    void shouldQueryPublishedFabricAsUser() {
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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: the user queries the fabric as a user
        var fabric = getPublishedFabric(fabricId, agent);

        // then: the published fabric is returned
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
    }

    @Test
    void shouldQueryPublishedFabricAsAnonymousUser() {
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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: querying the fabric as an anonymous user
        var fabric = getPublishedFabric(fabricId, Agent.anonymous());

        // then: the published fabric is returned
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
    }

    @Test
    void shouldQueryPublishedFabricAsSystemUser() {
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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: querying the fabric as a system user
        var fabric = getPublishedFabric(fabricId, Agent.system());

        // then: the published fabric is returned
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
    }

    @Test
    void shouldReturnEmptyWhenPublishedFabricDoesNotExist() {
        // when: querying a fabric that does not exist
        var fabric = getPublishedFabric("UNKNOWN_FABRIC_ID", Agent.anonymous());

        // then: the published fabric is null
        assertThat(fabric).isNull();
    }

    @Test
    void shouldReturnEmptyWhenFabricIsNotPublished() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: the user creates a fabric but does not publish it
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: querying the fabric as an anonymous user
        var fabric = getPublishedFabric(fabricId, Agent.anonymous());

        // then: the published fabric is null
        assertThat(fabric).isNull();
    }

    @Test
    void shouldReturnEmptyIfTheFabricIsUnpublished() {
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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // and: the fabric is unpublished again
        unpublishFabric(fabricId, 1L, agent);

        // when: querying the fabric as an anonymous user
        var fabric = getPublishedFabric(fabricId, Agent.anonymous());

        // then: the published fabric is null
        assertThat(fabric).isNull();
    }

}
