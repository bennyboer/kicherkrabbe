package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPatternPage;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;

public class InMemoryPatternLookupRepo extends InMemoryEventSourcingReadModelRepo<PatternId, LookupPattern>
        implements PatternLookupRepo {

    @Override
    protected PatternId getId(LookupPattern readModel) {
        return readModel.getId();
    }

    @Override
    public Mono<LookupPatternPage> find(
            Collection<PatternId> patternIds,
            Set<PatternCategoryId> categories,
            String searchTerm,
            long skip,
            long limit
    ) {
        return getAll()
                .filter(pattern -> patternIds.contains(pattern.getId()))
                .filter(pattern -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return pattern.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .sort(Comparator.comparing(LookupPattern::getCreatedAt))
                .collectList()
                .flatMap(patterns -> {
                    long total = patterns.size();

                    return Flux.fromIterable(patterns)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupPatternPage.of(skip, limit, total, results));
                });
    }

    @Override
    public Flux<LookupPattern> findByCategory(PatternCategoryId categoryId) {
        return getAll()
                .filter(pattern -> pattern.getCategories().contains(categoryId));
    }

    @Override
    public Mono<LookupPattern> findById(PatternId internalPatternId) {
        return get(internalPatternId);
    }

    @Override
    public Flux<PatternCategoryId> findUniqueCategories() {
        return getAll()
                .flatMap(pattern -> Flux.fromIterable(pattern.getCategories()))
                .distinct();
    }

    @Override
    public Mono<LookupPattern> findPublished(PatternId id) {
        return getAll()
                .filter(pattern -> pattern.getId().equals(id) && pattern.isPublished())
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupPatternPage> findPublished(
            String searchTerm,
            Set<PatternCategoryId> categories,
            boolean ascending,
            long skip,
            long limit
    ) {
        Comparator<LookupPattern> comparator = Comparator.comparing(pattern -> pattern.getName().getValue());
        if (!ascending) {
            comparator = comparator.reversed();
        }

        return getAll()
                .filter(LookupPattern::isPublished)
                .filter(pattern -> {
                    if (searchTerm.isBlank()) {
                        return true;
                    }

                    return pattern.getName()
                            .getValue()
                            .toLowerCase(Locale.ROOT)
                            .contains(searchTerm.toLowerCase(Locale.ROOT));
                })
                .filter(pattern -> categories.isEmpty() || pattern.getCategories()
                        .stream()
                        .anyMatch(categories::contains))
                .sort(comparator)
                .collectList()
                .flatMap(patterns -> {
                    long total = patterns.size();

                    return Flux.fromIterable(patterns)
                            .skip(skip)
                            .take(limit)
                            .collectList()
                            .map(results -> LookupPatternPage.of(skip, limit, total, results));
                });
    }

}
