package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.requests.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CreateFabricTest extends FabricsModuleTest {

    @Test
    void shouldCreateFabricAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates a fabric
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // then: the fabric is created
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
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
    }

    @Test
    void shouldNotBeAbleToCreateFabricGivenAnInvalidFabric() {
        String validName = "Ice bear party";
        String invalidName1 = "";
        String invalidName2 = null;

        String validImageId = "ICE_BEAR_IMAGE_ID";
        String invalidImageId1 = "";
        String invalidImageId2 = null;

        Set<String> validColorIds = Set.of("BLUE_ID", "WHITE_ID");
        Set<String> invalidColorIds = null;

        Set<String> validTopicIds = Set.of("WINTER_ID", "ANIMALS_ID");
        Set<String> invalidTopicIds = null;

        Set<FabricTypeAvailabilityDTO> validAvailability = Set.of(jerseyAvailability, cottonAvailability);
        Set<FabricTypeAvailabilityDTO> invalidAvailability = null;

        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user tries to create a fabric without name; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                invalidName1,
                validImageId,
                validColorIds,
                validTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric with null name; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                invalidName2,
                validImageId,
                validColorIds,
                validTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric without image ID; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                validName,
                invalidImageId1,
                validColorIds,
                validTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric with null image ID; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                validName,
                invalidImageId2,
                validColorIds,
                validTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric without color IDs; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                validName,
                validImageId,
                invalidColorIds,
                validTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric without topic IDs; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                validName,
                validImageId,
                validColorIds,
                invalidTopicIds,
                validAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);

        // when: the user tries to create a fabric without availability; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                validName,
                validImageId,
                validColorIds,
                validTopicIds,
                invalidAvailability,
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotCreateFabricWhenUserIsNotAllowed() {
        // when: a user that is not allowed to create a fabric tries to create a fabric; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Test",
                "IMAGE_ID",
                Set.of(),
                Set.of(),
                Set.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldCreateMultipleFabrics() {
        // given: a user is allowed to create topics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // when: the user creates multiple fabrics
        createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        createFabric(
                "Summer",
                "SUMMER_IMAGE_ID",
                Set.of("RED_ID", "YELLOW_ID"),
                Set.of("SUMMER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        createFabric(
                "Winter",
                "WINTER_IMAGE_ID",
                Set.of("WHITE_ID", "BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // then: the fabrics are created
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(3);

        // and: they have different IDs
        var fabricIds = fabrics.stream().map(FabricDetails::getId).toList();
        assertThat(fabricIds).doesNotHaveDuplicates();

        // and: they all have version zero
        var versions = fabrics.stream().map(FabricDetails::getVersion).toList();
        assertThat(versions).allMatch(v -> v.equals(Version.zero()));
    }

}
