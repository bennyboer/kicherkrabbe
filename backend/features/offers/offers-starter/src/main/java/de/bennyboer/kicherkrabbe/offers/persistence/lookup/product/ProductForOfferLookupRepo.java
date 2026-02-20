package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import reactor.core.publisher.Mono;

public interface ProductForOfferLookupRepo extends EventSourcingReadModelRepo<ProductId, LookupProduct> {

    Mono<LookupProduct> findById(ProductId id);

    Mono<LookupProductPage> findAll(String searchTerm, long skip, long limit);

}
