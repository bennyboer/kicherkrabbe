package de.bennyboer.kicherkrabbe.categories.persistence.lookup;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.CLOTHING;
import static de.bennyboer.kicherkrabbe.categories.CategoryGroup.NONE;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class CategoryLookupRepoTest {

    private CategoryLookupRepo repo;

    protected abstract CategoryLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateCategory() {
        // given: a category to update
        var category = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );

        // when: updating the category
        update(category);

        // then: the category is updated
        var categories = find(Set.of(category.getId()));
        assertThat(categories).containsExactly(category);
    }

    @Test
    void shouldRemoveCategory() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Skirt"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(category1);
        update(category2);

        // when: removing a category
        remove(category1.getId());

        // then: the category is removed
        var categories = find(Set.of(category1.getId(), category2.getId()));
        assertThat(categories).containsExactly(category2);
    }

    @Test
    void shouldFindCategories() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Top"),
                CLOTHING,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Trousers"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        update(category1);
        update(category2);

        // when: finding categories
        var categories = find(Set.of(category1.getId(), category2.getId()));

        // then: the categories are found sorted by creation date
        assertThat(categories).containsExactly(category2, category1);
    }

    @Test
    void shouldFindCategoriesBySearchTerm() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Onesie"),
                CLOTHING,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var category3 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Trousers"),
                CLOTHING,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(category1);
        update(category2);
        update(category3);

        // when: finding categories by search term
        var categoryIds = Set.of(category1.getId(), category2.getId(), category3.getId());
        var categories = find(categoryIds, "r");

        // then: the categories are found by search term
        assertThat(categories).containsExactly(category3, category1);

        // when: finding categories by another search term
        categories = find(categoryIds, "sie");

        // then: the categories are found by another search term
        assertThat(categories).containsExactly(category2);

        // when: finding categories by another search term
        categories = find(categoryIds, "    ");

        // then: the categories are found by another search term
        assertThat(categories).containsExactly(category2, category3, category1);

        // when: finding categories by another search term
        categories = find(categoryIds, "blblblbll");

        // then: the categories are found by another search term
        assertThat(categories).isEmpty();
    }

    @Test
    void shouldFindCategoriesWithPaging() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Skirt"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var category3 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Top"),
                CLOTHING,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(category1);
        update(category2);
        update(category3);

        // when: finding categories with paging
        var categoryIds = Set.of(category1.getId(), category2.getId(), category3.getId());
        var categories = find(categoryIds, 1, 1);

        // then: the categories are found with paging
        assertThat(categories).containsExactly(category2);

        // when: finding categories with paging
        categories = find(categoryIds, 2, 1);

        // then: the categories are found with paging
        assertThat(categories).containsExactly(category1);

        // when: finding categories with paging
        categories = find(categoryIds, 3, 1);

        // then: the categories are found with paging
        assertThat(categories).isEmpty();

        // when: finding categories with paging
        categories = find(categoryIds, 0, 2);

        // then: the categories are found with paging
        assertThat(categories).containsExactly(category3, category2);
    }

    @Test
    void shouldFindWithSearchTermAndPaging() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Top"),
                CLOTHING,
                Instant.parse("2024-03-12T09:30:00.00Z")
        );
        var category3 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Trousers"),
                CLOTHING,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        update(category1);
        update(category2);
        update(category3);

        // when: finding categories with search term and paging
        var categoryIds = Set.of(category1.getId(), category2.getId(), category3.getId());
        var page = findPage(categoryIds, "r", 0, 1);

        // then: the categories are found with search term and paging
        assertThat(page.getResults()).containsExactly(category3);
        assertThat(page.getTotal()).isEqualTo(2);

        // when: finding categories with search term and paging
        page = findPage(categoryIds, "op", 1, 1);

        // then: the categories are found with search term and paging
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void shouldFindCategoriesByGroup() {
        // given: some categories
        var category1 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Dress"),
                CLOTHING,
                Instant.parse("2024-03-12T13:00:00.00Z")
        );
        var category2 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Skirt"),
                CLOTHING,
                Instant.parse("2024-03-12T12:30:00.00Z")
        );
        var category3 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Top"),
                CLOTHING,
                Instant.parse("2024-03-12T11:00:00.00Z")
        );
        var category4 = LookupCategory.of(
                CategoryId.create(),
                Version.zero(),
                CategoryName.of("Trousers"),
                NONE,
                Instant.parse("2024-03-12T09:00:00.00Z")
        );
        update(category1);
        update(category2);
        update(category3);
        update(category4);

        // when: finding categories by group
        var categories = findByGroup(
                Set.of(category1.getId(), category2.getId(), category3.getId(), category4.getId()),
                CLOTHING,
                "",
                0,
                10
        );

        // then: the categories are found by group
        assertThat(categories).containsExactly(category3, category2, category1);

        // when: finding categories by group with paging
        categories = findByGroup(
                Set.of(category1.getId(), category2.getId(), category3.getId(), category4.getId()),
                CLOTHING,
                "",
                1,
                2
        );

        // then: the categories are found by group with paging
        assertThat(categories).containsExactly(category2, category1);

        // when: finding categories by group with search term
        categories = findByGroup(
                Set.of(category1.getId(), category2.getId(), category3.getId(), category4.getId()),
                CLOTHING,
                "r",
                0,
                10
        );

        // then: the categories are found by group with search term
        assertThat(categories).containsExactly(category2, category1);

        // when: finding categories by another group
        categories = findByGroup(
                Set.of(category1.getId(), category2.getId(), category3.getId(), category4.getId()),
                NONE,
                "",
                0,
                10
        );

        // then: the categories are found by another group
        assertThat(categories).containsExactly(category4);
    }

    private void update(LookupCategory category) {
        repo.update(category).block();
    }

    private void remove(CategoryId categoryId) {
        repo.remove(categoryId).block();
    }

    private List<LookupCategory> find(Collection<CategoryId> categoryIds) {
        return find(categoryIds, "", 0, Integer.MAX_VALUE);
    }

    private List<LookupCategory> find(Collection<CategoryId> categoryIds, String searchTerm) {
        return find(categoryIds, searchTerm, 0, Integer.MAX_VALUE);
    }

    private List<LookupCategory> find(Collection<CategoryId> categoryIds, long skip, long limit) {
        return find(categoryIds, "", skip, limit);
    }

    private List<LookupCategory> find(Collection<CategoryId> categoryIds, String searchTerm, long skip, long limit) {
        return repo.find(categoryIds, searchTerm, skip, limit).block().getResults();
    }

    private LookupCategoryPage findPage(Collection<CategoryId> categoryIds, String searchTerm, long skip, long limit) {
        return repo.find(categoryIds, searchTerm, skip, limit).block();
    }

    private List<LookupCategory> findByGroup(
            Collection<CategoryId> categoryIds,
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit
    ) {
        return repo.findByGroup(categoryIds, group, searchTerm, skip, limit).block().getResults();
    }

}
