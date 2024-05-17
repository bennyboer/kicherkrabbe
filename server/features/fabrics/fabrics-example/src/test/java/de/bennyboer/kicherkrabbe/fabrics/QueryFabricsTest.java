package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryFabricsAsUser() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        // and: some topics are available
        markTopicAsAvailable("WINTER_ID", "Winter");
        markTopicAsAvailable("ANIMALS_ID", "Animals");

        // and: some colors are available
        markColorAsAvailable("BLUE_ID", "Blue", 0, 0, 255);
        markColorAsAvailable("WHITE_ID", "White", 255, 255, 255);
        markColorAsAvailable("BLACK_ID", "Black", 0, 0, 0);

        // and: some fabric types are available
        markFabricTypeAsAvailable("JERSEY_ID", "Jersey");
        markFabricTypeAsAvailable("COTTON_ID", "Cotton");

        // and: the user creates some fabrics
        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );
        String fabricId2 = createFabric(
                "Penguin party",
                "PENGUIN_IMAGE_ID",
                Set.of("BLACK_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: the user queries the fabrics
        var fabrics = getFabrics(agent);

        // then: the fabrics are returned
        assertThat(fabrics).hasSize(2);

        var firstFabric = fabrics.stream()
                .filter(fabric -> fabric.getId().equals(FabricId.of(fabricId1)))
                .findFirst()
                .orElseThrow();
        assertThat(firstFabric.getVersion()).isEqualTo(Version.zero());
        assertThat(firstFabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(firstFabric.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(firstFabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(firstFabric.getTopics()).containsExactlyInAnyOrder(
                TopicId.of("WINTER_ID"),
                TopicId.of("ANIMALS_ID")
        );
        assertThat(firstFabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
        assertThat(firstFabric.isPublished()).isFalse();
        assertThat(firstFabric.getCreatedAt()).isNotNull();

        var secondFabric = fabrics.stream()
                .filter(fabric -> fabric.getId().equals(FabricId.of(fabricId2)))
                .findFirst()
                .orElseThrow();
        assertThat(secondFabric.getVersion()).isEqualTo(Version.zero());
        assertThat(secondFabric.getName()).isEqualTo(FabricName.of("Penguin party"));
        assertThat(secondFabric.getImage()).isEqualTo(ImageId.of("PENGUIN_IMAGE_ID"));
        assertThat(secondFabric.getColors()).containsExactlyInAnyOrder(ColorId.of("BLACK_ID"), ColorId.of("WHITE_ID"));
        assertThat(secondFabric.getTopics()).containsExactlyInAnyOrder(
                TopicId.of("WINTER_ID"),
                TopicId.of("ANIMALS_ID")
        );
        assertThat(secondFabric.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );
        assertThat(secondFabric.isPublished()).isFalse();
        assertThat(secondFabric.getCreatedAt()).isNotNull();

        // when: the user queries the fabrics with a search term
        var fabricsWithSearchTerm = getFabrics("bear", 0, 3, agent);

        // then: the fabrics are returned
        assertThat(fabricsWithSearchTerm.getSkip()).isEqualTo(0);
        assertThat(fabricsWithSearchTerm.getLimit()).isEqualTo(3);
        assertThat(fabricsWithSearchTerm.getTotal()).isEqualTo(1);

        var fabricWithSearchTerm = fabricsWithSearchTerm.getResults().get(0);
        assertThat(fabricWithSearchTerm.getId()).isEqualTo(FabricId.of(fabricId1));
    }

    @Test
    void shouldRespondWithEmptyPageWhenUserIsNotAllowedToQueryAFabric() {
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
        createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(jerseyAvailability, cottonAvailability),
                agent
        );

        // when: another user without permission on any fabric tries to query fabrics
        var fabrics = getFabrics(Agent.user(AgentId.of("OTHER_USER_ID")));

        // then: an empty page is returned
        assertThat(fabrics).isEmpty();
    }

}
