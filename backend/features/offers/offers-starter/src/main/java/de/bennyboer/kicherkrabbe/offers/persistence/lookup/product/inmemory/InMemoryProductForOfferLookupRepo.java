package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.offers.ProductId;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProductPage;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.ProductForOfferLookupRepo;
import reactor.core.publisher.Mono;

import java.util.Comparator;

public class InMemoryProductForOfferLookupRepo
        extends InMemoryEventSourcingReadModelRepo<ProductId, LookupProduct>
        implements ProductForOfferLookupRepo {

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        return getAll()
                .filter(product -> product.getId().equals(id))
                .singleOrEmpty();
    }

    @Override
    public Mono<LookupProductPage> findAll(String searchTerm, long skip, long limit) {
        return getAll()
                .filter(product -> {
                    if (searchTerm == null || searchTerm.isBlank()) {
                        return true;
                    }
                    return product.getNumber().getValue().toLowerCase()
                            .contains(searchTerm.toLowerCase());
                })
                .collectSortedList(Comparator.comparing(
                        (LookupProduct p) -> p.getNumber().getValue()
                ).reversed())
                .map(products -> {
                    long total = products.size();
                    var paged = products.stream()
                            .skip(skip)
                            .limit(limit)
                            .toList();
                    return LookupProductPage.of(skip, limit, total, paged);
                });
    }

}
