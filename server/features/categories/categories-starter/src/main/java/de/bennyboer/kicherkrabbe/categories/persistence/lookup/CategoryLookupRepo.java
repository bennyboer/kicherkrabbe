package de.bennyboer.kicherkrabbe.categories.persistence.lookup;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface CategoryLookupRepo extends EventSourcingReadModelRepo<CategoryId, LookupCategory> {

    Mono<LookupCategoryPage> find(Collection<CategoryId> categoryIds, String searchTerm, long skip, long limit);

    Mono<LookupCategoryPage> findByGroup(
            Collection<CategoryId> categoryIds,
            CategoryGroup group,
            String searchTerm,
            long skip,
            long limit
    );

    Mono<LookupCategory> findById(CategoryId categoryId);

}
