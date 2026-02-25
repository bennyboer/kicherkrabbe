package de.bennyboer.kicherkrabbe.offers.persistence.categories;

import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OfferCategoryRepoTest {

    private OfferCategoryRepo repo;

    protected abstract OfferCategoryRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldSaveCategory() {
        var category = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Dresses")
        );
        var saved = save(category);

        assertThat(saved).isEqualTo(category);
    }

    @Test
    void shouldFindCategoryById() {
        var category = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Dresses")
        );
        save(category);

        var found = findById(OfferCategoryId.of("CAT_1"));
        assertThat(found).isEqualTo(category);

        var notFound = findById(OfferCategoryId.of("UNKNOWN"));
        assertThat(notFound).isNull();
    }

    @Test
    void shouldRemoveCategoryById() {
        var category1 = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Dresses")
        );
        var category2 = OfferCategory.of(
                OfferCategoryId.of("CAT_2"),
                OfferCategoryName.of("Shirts")
        );
        save(category1);
        save(category2);

        removeById(OfferCategoryId.of("CAT_1"));

        assertThat(findById(OfferCategoryId.of("CAT_1"))).isNull();
        assertThat(findById(OfferCategoryId.of("CAT_2"))).isEqualTo(category2);
    }

    @Test
    void shouldFindAllCategories() {
        var category1 = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Dresses")
        );
        var category2 = OfferCategory.of(
                OfferCategoryId.of("CAT_2"),
                OfferCategoryName.of("Shirts")
        );
        save(category1);
        save(category2);

        var all = findAll();
        assertThat(all).containsExactlyInAnyOrder(category1, category2);
    }

    @Test
    void shouldUpdateExistingCategory() {
        var category = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Dresses")
        );
        save(category);

        var updated = OfferCategory.of(
                OfferCategoryId.of("CAT_1"),
                OfferCategoryName.of("Evening Dresses")
        );
        save(updated);

        var found = findById(OfferCategoryId.of("CAT_1"));
        assertThat(found).isEqualTo(updated);

        var all = findAll();
        assertThat(all).hasSize(1);
    }

    private OfferCategory save(OfferCategory category) {
        return repo.save(category).block();
    }

    private void removeById(OfferCategoryId id) {
        repo.removeById(id).block();
    }

    private OfferCategory findById(OfferCategoryId id) {
        return repo.findById(id).block();
    }

    private List<OfferCategory> findAll() {
        return repo.findAll().collectList().block();
    }

}
