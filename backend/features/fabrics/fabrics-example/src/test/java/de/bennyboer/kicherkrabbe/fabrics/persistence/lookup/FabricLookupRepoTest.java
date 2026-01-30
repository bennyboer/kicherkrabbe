package de.bennyboer.kicherkrabbe.fabrics.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.fabrics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class FabricLookupRepoTest {

    private FabricLookupRepo repo;

    protected abstract FabricLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateFabric() {
        // given: a fabric to update
        var fabric = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                false,
                false,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the fabric
        update(fabric);

        // then: the fabric is updated
        var fabrics = find(Set.of(fabric.getId()));
        assertThat(fabrics).containsExactly(fabric);
    }

    @Test
    void shouldRemoveFabric() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(fabric1);
        update(fabric2);

        // when: removing a fabric
        remove(fabric1.getId());

        // then: the fabric is removed
        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));
        assertThat(fabrics).containsExactly(fabric2);
    }

    @Test
    void shouldFindFabrics() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(fabric1);
        update(fabric2);

        // when: finding fabrics
        var fabrics = find(Set.of(fabric1.getId(), fabric2.getId()));

        // then: the fabrics are found sorted by creation date
        assertThat(fabrics).containsExactly(fabric2, fabric1);
    }

    @Test
    void shouldFindFabricsBySearchTerm() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Owls"),
                ImageId.of("OWL_IMAGE_ID"),
                Set.of(ColorId.of("BROWN_ID"), ColorId.of("GREEN_ID")),
                Set.of(TopicId.of("Night"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by search term
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var fabrics = find(fabricIds, "o");

        // then: the fabrics are found by search term
        assertThat(fabrics).containsExactly(fabric2, fabric3);

        // when: finding fabrics by another search term
        fabrics = find(fabricIds, "r");

        // then: the fabrics are found by another search term
        assertThat(fabrics).containsExactly(fabric2, fabric1);

        // when: finding fabrics by another search term
        fabrics = find(fabricIds, "    ");

        // then: the fabrics are found by another search term
        assertThat(fabrics).containsExactly(fabric2, fabric3, fabric1);

        // when: finding fabrics by another search term
        fabrics = find(fabricIds, "blblblbll");

        // then: the fabrics are found by another search term
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsWithPaging() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                false,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Owls"),
                ImageId.of("OWL_IMAGE_ID"),
                Set.of(ColorId.of("BROWN_ID"), ColorId.of("GREEN_ID")),
                Set.of(TopicId.of("Night"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics with paging
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var fabrics = find(fabricIds, 1, 1);

        // then: the fabrics are found with paging
        assertThat(fabrics).containsExactly(fabric3);

        // when: finding fabrics with paging
        fabrics = find(fabricIds, 2, 1);

        // then: the fabrics are found with paging
        assertThat(fabrics).containsExactly(fabric1);

        // when: finding fabrics with paging
        fabrics = find(fabricIds, 3, 1);

        // then: the fabrics are found with paging
        assertThat(fabrics).isEmpty();

        // when: finding fabrics with paging
        fabrics = find(fabricIds, 0, 2);

        // then: the fabrics are found with paging
        assertThat(fabrics).containsExactly(fabric2, fabric3);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Owls"),
                ImageId.of("OWL_IMAGE_ID"),
                Set.of(ColorId.of("BROWN_ID"), ColorId.of("GREEN_ID")),
                Set.of(TopicId.of("Night"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics with search term and paging
        var fabricIds = Set.of(fabric1.getId(), fabric2.getId(), fabric3.getId());
        var page = findPage(fabricIds, "r", 0, 1);

        // then: the fabrics are found with search term and paging
        assertThat(page.getResults()).containsExactly(fabric2);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding fabrics with search term and paging
        page = findPage(fabricIds, "color", 1, 1);

        // then: the fabrics are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindPublishedFabric() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        update(fabric1);
        update(fabric2);

        // when: finding the first fabric
        var foundFabric1 = findPublished(fabric1.getId());

        // then: the first fabric is found
        assertThat(foundFabric1).isEqualTo(fabric1);

        // when: finding the second fabric
        var foundFabric2 = findPublished(fabric2.getId());

        // then: the second fabric is not found
        assertThat(foundFabric2).isNull();
    }

    @Test
    void shouldFindPublishedFabrics() {
        // given: some fabrics
        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Ice bear party"),
                ImageId.of("ICE_BEAR_IMAGE_ID"),
                Set.of(ColorId.of("BLUE_ID"), ColorId.of("WHITE_ID")),
                Set.of(TopicId.of("Winter"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), true),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Colorful"),
                ImageId.of("COLORFUL_IMAGE_ID"),
                Set.of(ColorId.of("RED_ID"), ColorId.of("YELLOW_ID")),
                Set.of(TopicId.of("Summer"), TopicId.of("Colors")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), false)
                ),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Owls"),
                ImageId.of("OWL_IMAGE_ID"),
                Set.of(ColorId.of("BROWN_ID"), ColorId.of("GREEN_ID")),
                Set.of(TopicId.of("Night"), TopicId.of("Animals")),
                Set.of(
                        FabricTypeAvailability.of(FabricTypeId.of("JERSEY_ID"), false),
                        FabricTypeAvailability.of(FabricTypeId.of("COTTON_ID"), true)
                ),
                false,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding published fabrics
        var result = findPublished(
                "",
                Set.of(),
                Set.of(),
                false,
                false,
                true,
                0,
                10
        );

        // then: all published fabrics are found ordered by name ascending
        assertThat(result.getResults()).containsExactly(fabric2, fabric1);

        // when: finding published fabrics ordered by name descending
        result = findPublished(
                "",
                Set.of(),
                Set.of(),
                false,
                false,
                false,
                0,
                10
        );

        // then: all published fabrics are found ordered by name descending
        assertThat(result.getResults()).containsExactly(fabric1, fabric2);

        // when: finding published fabrics with search term
        result = findPublished(
                "o",
                Set.of(),
                Set.of(),
                false,
                false,
                true,
                0,
                10
        );

        // then: all published fabrics are found with search term
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with color filter
        result = findPublished(
                "",
                Set.of(ColorId.of("BLUE_ID")),
                Set.of(),
                false,
                false,
                true,
                0,
                10
        );

        // then: all published fabrics are found with color filter
        assertThat(result.getResults()).containsExactly(fabric1);

        // when: finding published fabrics with topic filter
        result = findPublished(
                "",
                Set.of(),
                Set.of(TopicId.of("Summer")),
                false,
                false,
                true,
                0,
                10
        );

        // then: all published fabrics are found with topic filter
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with availability filter
        result = findPublished(
                "",
                Set.of(),
                Set.of(),
                true,
                true,
                true,
                0,
                10
        );

        // then: all published fabrics are found with availability filter
        assertThat(result.getResults()).containsExactly(fabric1);

        // when: finding published fabrics with availability filter where we want fabrics that are not in stock
        result = findPublished(
                "",
                Set.of(),
                Set.of(),
                true,
                false,
                true,
                0,
                10
        );

        // then: all published fabrics are found with availability filter
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with paging
        result = findPublished(
                "",
                Set.of(),
                Set.of(),
                false,
                false,
                true,
                0,
                1
        );

        // then: all published fabrics are found with paging
        assertThat(result.getResults()).containsExactly(fabric2);

        // when: finding published fabrics with paging
        result = findPublished(
                "",
                Set.of(),
                Set.of(),
                false,
                false,
                true,
                1,
                1
        );

        // then: all published fabrics are found with paging
        assertThat(result.getResults()).containsExactly(fabric1);
    }

    @Test
    void shouldFindFabricsByColor() {
        // given: some fabrics with different colors
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");
        var colorId3 = ColorId.of("COLOR_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(colorId1),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(colorId2),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(colorId3, colorId2),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by color
        var fabrics = findByColor(colorId2);

        // then: the fabrics are found by color
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another color
        fabrics = findByColor(colorId1);

        // then: the fabrics are found by another color
        assertThat(fabrics).containsExactly(fabric1);

        // when: finding fabrics by another color that is not used
        fabrics = findByColor(ColorId.of("COLOR_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsByTopic() {
        // given: some fabrics with different topics
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");
        var topicId3 = TopicId.of("TOPIC_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(),
                Set.of(topicId1),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(),
                Set.of(topicId2),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(),
                Set.of(topicId3, topicId2),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by topic
        var fabrics = findByTopic(topicId2);

        // then: the fabrics are found by topic
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another topic
        fabrics = findByTopic(topicId1);

        // then: the fabrics are found by another topic
        assertThat(fabrics).containsExactly(fabric1);

        // when: finding fabrics by another topic that is not used
        fabrics = findByTopic(TopicId.of("TOPIC_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindFabricsByFabricType() {
        // given: some fabrics with different fabric types
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");
        var fabricTypeId3 = FabricTypeId.of("FABRIC_TYPE_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(),
                Set.of(),
                Set.of(FabricTypeAvailability.of(fabricTypeId1, true)),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(),
                Set.of(),
                Set.of(FabricTypeAvailability.of(fabricTypeId2, true)),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(),
                Set.of(),
                Set.of(
                        FabricTypeAvailability.of(fabricTypeId2, true),
                        FabricTypeAvailability.of(fabricTypeId3, true)
                ),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding fabrics by fabric type
        var fabrics = findByFabricType(fabricTypeId2);

        // then: the fabrics are found by fabric type
        assertThat(fabrics).containsExactlyInAnyOrder(fabric2, fabric3);

        // when: finding fabrics by another fabric type
        fabrics = findByFabricType(fabricTypeId1);

        // then: the fabrics are found by another fabric type
        assertThat(fabrics).containsExactly(fabric1);

        // when: finding fabrics by another fabric type that is not used
        fabrics = findByFabricType(FabricTypeId.of("FABRIC_TYPE_ID_4"));

        // then: no fabrics are found
        assertThat(fabrics).isEmpty();
    }

    @Test
    void shouldFindUniqueColors() {
        // given: some fabrics with different colors
        var colorId1 = ColorId.of("COLOR_ID_1");
        var colorId2 = ColorId.of("COLOR_ID_2");
        var colorId3 = ColorId.of("COLOR_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(colorId1),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(colorId2),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(colorId3, colorId2),
                Set.of(),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique colors
        var colors = findUniqueColors();

        // then: the unique colors are found
        assertThat(colors).containsExactlyInAnyOrder(colorId1, colorId2, colorId3);

        // when: finding unique colors with no fabrics
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());
        colors = findUniqueColors();

        // then: no unique colors are found
        assertThat(colors).isEmpty();
    }

    @Test
    void shouldFindUniqueTopics() {
        // given: some fabrics with different topics
        var topicId1 = TopicId.of("TOPIC_ID_1");
        var topicId2 = TopicId.of("TOPIC_ID_2");
        var topicId3 = TopicId.of("TOPIC_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(),
                Set.of(topicId1),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(),
                Set.of(topicId2),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(),
                Set.of(topicId3, topicId2),
                Set.of(),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique topics
        var topics = findUniqueTopics();

        // then: the unique topics are found
        assertThat(topics).containsExactlyInAnyOrder(topicId1, topicId2, topicId3);

        // when: finding unique topics with no fabrics
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());

        topics = findUniqueTopics();

        // then: no unique topics are found
        assertThat(topics).isEmpty();
    }

    @Test
    void shouldFindUniqueFabricTypes() {
        // given: some fabrics with different fabric types
        var fabricTypeId1 = FabricTypeId.of("FABRIC_TYPE_ID_1");
        var fabricTypeId2 = FabricTypeId.of("FABRIC_TYPE_ID_2");
        var fabricTypeId3 = FabricTypeId.of("FABRIC_TYPE_ID_3");

        var fabric1 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 1"),
                ImageId.of("IMAGE_ID_1"),
                Set.of(),
                Set.of(),
                Set.of(FabricTypeAvailability.of(fabricTypeId1, true)),
                true,
                false,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var fabric2 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 2"),
                ImageId.of("IMAGE_ID_2"),
                Set.of(),
                Set.of(),
                Set.of(FabricTypeAvailability.of(fabricTypeId2, true)),
                true,
                false,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var fabric3 = LookupFabric.of(
                FabricId.create(),
                Version.zero(),
                FabricName.of("Fabric 3"),
                ImageId.of("IMAGE_ID_3"),
                Set.of(),
                Set.of(),
                Set.of(
                        FabricTypeAvailability.of(fabricTypeId2, true),
                        FabricTypeAvailability.of(fabricTypeId3, true)
                ),
                true,
                false,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );

        update(fabric1);
        update(fabric2);
        update(fabric3);

        // when: finding unique fabric types
        var fabricTypes = findUniqueFabricTypes();

        // then: the unique fabric types are found
        assertThat(fabricTypes).containsExactlyInAnyOrder(fabricTypeId1, fabricTypeId2, fabricTypeId3);

        // when: finding unique fabric types with no fabrics
        remove(fabric1.getId());
        remove(fabric2.getId());
        remove(fabric3.getId());

        fabricTypes = findUniqueFabricTypes();

        // then: no unique fabric types are found
        assertThat(fabricTypes).isEmpty();
    }

    private List<ColorId> findUniqueColors() {
        return repo.findUniqueColors().collectList().block();
    }

    private List<TopicId> findUniqueTopics() {
        return repo.findUniqueTopics().collectList().block();
    }

    private List<FabricTypeId> findUniqueFabricTypes() {
        return repo.findUniqueFabricTypes().collectList().block();
    }

    private List<LookupFabric> findByColor(ColorId colorId) {
        return repo.findByColor(colorId).collectList().block();
    }

    private List<LookupFabric> findByTopic(TopicId topicId) {
        return repo.findByTopic(topicId).collectList().block();
    }

    private List<LookupFabric> findByFabricType(FabricTypeId fabricTypeId) {
        return repo.findByFabricType(fabricTypeId).collectList().block();
    }

    private LookupFabric findPublished(FabricId id) {
        return repo.findPublished(id).block();
    }

    private LookupFabricPage findPublished(
            String searchTerm,
            Set<ColorId> colors,
            Set<TopicId> topics,
            boolean filterAvailability,
            boolean inStock,
            boolean ascending,
            long skip,
            long limit
    ) {
        return repo.findPublished(
                searchTerm,
                colors,
                topics,
                filterAvailability,
                inStock,
                ascending,
                skip,
                limit
        ).block();
    }

    private void update(LookupFabric fabric) {
        repo.update(fabric).block();
    }

    private void remove(FabricId fabricId) {
        repo.remove(fabricId).block();
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds) {
        return find(fabricIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, String searchTerm) {
        return find(fabricIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, long skip, long limit) {
        return find(fabricIds, "", skip, limit);
    }

    private List<LookupFabric> find(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        return repo.find(fabricIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupFabricPage findPage(Collection<FabricId> fabricIds, String searchTerm, long skip, long limit) {
        return repo.find(fabricIds, searchTerm, skip, limit).block();
    }

}
