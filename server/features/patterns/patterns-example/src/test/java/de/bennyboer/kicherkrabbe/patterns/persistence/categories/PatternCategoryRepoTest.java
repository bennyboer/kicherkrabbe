package de.bennyboer.kicherkrabbe.patterns.persistence.categories;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PatternCategoryRepoTest {

    private PatternCategoryRepo repo;

    protected abstract PatternCategoryRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSavePatternCategory() {
        // given: a category to save
        var category = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID"),
                PatternCategoryName.of("Category")
        );

        // when: saving the category
        save(category);

        // then: the category is saved
        var saved = findById(category.getId());
        assertThat(saved).isEqualTo(category);
    }

    @Test
    void shouldFindPatternCategoryById() {
        // given: some categories
        var category1 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_1"),
                PatternCategoryName.of("Category 1")
        );
        var category2 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_2"),
                PatternCategoryName.of("Category 2")
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
    void shouldRemovePatternCategoryById() {
        // given: some categories
        var category1 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_1"),
                PatternCategoryName.of("Category 1")
        );
        var category2 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_2"),
                PatternCategoryName.of("Category 2")
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
    void shouldFindPatternCategorysByIds() {
        // given: some categories
        var category1 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_1"),
                PatternCategoryName.of("Category 1")
        );
        var category2 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_2"),
                PatternCategoryName.of("Category 2")
        );
        var category3 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_3"),
                PatternCategoryName.of("Category 3")
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
    void shouldFindAllPatternCategorys() {
        // given: some categories
        var category1 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_1"),
                PatternCategoryName.of("Category 1")
        );
        var category2 = PatternCategory.of(
                PatternCategoryId.of("CATEGORY_ID_2"),
                PatternCategoryName.of("Category 2")
        );
        save(category1);
        save(category2);

        // when: finding all categories
        var found = findAll();

        // then: all categories are found
        assertThat(found).containsExactlyInAnyOrder(category1, category2);
    }

    private void save(PatternCategory category) {
        repo.save(category).block();
    }

    private PatternCategory findById(PatternCategoryId id) {
        return repo.findByIds(Set.of(id)).blockFirst();
    }

    private void removeById(PatternCategoryId id) {
        repo.removeById(id).block();
    }

    private List<PatternCategory> findByIds(Set<PatternCategoryId> ids) {
        return repo.findByIds(ids).collectList().block();
    }

    private List<PatternCategory> findAll() {
        return repo.findAll().collectList().block();
    }

}
