package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryName;
import de.bennyboer.kicherkrabbe.embroideries.samples.SampleLookupEmbroidery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EmbroideryLookupRepoTest {

    private EmbroideryLookupRepo repo;

    protected abstract EmbroideryLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateEmbroidery() {
        // given: an embroidery to update
        var embroidery = SampleLookupEmbroidery.builder()
                .category(EmbroideryCategoryId.of("ANIMALS"))
                .build()
                .toModel();

        // when: updating the embroidery
        update(embroidery);

        // then: the embroidery is updated
        var embroideries = find(Set.of(embroidery.getId()));
        assertThat(embroideries).containsExactly(embroidery);
    }

    @Test
    void shouldRemoveEmbroidery() {
        // given: some embroideries
        var embroidery1 = SampleLookupEmbroidery.builder().build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().build().toModel();
        update(embroidery1);
        update(embroidery2);

        // when: removing an embroidery
        remove(embroidery1.getId());

        // then: the embroidery is removed
        var embroideries = find(Set.of(embroidery1.getId(), embroidery2.getId()));
        assertThat(embroideries).containsExactly(embroidery2);
    }

    @Test
    void shouldFindEmbroideries() {
        // given: some embroideries
        var embroidery1 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder().build().toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);

        // when: finding embroideries
        var embroideries = find(Set.of(embroidery1.getId(), embroidery2.getId()));

        // then: only the requested embroideries are found sorted by creation date
        assertThat(embroideries).containsExactly(embroidery2, embroidery1);
    }

    @Test
    void shouldFindEmbroideriesBySearchTerm() {
        // given: some embroideries with different names
        var embroidery1 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("Crab"))
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("Rose"))
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("Little fox"))
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);
        var ids = Set.of(embroidery1.getId(), embroidery2.getId(), embroidery3.getId());

        // when: finding embroideries by search term
        var embroideries = find(ids, "o");

        // then: the matching embroideries are found
        assertThat(embroideries).containsExactly(embroidery3, embroidery2);

        // when: finding embroideries by a case insensitive search term
        embroideries = find(ids, "CRAB");

        // then: the matching embroidery is found
        assertThat(embroideries).containsExactly(embroidery1);

        // when: finding embroideries by a blank search term
        embroideries = find(ids, "    ");

        // then: all embroideries are found
        assertThat(embroideries).containsExactly(embroidery3, embroidery2, embroidery1);

        // when: finding embroideries by a non-matching search term
        embroideries = find(ids, "blblblbll");

        // then: no embroideries are found
        assertThat(embroideries).isEmpty();
    }

    @Test
    void shouldFindEmbroideriesByCategoriesFilter() {
        // given: some embroideries with different categories
        var animals = EmbroideryCategoryId.of("ANIMALS");
        var flowers = EmbroideryCategoryId.of("FLOWERS");
        var embroidery1 = SampleLookupEmbroidery.builder()
                .category(animals)
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .category(flowers)
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);
        var ids = Set.of(embroidery1.getId(), embroidery2.getId(), embroidery3.getId());

        // when: finding embroideries with a category filter
        var page = repo.find(ids, Set.of(animals), "", 0, Integer.MAX_VALUE).block();

        // then: only embroideries with that category are found
        assertThat(page.getResults()).containsExactly(embroidery1);
        assertThat(page.getTotal()).isEqualTo(1);

        // when: finding embroideries with multiple categories
        page = repo.find(ids, Set.of(animals, flowers), "", 0, Integer.MAX_VALUE).block();

        // then: embroideries with any of the categories are found
        assertThat(page.getResults()).containsExactly(embroidery2, embroidery1);
    }

    @Test
    void shouldFindEmbroideriesWithPaging() {
        // given: some embroideries
        var embroidery1 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-12T12:30:00.00Z"))
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-12T12:00:00.00Z"))
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder()
                .createdAt(Instant.parse("2024-03-11T11:00:00.00Z"))
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);
        var ids = Set.of(embroidery1.getId(), embroidery2.getId(), embroidery3.getId());

        // when: finding the second page of size one
        var page = repo.find(ids, Set.of(), "", 1, 1).block();

        // then: the second embroidery is found together with the total count
        assertThat(page.getResults()).containsExactly(embroidery2);
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getSkip()).isEqualTo(1);
        assertThat(page.getLimit()).isEqualTo(1);

        // when: finding a page beyond the last embroidery
        page = repo.find(ids, Set.of(), "", 3, 1).block();

        // then: no embroideries are found but the total is still correct
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(3);
    }

    @Test
    void shouldFindEmbroideryById() {
        // given: some embroideries
        var embroidery1 = SampleLookupEmbroidery.builder().build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().build().toModel();
        update(embroidery1);
        update(embroidery2);

        // when: finding an embroidery by ID
        var found = repo.findById(embroidery2.getId()).block();

        // then: the embroidery is found
        assertThat(found).isEqualTo(embroidery2);

        // when: finding an embroidery by an unknown ID
        found = repo.findById(EmbroideryId.of("UNKNOWN")).block();

        // then: no embroidery is found
        assertThat(found).isNull();
    }

    @Test
    void shouldFindEmbroideryByAlias() {
        // given: some embroideries with different aliases
        var embroidery1 = SampleLookupEmbroidery.builder()
                .alias(EmbroideryAlias.of("crab"))
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .alias(EmbroideryAlias.of("rose"))
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);

        // when: finding an embroidery by alias
        var found = findByAlias(EmbroideryAlias.of("crab"));

        // then: the embroidery is found
        assertThat(found).isEqualTo(embroidery1);

        // when: finding an embroidery by an unknown alias
        found = findByAlias(EmbroideryAlias.of("non-existing-alias"));

        // then: no embroidery is found
        assertThat(found).isNull();
    }

    @Test
    void shouldFindEmbroideriesByCategory() {
        // given: some embroideries with different categories
        var animals = EmbroideryCategoryId.of("ANIMALS");
        var flowers = EmbroideryCategoryId.of("FLOWERS");
        var embroidery1 = SampleLookupEmbroidery.builder().category(animals).build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().category(flowers).build().toModel();
        var embroidery3 = SampleLookupEmbroidery.builder().category(flowers).category(animals).build().toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);

        // when: finding embroideries by category
        var embroideries = findByCategory(flowers);

        // then: the embroideries with that category are found
        assertThat(embroideries).containsExactlyInAnyOrder(embroidery2, embroidery3);

        // when: finding embroideries by another category
        embroideries = findByCategory(animals);

        // then: the embroideries with the other category are found
        assertThat(embroideries).containsExactlyInAnyOrder(embroidery1, embroidery3);

        // when: finding embroideries by a category that is not used
        embroideries = findByCategory(EmbroideryCategoryId.of("UNUSED"));

        // then: no embroideries are found
        assertThat(embroideries).isEmpty();
    }

    @Test
    void shouldFindUniqueCategories() {
        // given: some embroideries with different categories
        var animals = EmbroideryCategoryId.of("ANIMALS");
        var flowers = EmbroideryCategoryId.of("FLOWERS");
        var embroidery1 = SampleLookupEmbroidery.builder().category(animals).build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().category(flowers).build().toModel();
        var embroidery3 = SampleLookupEmbroidery.builder().category(flowers).category(animals).build().toModel();
        var embroidery4 = SampleLookupEmbroidery.builder().build().toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);
        update(embroidery4);

        // when: finding unique categories
        var categories = findUniqueCategories();

        // then: each used category is found once
        assertThat(categories).containsExactlyInAnyOrder(animals, flowers);

        // when: removing all embroideries and finding unique categories
        remove(embroidery1.getId());
        remove(embroidery2.getId());
        remove(embroidery3.getId());
        remove(embroidery4.getId());
        categories = findUniqueCategories();

        // then: no categories are found
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldFindPublishedEmbroidery() {
        // given: a published and an unpublished embroidery
        var embroidery1 = SampleLookupEmbroidery.builder().published(true).build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().published(false).build().toModel();
        update(embroidery1);
        update(embroidery2);

        // when: finding the published embroidery
        var found = repo.findPublished(embroidery1.getId()).block();

        // then: the published embroidery is found
        assertThat(found).isEqualTo(embroidery1);

        // when: finding the unpublished embroidery
        found = repo.findPublished(embroidery2.getId()).block();

        // then: the unpublished embroidery is not found
        assertThat(found).isNull();
    }

    @Test
    void shouldFindPublishedEmbroideryByAlias() {
        // given: a published and an unpublished embroidery
        var embroidery1 = SampleLookupEmbroidery.builder()
                .alias(EmbroideryAlias.of("crab"))
                .published(true)
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .alias(EmbroideryAlias.of("rose"))
                .published(false)
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);

        // when: finding the published embroidery by alias
        var found = repo.findPublishedByAlias(EmbroideryAlias.of("crab")).block();

        // then: the published embroidery is found
        assertThat(found).isEqualTo(embroidery1);

        // when: finding the unpublished embroidery by alias
        found = repo.findPublishedByAlias(EmbroideryAlias.of("rose")).block();

        // then: the unpublished embroidery is not found
        assertThat(found).isNull();
    }

    @Test
    void shouldFindPublishedEmbroideries() {
        // given: some embroideries with different names
        var embroidery1 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("C"))
                .published(true)
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("B"))
                .published(false)
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("A"))
                .published(true)
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);

        // when: finding all published embroideries ordered by name ascending
        var page = findPublished("", Set.of(), true, 0, 10);

        // then: only published embroideries are found ordered by name ascending
        assertThat(page.getResults()).containsExactly(embroidery3, embroidery1);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding all published embroideries ordered by name descending
        page = findPublished("", Set.of(), false, 0, 10);

        // then: only published embroideries are found ordered by name descending
        assertThat(page.getResults()).containsExactly(embroidery1, embroidery3);

        // when: finding published embroideries by search term
        page = findPublished("a", Set.of(), true, 0, 10);

        // then: only matching published embroideries are found
        assertThat(page.getResults()).containsExactly(embroidery3);

        // when: finding published embroideries with paging
        page = findPublished("", Set.of(), true, 1, 1);

        // then: the second published embroidery is found
        assertThat(page.getResults()).containsExactly(embroidery1);
        assertThat(page.getTotal()).isEqualTo(2);
    }

    @Test
    void shouldFindPublishedEmbroideriesByCategory() {
        // given: some published embroideries with different categories
        var animals = EmbroideryCategoryId.of("ANIMALS");
        var flowers = EmbroideryCategoryId.of("FLOWERS");
        var embroidery1 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("A"))
                .category(animals)
                .published(true)
                .build()
                .toModel();
        var embroidery2 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("B"))
                .category(flowers)
                .published(true)
                .build()
                .toModel();
        var embroidery3 = SampleLookupEmbroidery.builder()
                .name(EmbroideryName.of("C"))
                .category(animals)
                .published(false)
                .build()
                .toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);

        // when: finding published embroideries with a category filter
        var page = findPublished("", Set.of(animals), true, 0, 10);

        // then: only published embroideries with that category are found
        assertThat(page.getResults()).containsExactly(embroidery1);

        // when: finding published embroideries with multiple categories
        page = findPublished("", Set.of(animals, flowers), true, 0, 10);

        // then: published embroideries with any of the categories are found
        assertThat(page.getResults()).containsExactly(embroidery1, embroidery2);
    }

    @Test
    void shouldFindFeaturedEmbroideries() {
        // given: embroideries with different published and featured states
        var embroidery1 = SampleLookupEmbroidery.builder().published(true).featured(true).build().toModel();
        var embroidery2 = SampleLookupEmbroidery.builder().published(true).featured(false).build().toModel();
        var embroidery3 = SampleLookupEmbroidery.builder().published(false).featured(true).build().toModel();
        update(embroidery1);
        update(embroidery2);
        update(embroidery3);

        // when: finding featured embroideries
        var featured = repo.findFeatured().collectList().block();

        // then: only published and featured embroideries are found
        assertThat(featured).containsExactly(embroidery1);
    }

    private void update(LookupEmbroidery embroidery) {
        repo.update(embroidery).block();
    }

    private void remove(EmbroideryId id) {
        repo.remove(id).block();
    }

    private List<LookupEmbroidery> find(Set<EmbroideryId> ids) {
        return find(ids, "");
    }

    private List<LookupEmbroidery> find(Set<EmbroideryId> ids, String searchTerm) {
        return repo.find(ids, Set.of(), searchTerm, 0, Integer.MAX_VALUE).block().getResults();
    }

    private LookupEmbroidery findByAlias(EmbroideryAlias alias) {
        return repo.findByAlias(alias).block();
    }

    private List<LookupEmbroidery> findByCategory(EmbroideryCategoryId categoryId) {
        return repo.findByCategory(categoryId).collectList().block();
    }

    private List<EmbroideryCategoryId> findUniqueCategories() {
        return repo.findUniqueCategories().collectList().block();
    }

    private LookupEmbroideryPage findPublished(
            String searchTerm,
            Set<EmbroideryCategoryId> categories,
            boolean ascending,
            long skip,
            long limit
    ) {
        return repo.findPublished(searchTerm, categories, ascending, skip, limit).block();
    }

}
