package de.bennyboer.kicherkrabbe.products.persistence.lookup.product;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.EventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;

public interface ProductLookupRepo extends EventSourcingReadModelRepo<ProductId, LookupProduct> {

    Mono<LookupProduct> findById(ProductId id);

    Mono<LookupProductPage> findByIds(
            Set<ProductId> ids,
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit
    );

    Flux<LookupProduct> findByLink(LinkType linkType, LinkId linkId);

}
