package de.bennyboer.kicherkrabbe.offers.persistence.categories;

import de.bennyboer.kicherkrabbe.offers.OfferCategory;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OfferCategoryRepo {

    Mono<OfferCategory> save(OfferCategory category);

    Mono<Void> removeById(OfferCategoryId id);

    Mono<OfferCategory> findById(OfferCategoryId id);

    Flux<OfferCategory> findAll();

}
