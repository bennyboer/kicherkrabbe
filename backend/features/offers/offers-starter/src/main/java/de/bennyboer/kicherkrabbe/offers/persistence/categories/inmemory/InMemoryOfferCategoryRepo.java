package de.bennyboer.kicherkrabbe.offers.persistence.categories.inmemory;

import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.persistence.categories.OfferCategoryRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOfferCategoryRepo implements OfferCategoryRepo {

    private final Map<OfferCategoryId, OfferCategory> lookup = new ConcurrentHashMap<>();

    @Override
    public Mono<OfferCategory> save(OfferCategory category) {
        return Mono.fromCallable(() -> {
            lookup.put(category.getId(), category);
            return category;
        });
    }

    @Override
    public Mono<Void> removeById(OfferCategoryId id) {
        return Mono.fromCallable(() -> {
            lookup.remove(id);
            return null;
        });
    }

    @Override
    public Mono<OfferCategory> findById(OfferCategoryId id) {
        return Mono.justOrEmpty(lookup.get(id));
    }

    @Override
    public Flux<OfferCategory> findAll() {
        return Flux.fromIterable(lookup.values());
    }

}
