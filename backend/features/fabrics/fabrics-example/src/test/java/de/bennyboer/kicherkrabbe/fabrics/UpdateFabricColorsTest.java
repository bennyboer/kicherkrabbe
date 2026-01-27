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

public class UpdateFabricColorsTest extends FabricsModuleTest {

    @Test
    void shouldUpdateFabricColorsAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("NEW_COLOR_ID_1", "New color 1", 255, 0, 0);
        markColorAsAvailable("NEW_COLOR_ID_2", "New color 2", 0, 255, 0);
        markColorAsAvailable("NEW_COLOR_ID_3", "New color 3", 0, 0, 255);

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

        // when: the user updates the colors of the fabric
        updateFabricColors(fabricId, 0L, Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"), agent);

        // then: the fabric has the new colors
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(
                ColorId.of("NEW_COLOR_ID_1"),
                ColorId.of("NEW_COLOR_ID_2"),
                ColorId.of("NEW_COLOR_ID_3")
        );
    }

    @Test
    void shouldRaiseErrorWhenColorsAreMissing() {
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

        // when: the user tries to update the fabric colors with missing colors; then: an error is raised
        assertThatThrownBy(() -> updateFabricColors(
                fabricId,
                0L,
                Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"),
                agent
        )).matches(e -> e.getCause() instanceof ColorsMissingError
                && ((ColorsMissingError) e.getCause()).getMissingColors().equals(Set.of(
                ColorId.of("NEW_COLOR_ID_1"),
                ColorId.of("NEW_COLOR_ID_2"),
                ColorId.of("NEW_COLOR_ID_3")
        )));

        // when: the colors are marked as available
        markColorAsAvailable("NEW_COLOR_ID_1", "New color 1", 255, 0, 0);
        markColorAsAvailable("NEW_COLOR_ID_2", "New color 2", 0, 255, 0);
        markColorAsAvailable("NEW_COLOR_ID_3", "New color 3", 0, 0, 255);

        // when: the user updates the colors of the fabric
        updateFabricColors(fabricId, 0L, Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"), agent);

        // then: the fabric has the new colors
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);
        var fabric = fabrics.getFirst();
        assertThat(fabric.getId()).isEqualTo(FabricId.of(fabricId));
        assertThat(fabric.getVersion()).isEqualTo(Version.of(1));
        assertThat(fabric.getColors()).containsExactlyInAnyOrder(
                ColorId.of("NEW_COLOR_ID_1"),
                ColorId.of("NEW_COLOR_ID_2"),
                ColorId.of("NEW_COLOR_ID_3")
        );

        // when: the color is marked as unavailable again
        markColorAsUnavailable("NEW_COLOR_ID_1");

        // and: the user tries to update the colors of the fabric with the missing color again; then: an error is raised
        assertThatThrownBy(() -> updateFabricColors(
                fabricId,
                1L,
                Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"),
                agent
        )).matches(e -> e.getCause() instanceof ColorsMissingError
                && ((ColorsMissingError) e.getCause()).getMissingColors().equals(Set.of(
                ColorId.of("NEW_COLOR_ID_1")
        )));
    }

    @Test
    void shouldNotUpdateFabricColorsWhenUserIsNotAllowed() {
        // when: a user that is not allowed to update a fabrics colors tries to update the colors; then: an error is
        // raised
        assertThatThrownBy(() -> updateFabricColors(
                "FABRIC_ID",
                0L,
                Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"),
                Agent.user(AgentId.of("USER_ID"))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotUpdateFabricColorsGivenAnOutdatedVersion() {
        // given: a user that is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("NEW_COLOR_ID_1", "New color 1", 255, 0, 0);
        markColorAsAvailable("NEW_COLOR_ID_2", "New color 2", 0, 255, 0);
        markColorAsAvailable("NEW_COLOR_ID_3", "New color 3", 0, 0, 255);

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

        // and: the fabric is published
        publishFabric(fabricId, 0L, agent);

        // when: the user tries to update the fabric colors with an outdated version
        assertThatThrownBy(() -> updateFabricColors(
                fabricId,
                0L,
                Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", "NEW_COLOR_ID_3"),
                agent
        )).matches(e -> e.getCause() instanceof AggregateVersionOutdatedError);
    }

    @Test
    void shouldRaiseIllegalArgumentExceptionWhenColorIsBlank() {
        // given: a user that is allowed to create fabrics
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

        // when: the user tries to update the fabric colors with an empty color
        assertThatThrownBy(() -> updateFabricColors(
                fabricId,
                0L,
                Set.of("NEW_COLOR_ID_1", "NEW_COLOR_ID_2", ""),
                agent
        )).isInstanceOf(IllegalArgumentException.class);
    }

}
