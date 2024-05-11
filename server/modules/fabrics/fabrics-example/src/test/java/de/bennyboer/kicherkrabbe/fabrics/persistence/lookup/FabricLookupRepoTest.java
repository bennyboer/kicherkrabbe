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
