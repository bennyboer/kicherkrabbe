package de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryAlias;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.EmbroideryLookupRepo;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroidery;
import de.bennyboer.kicherkrabbe.embroideries.persistence.lookup.LookupEmbroideryPage;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InMemoryEmbroideryLookupRepo
        extends InMemoryEventSourcingReadModelRepo<EmbroideryId, LookupEmbroidery>
        implements EmbroideryLookupRepo {

    @Override
    public Mono<LookupEmbroideryPage> find(
            Collection<EmbroideryId> embroideryIds,
            Set<EmbroideryCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return getAll()
                .filter(embroidery -> embroideryIds.contains(embroidery.getId()))
                .filter(embroidery -> matchesSearchTerm(embroidery, searchTerm))
                .filter(embroidery -> matchesCategories(embroidery, categories))
                .sort(Comparator.comparing(LookupEmbroidery::getCreatedAt))
                .collectList()
                .flatMap(embroideries -> toPage(embroideries, skip, limit));
    }

    @Override
    public Mono<LookupEmbroidery> findById(EmbroideryId id) {
        return get(id);
    }

    @Override
    public Mono<LookupEmbroidery> findByAlias(EmbroideryAlias alias) {
        return getAll()
                .filter(embroidery -> embroidery.getAlias().equals(alias))
                .singleOrEmpty();
    }

    @Override
    public Flux<LookupEmbroidery> findByCategory(EmbroideryCategoryId categoryId) {
        return getAll()
                .filter(embroidery -> embroidery.getCategories().contains(categoryId));
    }

    @Override
    public Flux<EmbroideryCategoryId> findUniqueCategories() {
        return getAll()
                .flatMap(embroidery -> Flux.fromIterable(embroidery.getCategories()))
                .distinct();
    }

    @Override
    public Mono<LookupEmbroidery> findPublished(EmbroideryId id) {
        return getAll()
                .filter(embroidery -> embroidery.getId().equals(id) && embroidery.isPublished())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupEmbroidery> findPublishedByAlias(EmbroideryAlias alias) {
        return getAll()
                .filter(embroidery -> embroidery.getAlias().equals(alias) && embroidery.isPublished())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupEmbroideryPage> findPublished(
            String searchTerm,
            Set<EmbroideryCategoryId> categories,
            boolean ascending,
            long skip,
            long limit
    ) {
        Comparator<LookupEmbroidery> comparator = Comparator.comparing(embroidery -> embroidery.getName().getValue());
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return getAll()
                .filter(LookupEmbroidery::isPublished)
                .filter(embroidery -> matchesSearchTerm(embroidery, searchTerm))
                .filter(embroidery -> matchesCategories(embroidery, categories))
                .sort(comparator)
                .collectList()
                .flatMap(embroideries -> toPage(embroideries, skip, limit));
    }

    @Override
    public Flux<LookupEmbroidery> findFeatured() {
        return getAll()
                .filter(embroidery -> embroidery.isPublished() && embroidery.isFeatured());
    }

    private boolean matchesSearchTerm(LookupEmbroidery embroidery, String searchTerm) {
        if (searchTerm.isBlank()) {
            return true;
        }

        return embroidery.getName()
                .getValue()
                .toLowerCase(Locale.ROOT)
                .contains(searchTerm.toLowerCase(Locale.ROOT));
    }

    private boolean matchesCategories(LookupEmbroidery embroidery, Set<EmbroideryCategoryId> categories) {
        return categories.isEmpty() || embroidery.getCategories()
                .stream()
                .anyMatch(categories::contains);
    }

    private Mono<LookupEmbroideryPage> toPage(List<LookupEmbroidery> embroideries, long skip, long limit) {
        long total = embroideries.size();

        return Flux.fromIterable(embroideries)
                .skip(skip)
                .take(limit)
                .collectList()
                .map(results -> LookupEmbroideryPage.of(skip, limit, total, results));
    }

}
