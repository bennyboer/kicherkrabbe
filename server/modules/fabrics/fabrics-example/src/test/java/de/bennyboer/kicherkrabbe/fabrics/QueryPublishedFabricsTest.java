package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricTypeAvailabilityDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsAvailabilityFilterDTO;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDTO;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDirectionDTO.ASCENDING;
import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortDirectionDTO.DESCENDING;
import static de.bennyboer.kicherkrabbe.fabrics.http.api.FabricsSortPropertyDTO.ALPHABETICAL;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryPublishedFabricsTest extends FabricsModuleTest {

    @Test
    void shouldQueryPublishedFabrics() {
        // given: a user is allowed to create fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

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

        // when: the user queries the published fabrics
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: the published fabrics are returned (there are none)
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();

        // when: the fabrics are published
        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // and: the user queries the published fabrics
        result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: the published fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);

        var fabric1 = result.getResults()
                .stream()
                .filter(f -> f.getId().equals(FabricId.of(fabricId1)))
                .findFirst()
                .orElseThrow();
        assertThat(fabric1.getName()).isEqualTo(FabricName.of("Ice bear party"));
        assertThat(fabric1.getImage()).isEqualTo(ImageId.of("ICE_BEAR_IMAGE_ID"));
        assertThat(fabric1.getColors()).containsExactlyInAnyOrder(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric1.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric1.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );

        var fabric2 = result.getResults()
                .stream()
                .filter(f -> f.getId().equals(FabricId.of(fabricId2)))
                .findFirst()
                .orElseThrow();
        assertThat(fabric2.getName()).isEqualTo(FabricName.of("Penguin party"));
        assertThat(fabric2.getImage()).isEqualTo(ImageId.of("PENGUIN_IMAGE_ID"));
        assertThat(fabric2.getColors()).containsExactlyInAnyOrder(ColorId.of("BLACK_ID"), ColorId.of("WHITE_ID"));
        assertThat(fabric2.getTopics()).containsExactlyInAnyOrder(TopicId.of("WINTER_ID"), TopicId.of("ANIMALS_ID"));
        assertThat(fabric2.getAvailability()).containsExactlyInAnyOrder(
                FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
        );

        // and: the results are sorted alphabetically in ascending order
        assertThat(result.getResults().get(0).getName().getValue()).isEqualTo("Ice bear party");
        assertThat(result.getResults().get(1).getName().getValue()).isEqualTo("Penguin party");

        // when: an anonymous user queries the published fabrics
        result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                Agent.anonymous()
        );

        // then: the published fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);

        // when: the system user queries the published fabrics
        result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                Agent.system()
        );

        // then: the published fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);
    }

    @Test
    void shouldFilterBySearchTerm() {
        // given: some fabrics are published
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

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

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with a search term
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "Penguin",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: the published fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Penguin party"));

        // when: the user queries the published fabrics with a search term that does not match any fabric
        result = getPublishedFabrics(
                "test",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: no fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();

        // when: the user queries the published fabrics with a search term that matches multiple fabrics
        result = getPublishedFabrics(
                "party",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: all matching fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);
    }

    @Test
    void shouldFilterByAvailability() {
        // given: some fabrics are published
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        var availableInJersey = new FabricTypeAvailabilityDTO();
        availableInJersey.typeId = "JERSEY_ID";
        availableInJersey.inStock = true;

        var notAvailableInJersey = new FabricTypeAvailabilityDTO();
        notAvailableInJersey.typeId = "JERSEY_ID";
        notAvailableInJersey.inStock = false;

        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(availableInJersey),
                agent
        );
        String fabricId2 = createFabric(
                "Penguin party",
                "PENGUIN_IMAGE_ID",
                Set.of("BLACK_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID"),
                Set.of(notAvailableInJersey),
                agent
        );

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with an availability filter
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = true;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: only the matching fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));

        // when: the availability filter is inactive
        availability.active = false;

        result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: all fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(2);
    }

    @Test
    void shouldSortAlphabeticallyDescending() {
        // given: some fabrics are published
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

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

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with a descending alphabetical sort
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = DESCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: the results are sorted alphabetically in descending order
        assertThat(result.getResults().get(0).getName().getValue()).isEqualTo("Penguin party");
        assertThat(result.getResults().get(1).getName().getValue()).isEqualTo("Ice bear party");
    }

    @Test
    void shouldFilterByColors() {
        // given: some fabrics are published
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

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

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with a color filter
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of("BLUE_ID"),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: only the matching fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));

        // when: we filter by the black color
        result = getPublishedFabrics(
                "",
                Set.of("BLACK_ID"),
                Set.of(),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: only the matching fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Penguin party"));
    }

    @Test
    void shouldFilterByTopic() {
        // given: some fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

        String fabricId1 = createFabric(
                "Ice bear party",
                "ICE_BEAR_IMAGE_ID",
                Set.of("BLUE_ID", "WHITE_ID"),
                Set.of("WINTER_ID", "ANIMALS_ID", "PARTY_ID"),
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

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with a topic filter
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of("PARTY_ID"),
                availability,
                sort,
                0,
                100,
                agent
        );

        // then: only the matching fabrics are returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(100);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(1);

        var fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));
    }

    @Test
    void shouldDoPaging() {
        // given: some fabrics
        allowUserToCreateFabrics("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));

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

        publishFabric(fabricId1, 0L, agent);
        publishFabric(fabricId2, 0L, agent);

        // when: the user queries the published fabrics with a limit
        var availability = new FabricsAvailabilityFilterDTO();
        availability.active = false;
        availability.inStock = true;

        var sort = new FabricsSortDTO();
        sort.property = ALPHABETICAL;
        sort.direction = ASCENDING;

        var result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                0,
                1,
                agent
        );

        // then: only the first fabric is returned
        assertThat(result.getSkip()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(1);

        var fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Ice bear party"));

        // when: the user queries the published fabrics with a skip
        result = getPublishedFabrics(
                "",
                Set.of(),
                Set.of(),
                availability,
                sort,
                1,
                1,
                agent
        );

        // then: only the second fabric is returned
        assertThat(result.getSkip()).isEqualTo(1);
        assertThat(result.getLimit()).isEqualTo(1);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getResults()).hasSize(1);

        fabric = result.getResults().get(0);
        assertThat(fabric.getName()).isEqualTo(FabricName.of("Penguin party"));
    }

}
