package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import reactor.core.publisher.Mono;

public class InMemoryProductLookupRepo
        extends InMemoryEventSourcingReadModelRepo<ProductId, LookupProduct>
        implements ProductLookupRepo {

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        return getAll()
                .filter(product -> product.getId().equals(id))
                .singleOrEmpty();
    }

    @Override
    protected ProductId getId(LookupProduct readModel) {
        return readModel.getId();
    }

}
