package de.bennyboer.kicherkrabbe.embroideries.persistence.categories.inmemory;

import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategory;
import de.bennyboer.kicherkrabbe.embroideries.EmbroideryCategoryId;
import de.bennyboer.kicherkrabbe.embroideries.persistence.categories.EmbroideryCategoryRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEmbroideryCategoryRepo implements EmbroideryCategoryRepo {

    private final Map<EmbroideryCategoryId, EmbroideryCategory> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<EmbroideryCategory> save(EmbroideryCategory category) {
        return Mono.fromCallable(() -> {
            lookup.put(category.getId(), category);
            return category;
        });
    }

    @Override
    public Mono<Void> removeById(EmbroideryCategoryId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Flux<EmbroideryCategory> findByIds(Set<EmbroideryCategoryId> ids) {
        return Flux.fromIterable(ids)
                .mapNotNull(lookup::get);
    }

    @Override
    public Flux<EmbroideryCategory> findAll() {
        return Flux.fromIterable(lookup.values());
    }

}
