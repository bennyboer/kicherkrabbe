package de.bennyboer.kicherkrabbe.categories.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.CategoryLookupRepo;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategory;
import de.bennyboer.kicherkrabbe.categories.persistence.lookup.LookupCategoryPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class InMemoryCategoryLookupRepo extends InMemoryEventSourcingReadModelRepo<CategoryId, LookupCategory>
        implements CategoryLookupRepo {

    @Override
    public Mono<LookupCategoryPage> find(Collection<CategoryId> categoryIds, String searchTerm, long skip, long limit) {
        return getAll()
                .filter(category -> categoryIds.contains(category.getId()))
                .filter(category -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return category.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupCategory::getCreatedAt))
                .collectList()
                .flatMap(categories -> {
                    long total = categories.size();

                    return Flux.fromIterable(categories)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupCategoryPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupCategoryPage> findByGroup(
            Collection<CategoryId> categoryIds,
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit
    ) {
        return getAll()
                .filter(category -> categoryIds.contains(category.getId()))
                .filter(category -> category.getGroup() == group)
                .filter(category -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return category.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupCategory::getCreatedAt))
                .collectList()
                .flatMap(categories -> {
                    long total = categories.size();

                    return Flux.fromIterable(categories)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupCategoryPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Mono<LookupCategory> findById(CategoryId categoryId) {
        return get(categoryId);
    }

}
