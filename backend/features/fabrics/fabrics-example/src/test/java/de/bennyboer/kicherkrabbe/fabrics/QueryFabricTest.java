package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateNotFoundError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QueryFabricTest extends FabricsModuleTest {

    @Test
    void shouldQueryFabricAsUser() {
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

        // when: the user queries the fabric
        var fabric = getFabric(fabricId, agent);

        // then: the fabric is returned
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.zero());
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
        assertThat(fabric.isPublished()).isFalse();
        assertThat(fabric.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldNotQueryFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to query a fabric tries to query a fabric; then: an error is raised
        assertThatThrownBy(() -> getFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldRaiseNotFoundErrorWhenFabricDoesNotExist() {
        // given: a user has permissions to read a fabric that does not exist anymore
        allowUserToReadFabric("USER_ID", "FABRIC_ID");

        // when: querying a fabric that does not exist; then: an error is raised
        assertThatThrownBy(() -> getFabric(
                "FABRIC_ID",
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof AggregateNotFoundError);
    }

}
