package de.bennyboer.kicherkrabbe.offers.persistence.lookup;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.OfferAlias;
import de.bennyboer.kicherkrabbe.offers.OfferCategoryId;
import de.bennyboer.kicherkrabbe.offers.OfferId;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface OfferLookupRepo extends EventSourcingReadModelRepo<OfferId, LookupOffer> {

    Mono<LookupOfferPage> find(Collection<OfferId> offerIds, String searchTerm, long skip, long limit);

    Mono<LookupOfferPage> findPublished(PublishedOfferQuery query);

    Mono<LookupOffer> findPublished(OfferId id);

    Mono<LookupOffer> findPublishedByAlias(OfferAlias alias);

    Mono<LookupOffer> findByAlias(OfferAlias alias);

    Flux<LookupOffer> findByProductId(ProductId productId);

    Flux<LookupOffer> findByCategoryId(OfferCategoryId categoryId);

    Flux<String> findDistinctPublishedSizes();

}
