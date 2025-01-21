package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import reactor.core.publisher.Mono;

public interface ProductLookupRepo extends EventSourcingReadModelRepo<ProductId, LookupProduct> {

    Mono<LookupProduct> findById(ProductId id);

}
