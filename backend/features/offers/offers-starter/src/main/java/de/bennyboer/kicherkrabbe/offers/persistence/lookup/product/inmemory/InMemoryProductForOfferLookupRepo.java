package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import reactor.core.publisher.Mono;

public class InMemoryProductForOfferLookupRepo
        extends InMemoryEventSourcingReadModelRepo<ProductId, LookupProduct>
        implements ProductForOfferLookupRepo {

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        return getAll()
                .filter(product -> product.getId().equals(id))
                .singleOrEmpty();
    }

}
