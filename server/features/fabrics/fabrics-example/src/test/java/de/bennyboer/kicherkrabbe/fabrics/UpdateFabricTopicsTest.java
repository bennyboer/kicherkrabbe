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

public class UpdateFabricTopicsTest extends FabricsModuleTest {

    @Test
    void shouldUpdateFabricTopicsAsUser() {
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

        // when: the user updates the topics of the fabric
        updateFabricTopics(fabricId, 0L, Set.of("NEW_TOPIC_ID_1", "NEW_TOPIC_ID_2", "NEW_TOPIC_ID_3"), agent);

        // then: the fabric has the new topics
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getTopics()).containsExactlyInAnyOrder(
                TopicId.of("NEW_TOPIC_ID_1"),
                TopicId.of("NEW_TOPIC_ID_2"),
                TopicId.of("NEW_TOPIC_ID_3")
        );
    }

    @Test
    void shouldNotUpdateFabricTopicsWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a fabrics topics tries to update the topics; then: an error is
        // raised
        assertThatThrownBy(() -> updateFabricTopics(
                "FABRIC_ID",
                0L,
                Set.of("NEW_TOPIC_ID_1", "NEW_TOPIC_ID_2", "NEW_TOPIC_ID_3"),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateFabricTopicsGivenAnOutdatedVersion() {
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

        // when: the user tries to update the fabric topics with an outdated version
        assertThatThrownBy(() -> updateFabricTopics(
                fabricId,
                0L,
                Set.of("NEW_TOPIC_ID_1", "NEW_TOPIC_ID_2", "NEW_TOPIC_ID_3"),
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

        // when: the user tries to update the fabric topics with an empty topic
        assertThatThrownBy(() -> updateFabricTopics(
                fabricId,
                0L,
                Set.of("NEW_TOPIC_ID_1", "NEW_TOPIC_ID_2", ""),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

}
