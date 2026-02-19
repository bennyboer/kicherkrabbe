package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.AggregateVersionOutdatedError;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateFabricImagesTest extends FabricsModuleTest {

    @Test
    void shouldUpdateFabricImagesAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics, colors, and fabric types are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: a fabric is created
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: updating the fabric images
        updateFabricImages(fabricId, 0L, "NEW_IMAGE_ID", List.of(), agent);

        // then: the fabric images are updated
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("NEW_IMAGE_ID"));
        assertThat(fabric.getExampleImages()).isEmpty();
    }

    @Test
    void shouldUpdateFabricImagesWithExampleImages() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a fabric type is available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: a fabric is created
        String fabricId = createSampleFabric(agent);

        // when: updating the fabric images with example images
        updateFabricImages(fabricId, 0L, "MAIN_IMAGE_ID", List.of("EXAMPLE_1", "EXAMPLE_2"), agent);

        // then: the fabric images are updated with example images
        var fabric = getFabric(fabricId, agent);
        assertThat(fabric.getImage()).isEqualTo(ImageId.of("MAIN_IMAGE_ID"));
        assertThat(fabric.getExampleImages()).containsExactly(ImageId.of("EXAMPLE_1"), ImageId.of("EXAMPLE_2"));
    }

    @Test
    void shouldReorderExampleImages() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a fabric type is available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: a fabric is created with example images
        String fabricId = createSampleFabric(agent);
        updateFabricImages(fabricId, 0L, "MAIN_IMAGE_ID", List.of("EXAMPLE_1", "EXAMPLE_2", "EXAMPLE_3"), agent);

        // when: reordering the example images
        updateFabricImages(fabricId, 1L, "MAIN_IMAGE_ID", List.of("EXAMPLE_3", "EXAMPLE_1", "EXAMPLE_2"), agent);

        // then: the example images are reordered
        var fabric = getFabric(fabricId, agent);
        assertThat(fabric.getExampleImages()).containsExactly(
                ImageId.of("EXAMPLE_3"),
                ImageId.of("EXAMPLE_1"),
                ImageId.of("EXAMPLE_2")
        );
    }

    @Test
    void shouldClearExampleImages() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: a fabric type is available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: a fabric is created with example images
        String fabricId = createSampleFabric(agent);
        updateFabricImages(fabricId, 0L, "MAIN_IMAGE_ID", List.of("EXAMPLE_1", "EXAMPLE_2"), agent);

        // when: clearing the example images
        updateFabricImages(fabricId, 1L, "MAIN_IMAGE_ID", List.of(), agent);

        // then: the example images are cleared
        var fabric = getFabric(fabricId, agent);
        assertThat(fabric.getExampleImages()).isEmpty();
    }

    @Test
    void shouldNotUpdateFabricImagesWhenUserIsNotAllowed() {
        // when: a user that is not allowed tries to update fabric images; then: an error is raised
        assertThatThrownBy(() -> updateFabricImages(
                "FABRIC_ID",
                0L,
                "NEW_IMAGE_ID",
                List.of(),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateFabricImagesGivenAnOutdatedVersion() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics, colors, and fabric types are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: a fabric is created
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

        // when: updating the fabric images with an outdated version; then: an error is raised
        assertThatThrownBy(() -> updateFabricImages(
                fabricId,
                0L,
                "NEW_IMAGE_ID",
                List.of(),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

}
