package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.inmemory;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.inmemory.InMemoryEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.DateRangeFilter;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProductPage;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.Set;

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
    public Mono<LookupProductPage> findByIds(
            Set<ProductId> ids,
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit
    ) {
        var dateRangeFilter = DateRangeFilter.of(from, to);

        return getAll()
                .filter(product -> ids.contains(product.getId()))
                .filter(product -> dateRangeFilter.contains(product.getCreatedAt()))
                .filter(product -> searchTerm.isBlank() || product.getNumber()
                        .getValue()
                        .toLowerCase()
                        .contains(searchTerm.toLowerCase()))
                .sort(Comparator.comparing(LookupProduct::getCreatedAt).reversed())
                .collectList()
                .map(products -> {
                    long total = products.size();
                    long fromIdx = Math.min(skip, total);
                    long toIdx = Math.min(skip + limit, total);

                    return LookupProductPage.of(total, products.subList((int) fromIdx, (int) toIdx));
                });
    }

    @Override
    public Flux<LookupProduct> findByLink(LinkType linkType, LinkId linkId) {
        return getAll()
                .filter(product -> product.getLinks().contains(linkType, linkId));
    }

    @Override
    public Flux<LookupProduct> findAll() {
        return getAll();
    }

}
