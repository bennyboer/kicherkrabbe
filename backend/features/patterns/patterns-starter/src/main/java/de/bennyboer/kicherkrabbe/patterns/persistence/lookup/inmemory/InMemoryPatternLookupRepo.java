package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.patterns.PatternAlias;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.PatternId;
import de.bennyboer.kicherkrabbe.patterns.PatternNumber;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPatternPage;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.PatternLookupRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

public class InMemoryPatternLookupRepo
        extends InMemoryEventSourcingReadModelRepo<PatternId, LookupPattern>
        implements PatternLookupRepo {

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
                .filter(pattern -> categories.isEmpty() || pattern.getCategories()
                        .stream()
                        .anyMatch(categories::contains))
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
    public Mono<LookupPattern> findByAlias(PatternAlias alias) {
        return getAll()
                .filter(pattern -> pattern.getAlias().equals(alias))
                .singleOrEmpty();
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
            Set<Long> sizes,
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
                .filter(pattern -> {
                    if (sizes.isEmpty()) {
                        return true;
                    }

                    var availableSizes = new HashSet<>();
                    for (var variant : pattern.getVariants()) {
                        for (var range : variant.getPricedSizeRanges()) {
                            availableSizes.add(range.getFrom());
                            range.getTo().ifPresent(availableSizes::add);
                        }
                    }

                    return !Collections.disjoint(sizes, availableSizes);
                })
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

    @Override
    public Mono<LookupPattern> findByNumber(PatternNumber number) {
        return getAll()
                .filter(pattern -> pattern.getNumber().equals(number))
                .singleOrEmpty();
    }

}
