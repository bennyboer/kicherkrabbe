package de.bennyboer.kicherkrabbe.patterns.persistence.categories.inmemory;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.PatternCategoryRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPatternCategoryRepo implements PatternCategoryRepo {

    private final Map<PatternCategoryId, PatternCategory> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<PatternCategory> save(PatternCategory category) {
        return Mono.fromCallable(() -> {
            lookup.put(category.getId(), category);
            return category;
        });
    }

    @Override
    public Mono<Void> removeById(PatternCategoryId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Flux<PatternCategory> findByIds(Set<PatternCategoryId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

    @Override
    public Flux<PatternCategory> findAll() {
        return Flux.fromIterable(lookup.values());
    }

}
