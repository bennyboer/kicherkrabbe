package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

public interface EmbroideryLookupRepo extends EventSourcingReadModelRepo<EmbroideryId, LookupEmbroidery> {

    Mono<LookupEmbroideryPage> find(
            Collection<EmbroideryId> embroideryIds,
            Set<EmbroideryCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    );

    Mono<LookupEmbroidery> findById(EmbroideryId id);

    Mono<LookupEmbroidery> findByAlias(EmbroideryAlias alias);

    Flux<LookupEmbroidery> findByCategory(EmbroideryCategoryId categoryId);

    Flux<EmbroideryCategoryId> findUniqueCategories();

    Mono<LookupEmbroidery> findPublished(EmbroideryId id);

    Mono<LookupEmbroidery> findPublishedByAlias(EmbroideryAlias alias);

    Mono<LookupEmbroideryPage> findPublished(
            String searchTerm,
            Set<EmbroideryCategoryId> categories,
            boolean ascending,
            long skip,
            long limit
    );

    Flux<LookupEmbroidery> findFeatured();

}
