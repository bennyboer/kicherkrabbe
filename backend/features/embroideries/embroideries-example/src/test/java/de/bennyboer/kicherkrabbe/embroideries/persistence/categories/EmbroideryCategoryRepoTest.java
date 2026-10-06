package de.bennyboer.kicherkrabbe.embroideries.persistence.categories;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EmbroideryCategoryRepoTest {

    private EmbroideryCategoryRepo repo;

    protected abstract EmbroideryCategoryRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveEmbroideryCategory() {
        // given: a category to save
        var category = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID"),
                EmbroideryCategoryName.of("Category")
        );

        // when: saving the category
        save(category);

        // then: the category is saved
        var saved = findById(category.getId());
        assertThat(saved).isEqualTo(category);
    }

    @Test
    void shouldFindEmbroideryCategoryById() {
        // given: some categories
        var category1 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_1"),
                EmbroideryCategoryName.of("Category 1")
        );
        var category2 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_2"),
                EmbroideryCategoryName.of("Category 2")
        );
        save(category1);
        save(category2);

        // when: finding the first category by id
        var found1 = findById(category1.getId());

        // then: the first category is found
        assertThat(found1).isEqualTo(category1);

        // when: finding the second category by id
        var found2 = findById(category2.getId());

        // then: the second category is found
        assertThat(found2).isEqualTo(category2);
    }

    @Test
    void shouldRemoveEmbroideryCategoryById() {
        // given: some categories
        var category1 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_1"),
                EmbroideryCategoryName.of("Category 1")
        );
        var category2 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_2"),
                EmbroideryCategoryName.of("Category 2")
        );
        save(category1);
        save(category2);

        // when: removing the first category by id
        removeById(category1.getId());

        // then: the first category is removed
        var found1 = findById(category1.getId());
        assertThat(found1).isNull();

        // and: the second category is still there
        var found2 = findById(category2.getId());
        assertThat(found2).isEqualTo(category2);

        // when: removing the second category by id
        removeById(category2.getId());

        // then: the second category is removed
        var found3 = findById(category2.getId());
        assertThat(found3).isNull();
    }

    @Test
    void shouldFindEmbroideryCategoriesByIds() {
        // given: some categories
        var category1 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_1"),
                EmbroideryCategoryName.of("Category 1")
        );
        var category2 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_2"),
                EmbroideryCategoryName.of("Category 2")
        );
        var category3 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_3"),
                EmbroideryCategoryName.of("Category 3")
        );
        save(category1);
        save(category2);
        save(category3);

        // when: finding the categories by ids
        var found = findByIds(Set.of(category1.getId(), category3.getId()));

        // then: the categories are found
        assertThat(found).containsExactlyInAnyOrder(category1, category3);
    }

    @Test
    void shouldFindAllEmbroideryCategories() {
        // given: some categories
        var category1 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_1"),
                EmbroideryCategoryName.of("Category 1")
        );
        var category2 = EmbroideryCategory.of(
                EmbroideryCategoryId.of("CATEGORY_ID_2"),
                EmbroideryCategoryName.of("Category 2")
        );
        save(category1);
        save(category2);

        // when: finding all categories
        var found = findAll();

        // then: all categories are found
        assertThat(found).containsExactlyInAnyOrder(category1, category2);
    }

    private void save(EmbroideryCategory category) {
        repo.save(category).block();
    }

    private EmbroideryCategory findById(EmbroideryCategoryId id) {
        return repo.findByIds(Set.of(id)).blockFirst();
    }

    private void removeById(EmbroideryCategoryId id) {
        repo.removeById(id).block();
    }

    private List<EmbroideryCategory> findByIds(Set<EmbroideryCategoryId> ids) {
        return repo.findByIds(ids).collectList().block();
    }

    private List<EmbroideryCategory> findAll() {
        return repo.findAll().collectList().block();
    }

}
