package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
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

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

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
    void shouldNotCreateFabricWhenTopicsAreMissing() {
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

        // when: the user creates a fabric with a missing topic; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID", "SUMMER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof TopicsMissingError
                && ((TopicsMissingError) e.getCause()).getMissingTopics().equals(Set.of(TopicId.of("SUMMER_ID"))));

        // when: the topic is marked as available
        markTopicAsAvailable("SUMMER_ID", "Summer");

        // and: the user creates a fabric with all topics
        createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID", "SUMMER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // then: the fabric is created
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);

        // when: the topic is marked as unavailable again
        markTopicAsUnavailable("SUMMER_ID");

        // and: the user tries to create a fabric with all topics; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID", "SUMMER_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof TopicsMissingError
                && ((TopicsMissingError) e.getCause()).getMissingTopics().equals(Set.of(TopicId.of("SUMMER_ID"))));
    }

    @Test
    void shouldNotCreateFabricWhenColorsAreMissing() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);

        // when: the user creates a fabric with a missing color; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID", "RED_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof ColorsMissingError
                && ((ColorsMissingError) e.getCause()).getMissingColors().equals(Set.of(ColorId.of("RED_ID"))));

        // when: the color is marked as available
        markColorAsAvailable("RED_ID", "Red", 255, 0, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates a fabric with all colors
        createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID", "RED_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // then: the fabric is created
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(1);

        // when: the color is marked as unavailable again
        markColorAsUnavailable("RED_ID");

        // and: the user tries to create a fabric with all colors; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID", "RED_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof ColorsMissingError
                && ((ColorsMissingError) e.getCause()).getMissingColors().equals(Set.of(ColorId.of("RED_ID"))));
    }

    @Test
    void shouldNotCreateFabricWhenFabricTypesAreMissing() {
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

        // when: the user creates a fabric with a missing fabric type; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof FabricTypesMissingError
                && ((FabricTypesMissingError) e.getCause()).getMissingFabricTypes()
                .equals(Set.of(FabricTypeId.of("COTTON_ID"))));

        // when: the fabric type is marked as available
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates a fabric with all fabric types
        createFabric(
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

        // when: the fabric type is marked as unavailable again
        markFabricTypeAsUnavailable("COTTON_ID");

        // and: the user tries to create a fabric with all fabric types; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        )).matches(e -> e.getCause() instanceof FabricTypesMissingError
                && ((FabricTypesMissingError) e.getCause()).getMissingFabricTypes()
                .equals(Set.of(FabricTypeId.of("COTTON_ID"))));
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
    void shouldNotCreateFabricWhenAliasIsAlreadyInUse() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");

        // and: a fabric is created
        String fabricId = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        );

        // when: another fabric with the same name (and thus alias) is created; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice bear party",
                "OTHER_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError
                && ((AliasAlreadyInUseError) e.getCause()).getConflictingFabricId().equals(FabricId.of(fabricId))
                && ((AliasAlreadyInUseError) e.getCause()).getAlias().equals(FabricAlias.of("ice-bear-party")));

        // when: a fabric with a different name that slugifies to the same alias is created; then: an error is raised
        assertThatThrownBy(() -> createFabric(
                "Ice-Bear-Party",
                "OTHER_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        )).matches(e -> e.getCause() instanceof AliasAlreadyInUseError);

        // when: a fabric with a different name is created; then: no error is raised
        createFabric(
                "Summer flowers",
                "SUMMER_IMAGE_ID",
                Set.of("BLUE_ID"),
                Set.of("WINTER_ID"),
                Set.of(jerseyAvailability),
                agent
        );

        // then: two fabrics exist
        var fabrics = getFabrics(agent);
        assertThat(fabrics).hasSize(2);
    }

    @Test
    void shouldCreateMultipleFabrics() {
        // given: a user is allowed to create topics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");
        markTopicAsAvailable("SUMMER_ID", "Summer");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("RED_ID", "Red", 255, 0, 0);
        markColorAsAvailable("YELLOW_ID", "Yellow", 255, 255, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

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
